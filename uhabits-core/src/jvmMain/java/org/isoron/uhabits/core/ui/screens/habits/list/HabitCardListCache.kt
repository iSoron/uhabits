/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isoron.uhabits.core.ui.screens.habits.list

import me.tatarka.inject.annotations.Inject
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.io.Logging
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitList.Order
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.tasks.Task
import org.isoron.uhabits.core.tasks.TaskRunner

/**
 * A HabitCardListCache fetches and keeps a cache of all the data necessary to
 * render a HabitCardListView.
 *
 *
 * This is needed since performing database lookups during scrolling can make
 * the ListView very slow. It also registers itself as an observer of the
 * models, in order to update itself automatically.
 *
 *
 * Note that this class is singleton-scoped, therefore it is shared among all
 * activities.
 */
@AppScope
@Inject
class HabitCardListCache(
    private val habits: HabitList,
    private val habitGroups: HabitGroupList,
    private val commandRunner: CommandRunner,
    taskRunner: TaskRunner,
    logging: Logging
) : CommandRunner.Listener {

    private val logger = logging.getLogger("HabitCardListCache")

    private var checkmarkCount = 0
    private var currentFetchTask: Task? = null
    private var listener: Listener
    private val data: CacheData
    private var filteredHabits: HabitList
    private var filteredHabitGroups: HabitGroupList
    private val taskRunner: TaskRunner

    @Synchronized
    fun cancelTasks() {
        currentFetchTask?.cancel()
    }

    @Synchronized
    fun getCheckmarks(habitID: Long): IntArray {
        return data.checkmarks[habitID]!!
    }

    @Synchronized
    fun getNotes(habitID: Long): Array<String> {
        return data.notes[habitID]!!
    }

    @Synchronized
    fun hasNoHabit(): Boolean {
        return habits.isEmpty
    }

    @Synchronized
    fun hasNoHabitGroup(): Boolean {
        return habitGroups.isEmpty
    }

    @Synchronized
    fun hasNoSubHabits(): Boolean {
        return habitGroups.all { it.habitList.isEmpty }
    }

    /**
     * Returns the habits that occupies a certain position on the list.
     *
     * @param position the position of the list of habits and groups
     * @return the habit at given position or null if position is invalid
     */
    @Synchronized
    fun getHabitByPosition(position: Int): Habit? {
        return data.positionToHabit[position]
    }

    /**
     * Returns the habit groups that occupies a certain position on the list.
     *
     * @param position the position of the list of habits and groups
     * @return the habit group at given position or null if position is invalid
     */
    @Synchronized
    fun getHabitGroupByPosition(position: Int): HabitGroup? {
        return data.positionToHabitGroup[position]
    }

    @Synchronized
    fun getIdByPosition(position: Int): Long? {
        return if (data.positionTypes[position] == STANDALONE_HABIT || data.positionTypes[position] == SUB_HABIT) {
            data.positionToHabit[position]!!.id
        } else {
            data.positionToHabitGroup[position]!!.id
        }
    }

    @get:Synchronized
    val itemCount: Int
        get() = habitCount + habitGroupCount + subHabitCount

    @get:Synchronized
    val habitCount: Int
        get() = data.habits.size

    @get:Synchronized
    val habitGroupCount: Int
        get() = data.habitGroups.size

    @get:Synchronized
    val subHabitCount: Int
        get() = data.subHabits.sumOf { it.size }

    @get:Synchronized
    @set:Synchronized
    var primaryOrder: Order
        get() = filteredHabits.primaryOrder
        set(order) {
            habits.primaryOrder = order
            habitGroups.primaryOrder = order
            filteredHabits.primaryOrder = order
            filteredHabitGroups.primaryOrder = order
            refreshAllHabits()
        }

    @get:Synchronized
    @set:Synchronized
    var secondaryOrder: Order
        get() = filteredHabits.secondaryOrder
        set(order) {
            habits.secondaryOrder = order
            habitGroups.secondaryOrder = order
            filteredHabits.secondaryOrder = order
            filteredHabitGroups.secondaryOrder = order
            refreshAllHabits()
        }

    @Synchronized
    fun getScore(id: Long): Double {
        return data.scores[id]!!
    }

    @Synchronized
    fun onAttached() {
        refreshAllHabits()
        commandRunner.addListener(this)
    }

    @Synchronized
    override fun onCommandFinished(command: Command) {
        if (command is CreateRepetitionCommand) {
            command.habit.id?.let { refreshHabit(it) }
        } else {
            refreshAllHabits()
        }
    }

    @Synchronized
    fun onDetached() {
        commandRunner.removeListener(this)
    }

    @Synchronized
    fun refreshAllHabits() {
        if (currentFetchTask != null) currentFetchTask!!.cancel()
        val task = RefreshTask()
        currentFetchTask = task
        taskRunner.execute(task)
    }

    @Synchronized
    fun refreshHabit(id: Long) {
        taskRunner.execute(RefreshTask(id))
    }

    @Synchronized
    fun remove(id: Long) {
        val position = data.habitIdToPosition[id] ?: data.groupIdToPosition[id] ?: return
        val type = data.positionTypes[position]
        if (type == STANDALONE_HABIT) {
            val h = data.positionToHabit[position]
            if (h != null) {
                val idx = data.positionIndices[position]
                data.habits.removeAt(idx)
                data.removeWithID(id, STANDALONE_HABIT)
                data.rebuildPositions()
                listener.onItemRemoved(position)
            }
        } else if (type == SUB_HABIT) {
            val h = data.positionToHabit[position]
            if (h != null) {
                val hgrID = h.groupId
                val hgrPos = data.groupIdToPosition[hgrID]
                val hgr = data.positionToHabitGroup[hgrPos]
                val hgrIdx = data.habitGroups.indexOf(hgr)
                data.subHabits[hgrIdx].remove(h)
                data.removeWithID(id, SUB_HABIT)
                data.rebuildPositions()
                listener.onItemRemoved(position)
            }
        } else if (type == HABIT_GROUP) {
            val hgr = data.positionToHabitGroup[position]
            if (hgr != null) {
                val hgrIdx = data.positionIndices[position]
                val subHabitsCopy = data.subHabits[hgrIdx].toList()
                for (habit in subHabitsCopy.reversed()) {
                    val habitPos = data.habitIdToPosition[habit.id]!!
                    data.subHabits[hgrIdx].remove(habit)
                    data.removeWithID(habit.id, SUB_HABIT)
                    data.rebuildPositions()
                    listener.onItemRemoved(habitPos)
                }
                data.subHabits.removeAt(hgrIdx)
                data.habitGroups.removeAt(hgrIdx)
                data.removeWithID(hgr.id, HABIT_GROUP)
                data.rebuildPositions()
                listener.onItemRemoved(position)
            }
        }
    }

    @Synchronized
    fun reorder(from: Int, to: Int) {
        if (from == to) return
        val type = data.positionTypes[from]
        if (type == STANDALONE_HABIT || type == SUB_HABIT) {
            val habit = data.positionToHabit[from]!!
            data.performMove(habit, from, to)
        } else {
            val habitGroup = data.positionToHabitGroup[from]!!
            data.performMove(habitGroup, from, to)
        }
    }

    @Synchronized
    fun setCheckmarkCount(checkmarkCount: Int) {
        this.checkmarkCount = checkmarkCount
    }

    @Synchronized
    fun setFilter(matcher: HabitMatcher) {
        filteredHabits = habits.getFiltered(matcher)
        filteredHabitGroups = habitGroups.getFiltered(matcher)
    }

    @Synchronized
    fun setListener(listener: Listener) {
        this.listener = listener
    }

    /**
     * Interface definition for a callback to be invoked when the data on the
     * cache has been modified.
     */
    interface Listener {
        fun onItemChanged(position: Int) {}
        fun onItemInserted(position: Int) {}
        fun onItemMoved(oldPosition: Int, newPosition: Int) {}
        fun onItemRemoved(position: Int) {}
        fun onRefreshFinished() {}
    }

    private inner class CacheData {
        val habits: MutableList<Habit>
        val habitGroups: MutableList<HabitGroup>
        val subHabits: MutableList<MutableList<Habit>>
        val habitIdToPosition: MutableMap<Long?, Int>
        val groupIdToPosition: MutableMap<Long?, Int>
        val positionTypes: MutableList<Int>
        val positionIndices: MutableList<Int>
        val positionToHabit: MutableMap<Int, Habit>
        val positionToHabitGroup: MutableMap<Int, HabitGroup>
        val checkmarks: MutableMap<Long?, IntArray>
        val scores: MutableMap<Long?, Double>
        val notes: MutableMap<Long?, Array<String>>

        @Synchronized
        fun copyCheckmarksFrom(oldData: CacheData) {
            val empty = IntArray(checkmarkCount)
            for (id in habitIdToPosition.keys) {
                if (oldData.checkmarks.containsKey(id)) {
                    checkmarks[id] =
                        oldData.checkmarks[id]!!
                } else {
                    checkmarks[id] = empty
                }
            }
        }

        @Synchronized
        fun copyNoteIndicatorsFrom(oldData: CacheData) {
            val empty = (0..checkmarkCount).map { "" }.toTypedArray()
            for (id in habitIdToPosition.keys) {
                if (oldData.notes.containsKey(id)) {
                    notes[id] =
                        oldData.notes[id]!!
                } else {
                    notes[id] = empty
                }
            }
        }

        @Synchronized
        fun copyScoresFrom(oldData: CacheData) {
            for (id in habitIdToPosition.keys) {
                if (oldData.scores.containsKey(id)) {
                    scores[id] =
                        oldData.scores[id]!!
                } else {
                    scores[id] = 0.0
                }
            }
            for (id in groupIdToPosition.keys) {
                if (oldData.scores.containsKey(id)) {
                    scores[id] =
                        oldData.scores[id]!!
                } else {
                    scores[id] = 0.0
                }
            }
        }

        @Synchronized
        fun fetchHabits() {
            for (h in filteredHabits) {
                if (h.uuid == null || h.id == null) continue
                habits.add(h)
            }

            for (hgr in filteredHabitGroups) {
                if (hgr.uuid == null || hgr.id == null) continue
                habitGroups.add(hgr)
                val habitList = LinkedList<Habit>()
                for (h in hgr.habitList) {
                    habitList.add(h)
                }
                subHabits.add(habitList)
            }
        }

        @Synchronized
        fun rebuildPositions() {
            positionToHabit.clear()
            positionToHabitGroup.clear()
            habitIdToPosition.clear()
            groupIdToPosition.clear()
            positionTypes.clear()
            positionIndices.clear()
            var position = 0
            for ((idx, h) in habits.withIndex()) {
                habitIdToPosition[h.id] = position
                positionToHabit[position] = h
                positionTypes.add(STANDALONE_HABIT)
                positionIndices.add(idx)
                position++
            }

            for ((idx, hgr) in habitGroups.withIndex()) {
                groupIdToPosition[hgr.id] = position
                positionToHabitGroup[position] = hgr
                positionTypes.add(HABIT_GROUP)
                positionIndices.add(idx)
                val habitList = subHabits[idx]
                position++

                for ((hIdx, h) in habitList.withIndex()) {
                    habitIdToPosition[h.id] = position
                    positionToHabit[position] = h
                    positionTypes.add(SUB_HABIT)
                    positionIndices.add(hIdx)
                    position++
                }
            }
        }

        @Synchronized
        fun isValidInsert(habit: Habit, position: Int): Boolean {
            if (habit.groupId == null) {
                return position <= habits.size
            } else {
                val parentPos = groupIdToPosition[habit.groupId] ?: return false
                val parent = positionToHabitGroup[parentPos] ?: return false
                val parentPosition = groupIdToPosition[habit.groupId]!!
                val parentIndex = habitGroups.indexOf(parent)
                val nextGroup = habitGroups.getOrNull(parentIndex + 1)
                val nextGroupPosition = groupIdToPosition[nextGroup?.id]
                return (position > parentPosition && position <= positionTypes.size) && (nextGroupPosition == null || position <= nextGroupPosition)
            }
        }

        @Synchronized
        fun isValidInsert(habitGroup: HabitGroup, position: Int): Boolean {
            return (position == positionTypes.size) || (positionTypes[position] == HABIT_GROUP)
        }

        @Synchronized
        fun performMove(
            habit: Habit,
            fromPosition: Int,
            toPosition: Int
        ) {
            val type = positionTypes[fromPosition]
            if (type == HABIT_GROUP) return

            // Workaround for https://github.com/iSoron/uhabits/issues/968
            val checkedToPosition = if (toPosition >= positionTypes.size) {
                logger.error("performMove: $toPosition for habit is strictly higher than ${habits.size}")
                positionTypes.size - 1
            } else {
                toPosition
            }

            val verifyPosition = if (fromPosition > checkedToPosition) checkedToPosition else checkedToPosition + 1
            if (!isValidInsert(habit, verifyPosition)) return

            if (type == STANDALONE_HABIT) {
                val fromIdx = positionIndices[fromPosition]
                habits.removeAt(fromIdx)

                // Cap the internal index to avoid IndexOutOfBoundsException
                var toIdx = checkedToPosition
                if (toIdx > habits.size) toIdx = habits.size
                habits.add(toIdx, habit)
            } else {
                val hgrPos = groupIdToPosition[habit.groupId]!!
                val hgr = positionToHabitGroup[hgrPos]!!
                val hgrIdx = habitGroups.indexOf(hgr)
                val fromIdx = positionIndices[fromPosition]
                subHabits[hgrIdx].removeAt(fromIdx)

                val toIdx = checkedToPosition - groupIdToPosition[hgr.id]!! - 1
                subHabits[hgrIdx].add(toIdx, habit)
            }

            rebuildPositions()
            listener.onItemMoved(fromPosition, checkedToPosition)
        }

        @Synchronized
        fun performMove(
            habitGroup: HabitGroup,
            fromPosition: Int,
            toPosition: Int
        ) {
            if (positionTypes[fromPosition] != HABIT_GROUP) return
            if (!isValidInsert(habitGroup, toPosition)) return
            val fromIdx = positionIndices[fromPosition]
            val habitList = subHabits[fromIdx]

            var toIdx = if (toPosition >= positionTypes.size) {
                habitGroups.size - 1 // Fix for dragging to bottom
            } else {
                habitGroups.indexOf(positionToHabitGroup[toPosition])
            }
            if (toIdx < 0) toIdx = habitGroups.size - 1

            habitGroups.removeAt(fromIdx)
            subHabits.removeAt(fromIdx)

            habitGroups.add(toIdx, habitGroup)
            subHabits.add(toIdx, habitList)

            rebuildPositions()
            listener.onItemMoved(fromPosition, toPosition)
        }

        fun removeWithID(id: Long?, type: Int) {
            if (type == HABIT_GROUP) {
                groupIdToPosition.remove(id)
            } else {
                habitIdToPosition.remove(id)
            }
            scores.remove(id)
            notes.remove(id)
            checkmarks.remove(id)
        }

        /**
         * Creates a new CacheData without any content.
         */
        init {
            habits = mutableListOf()
            habitGroups = mutableListOf()
            subHabits = mutableListOf()
            positionTypes = mutableListOf()
            positionIndices = mutableListOf()
            habitIdToPosition = mutableMapOf()
            groupIdToPosition = mutableMapOf()
            positionToHabit = mutableMapOf()
            positionToHabitGroup = mutableMapOf()
            checkmarks = mutableMapOf()
            scores = mutableMapOf()
            notes = mutableMapOf()
        }
    }

    private inner class RefreshTask : Task {
        private val newData: CacheData
        private val targetID: Long?
        private var isCancelled = false
        private var runner: TaskRunner? = null

        constructor() {
            newData = CacheData()
            targetID = null
            isCancelled = false
        }

        constructor(targetID: Long) {
            newData = CacheData()
            this.targetID = targetID
        }

        @Synchronized
        override fun cancel() {
            isCancelled = true
        }

        @Synchronized
        override fun doInBackground() {
            newData.fetchHabits()
            newData.rebuildPositions()
            newData.copyScoresFrom(data)
            newData.copyCheckmarksFrom(data)
            newData.copyNoteIndicatorsFrom(data)
            val today = getToday()
            val dateFrom = today.minus(checkmarkCount - 1)
            if (runner != null) runner!!.publishProgress(this, -1)
            for ((position, type) in newData.positionTypes.withIndex()) {
                if (isCancelled) return
                if (type == STANDALONE_HABIT || type == SUB_HABIT) {
                    val habit = newData.positionToHabit[position]!!
                    if (targetID != null && targetID != habit.id) continue
                    newData.scores[habit.id] = habit.scores[today].value
                    val checkmarkList = mutableListOf<Int>()
                    val noteList = mutableListOf<String>()
                    for ((_, value, note) in habit.computedEntries.getByInterval(dateFrom, today)) {
                        val checkmarkList = mutableListOf<Int>()
                        val noteList = mutableListOf<String>()
                    }
                    newData.checkmarks[habit.id] = checkmarkList.toIntArray()
                    newData.notes[habit.id] = noteList.toTypedArray()
                    runner!!.publishProgress(this, position)
                } else if (type == HABIT_GROUP) {
                    val habitGroup = newData.positionToHabitGroup[position]!!
                    if (targetID != null && targetID != habitGroup.id) continue
                    newData.scores[habitGroup.id] = habitGroup.scores[today].value
                    runner!!.publishProgress(this, position)
                }
            }
        }

        @Synchronized
        override fun onAttached(runner: TaskRunner) {
            this.runner = runner
        }

        @Synchronized
        override fun onPostExecute() {
            currentFetchTask = null
            listener.onRefreshFinished()
        }

        @Synchronized
        override fun onProgressUpdate(currentPosition: Int) {
            if (currentPosition < 0) processRemovedHabits() else processPosition(currentPosition)
        }

        @Synchronized
        private fun performInsert(habit: Habit, position: Int) {
            if (!data.isValidInsert(habit, position)) return
            val id = habit.id
            val habitIndex = newData.positionIndices[position]
            if (habit.groupId == null) {
                data.habits.add(habitIndex, habit)
            } else {
                val hgrPos = data.groupIdToPosition[habit.groupId]!!
                val hgrIdx = data.positionIndices[hgrPos]
                data.subHabits[hgrIdx].add(habitIndex, habit)
            }
            data.scores[id] = newData.scores[id]!!
            data.checkmarks[id] = newData.checkmarks[id]!!
            data.notes[id] = newData.notes[id]!!

            data.rebuildPositions()
            listener.onItemInserted(position)
        }

        @Synchronized
        private fun performInsert(habitGroup: HabitGroup, position: Int) {
            if (!data.isValidInsert(habitGroup, position)) return
            val id = habitGroup.id
            val idx = if (position < data.positionIndices.size) {
                data.positionIndices[position]
            } else {
                data.habitGroups.size
            }

            data.habitGroups.add(idx, habitGroup)

            data.subHabits.add(idx, LinkedList<Habit>())
            data.scores[id] = newData.scores[id]!!

            data.rebuildPositions()
            listener.onItemInserted(position)
        }

        @Synchronized
        private fun performUpdate(id: Long, position: Int) {
            var unchanged = true
            val oldScore = data.scores[id]!!
            val newScore = newData.scores[id]!!
            if (oldScore != newScore) unchanged = false

            if (data.positionTypes[position] != HABIT_GROUP) {
                val oldCheckmarks = data.checkmarks[id]
                val newCheckmarks = newData.checkmarks[id]!!
                val oldNoteIndicators = data.notes[id]
                val newNoteIndicators = newData.notes[id]!!
                if (!oldCheckmarks.contentEquals(newCheckmarks)) unchanged = false
                if (!oldNoteIndicators.contentEquals(newNoteIndicators)) unchanged = false
                if (unchanged) return
                data.checkmarks[id] = newCheckmarks
                data.notes[id] = newNoteIndicators
            }

            if (unchanged) return
            data.scores[id] = newScore
            listener.onItemChanged(position)
        }

        @Synchronized
        private fun processPosition(currentPosition: Int) {
            val type = newData.positionTypes[currentPosition]

            if (type == STANDALONE_HABIT || type == SUB_HABIT) {
                val habit = newData.positionToHabit[currentPosition]!!
                val id = habit.id ?: throw NullPointerException()
                val prevPosition = data.habitIdToPosition[id] ?: -1
                val newPosition = if (type == STANDALONE_HABIT) {
                    currentPosition
                } else {
                    val hgrPos = data.groupIdToPosition[habit.groupId]!!
                    val newHgrPos = newData.groupIdToPosition[habit.groupId]!! // Get new position
                    val newHgrIdx = newData.positionIndices[newHgrPos] // Get new index
                    newData.subHabits[newHgrIdx].indexOf(habit) + hgrPos + 1
                }
                if (prevPosition < 0) {
                    performInsert(habit, newPosition)
                } else {
                    if (prevPosition != newPosition) {
                        data.performMove(
                            habit,
                            prevPosition,
                            newPosition
                        )
                    }
                    performUpdate(id, currentPosition)
                }
            } else if (type == HABIT_GROUP) {
                val habitGroup = newData.positionToHabitGroup[currentPosition]!!
                val id = habitGroup.id ?: throw NullPointerException()
                val prevPosition = data.groupIdToPosition[id] ?: -1
                if (prevPosition < 0) {
                    performInsert(habitGroup, currentPosition)
                } else {
                    if (prevPosition != currentPosition) {
                        data.performMove(
                            habitGroup,
                            prevPosition,
                            currentPosition
                        )
                    }
                    performUpdate(id, currentPosition)
                }
            }
        }

        @Synchronized
        private fun processRemovedHabits() {
            val beforeHabits: Set<Long?> = data.habitIdToPosition.keys
            val beforeGroups: Set<Long?> = data.groupIdToPosition.keys
            val afterHabits: Set<Long?> = newData.habitIdToPosition.keys
            val afterGroups: Set<Long?> = newData.groupIdToPosition.keys
            val removedHabits = beforeHabits - afterHabits
            val removedGroups = beforeGroups - afterGroups

            for (hId in removedHabits.sortedByDescending { data.habitIdToPosition[it] ?: -1 }) {
                remove(hId!!)
            }

            for (grId in removedGroups.sortedByDescending { data.groupIdToPosition[it] ?: -1 }) {
                remove(grId!!)
            }
        }
    }

    companion object {
        const val STANDALONE_HABIT = 0
        const val HABIT_GROUP = 1
        const val SUB_HABIT = 2
    }

    init {
        filteredHabits = habits
        filteredHabitGroups = habitGroups

        this.taskRunner = taskRunner
        listener = object : Listener {}
        data = CacheData()
    }
}
