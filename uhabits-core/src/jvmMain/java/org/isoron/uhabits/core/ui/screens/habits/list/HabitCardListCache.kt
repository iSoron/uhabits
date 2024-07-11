/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

import org.apache.commons.lang3.ArrayUtils
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.io.Logging
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitList.Order
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.tasks.Task
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import java.util.Arrays
import java.util.LinkedList
import java.util.TreeSet
import javax.inject.Inject

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
class HabitCardListCache @Inject constructor(
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
        val position = data.idToPosition[id] ?: return
        val type = data.positionTypes[position]
        if (type == STANDALONE_HABIT) {
            val h = data.idToHabit[id]
            if (h != null) {
                data.habits.removeAt(position)
                data.removeWithID(id)
                data.removeWithPos(position)
                data.decrementPositions(position + 1, data.positionTypes.size)
                listener.onItemRemoved(position)
            }
        } else if (type == SUB_HABIT) {
            val h = data.idToHabit[id]
            if (h != null) {
                val hgrID = h.groupId
                val hgr = data.idToHabitGroup[hgrID]
                val hgrIdx = data.habitGroups.indexOf(hgr)
                data.subHabits[hgrIdx].remove(h)
                data.removeWithID(id)
                data.removeWithPos(position)
                data.decrementPositions(position + 1, data.positionTypes.size)
                listener.onItemRemoved(position)
            }
        } else if (type == HABIT_GROUP) {
            val hgr = data.idToHabitGroup[id]
            if (hgr != null) {
                val hgrIdx = data.positionIndices[position]

                for (habit in data.subHabits[hgrIdx].reversed()) {
                    val habitPos = data.idToPosition[habit.id]!!
                    data.removeWithID(habit.id)
                    listener.onItemRemoved(habitPos)
                }
                data.subHabits.removeAt(hgrIdx)
                data.habitGroups.removeAt(hgrIdx)
                data.removeWithID(hgr.id)
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
        val idToHabit: HashMap<Long?, Habit> = HashMap()
        val idToHabitGroup: HashMap<Long?, HabitGroup> = HashMap()
        val habits: MutableList<Habit>
        val habitGroups: MutableList<HabitGroup>
        val subHabits: MutableList<MutableList<Habit>>
        val idToPosition: HashMap<Long?, Int>
        val positionTypes: MutableList<Int>
        val positionIndices: MutableList<Int>
        val positionToHabit: HashMap<Int, Habit>
        val positionToHabitGroup: HashMap<Int, HabitGroup>
        val checkmarks: HashMap<Long?, IntArray>
        val scores: HashMap<Long?, Double>
        val notes: HashMap<Long?, Array<String>>

        @Synchronized
        fun copyCheckmarksFrom(oldData: CacheData) {
            val empty = IntArray(checkmarkCount)
            for (id in idToHabit.keys) {
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
            for (id in idToHabit.keys) {
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
            for (uuid in idToHabit.keys) {
                if (oldData.scores.containsKey(uuid)) {
                    scores[uuid] =
                        oldData.scores[uuid]!!
                } else {
                    scores[uuid] = 0.0
                }
            }
            for (uuid in idToHabitGroup.keys) {
                if (oldData.scores.containsKey(uuid)) {
                    scores[uuid] =
                        oldData.scores[uuid]!!
                } else {
                    scores[uuid] = 0.0
                }
            }
        }

        @Synchronized
        fun fetchHabits() {
            for (h in filteredHabits) {
                if (h.uuid == null) continue
                habits.add(h)
            }

            for (hgr in filteredHabitGroups) {
                if (hgr.uuid == null) continue
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
            idToPosition.clear()
            positionTypes.clear()
            positionIndices.clear()
            var position = 0
            for ((idx, h) in habits.withIndex()) {
                idToHabit[h.id] = h
                idToPosition[h.id] = position
                positionToHabit[position] = h
                positionTypes.add(STANDALONE_HABIT)
                positionIndices.add(idx)
                position++
            }

            for ((idx, hgr) in habitGroups.withIndex()) {
                idToHabitGroup[hgr.id] = hgr
                idToPosition[hgr.id] = position
                positionToHabitGroup[position] = hgr
                positionTypes.add(HABIT_GROUP)
                positionIndices.add(idx)
                val habitList = subHabits[idx]
                position++

                for ((hIdx, h) in habitList.withIndex()) {
                    idToHabit[h.id] = h
                    idToPosition[h.id] = position
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
                val parent = idToHabitGroup[habit.groupId] ?: return false
                val parentPosition = idToPosition[habit.groupId]!!
                val parentIndex = habitGroups.indexOf(parent)
                val nextGroup = habitGroups.getOrNull(parentIndex + 1)
                val nextGroupPosition = idToPosition[nextGroup?.id]
                return (position > parentPosition && position <= positionTypes.size) && (nextGroupPosition == null || position <= nextGroupPosition)
            }
        }

        @Synchronized
        fun isValidInsert(habitGroup: HabitGroup, position: Int): Boolean {
            return (position == positionTypes.size) || (positionTypes[position] == HABIT_GROUP)
        }

        @Synchronized
        fun incrementPositions(from: Int, to: Int) {
            for (pos in positionToHabit.keys.sortedDescending()) {
                if (pos in from..to) {
                    positionToHabit[pos + 1] = positionToHabit[pos]!!
                    positionToHabit.remove(pos)
                }
            }
            for (pos in positionToHabitGroup.keys.sortedDescending()) {
                if (pos in from..to) {
                    positionToHabitGroup[pos + 1] = positionToHabitGroup[pos]!!
                    positionToHabitGroup.remove(pos)
                }
            }
            for ((key, pos) in idToPosition.entries) {
                if (pos in from..to) {
                    idToPosition[key] = pos + 1
                }
            }
        }

        @Synchronized
        fun decrementPositions(fromPosition: Int, toPosition: Int) {
            for (pos in positionToHabit.keys.sorted()) {
                if (pos in fromPosition..toPosition) {
                    positionToHabit[pos - 1] = positionToHabit[pos]!!
                    positionToHabit.remove(pos)
                }
            }
            for (pos in positionToHabitGroup.keys.sorted()) {
                if (pos in fromPosition..toPosition) {
                    positionToHabitGroup[pos - 1] = positionToHabitGroup[pos]!!
                    positionToHabitGroup.remove(pos)
                }
            }
            for ((key, pos) in idToPosition.entries) {
                if (pos in fromPosition..toPosition) {
                    idToPosition[key] = pos - 1
                }
            }
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
            val checkedToPosition = if (toPosition > positionTypes.size) {
                logger.error("performMove: $toPosition for habit is strictly higher than ${habits.size}")
                positionTypes.size - 1
            } else {
                toPosition
            }

            val verifyPosition = if (fromPosition > checkedToPosition) checkedToPosition else checkedToPosition + 1
            if (!isValidInsert(habit, verifyPosition)) return

            if (type == STANDALONE_HABIT) {
                habits.removeAt(fromPosition)
                removeWithPos(fromPosition)
                if (fromPosition < checkedToPosition) {
                    decrementPositions(fromPosition + 1, checkedToPosition)
                } else {
                    incrementPositions(checkedToPosition, fromPosition - 1)
                }
                habits.add(checkedToPosition, habit)
                positionTypes.add(checkedToPosition, STANDALONE_HABIT)
                positionIndices.add(checkedToPosition, checkedToPosition)
            } else {
                val hgr = idToHabitGroup[habit.groupId]
                val hgrIdx = habitGroups.indexOf(hgr)
                val fromIdx = positionIndices[fromPosition]
                subHabits[hgrIdx].removeAt(fromIdx)
                removeWithPos(fromPosition)
                if (fromPosition < checkedToPosition) {
                    decrementPositions(fromPosition + 1, checkedToPosition)
                } else {
                    incrementPositions(checkedToPosition, fromPosition - 1)
                }
                val toIdx = checkedToPosition - idToPosition[hgr!!.id]!! - 1
                subHabits[hgrIdx].add(toIdx, habit)
                positionTypes.add(checkedToPosition, SUB_HABIT)
                positionIndices.add(checkedToPosition, toIdx)
            }

            positionToHabit[checkedToPosition] = habit
            idToPosition[habit.id] = checkedToPosition
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
            val toIdx = habitGroups.indexOf(positionToHabitGroup[toPosition]) - (if (fromPosition < toPosition) 1 else 0)

            habitGroups.removeAt(fromIdx)
            subHabits.removeAt(fromIdx)

            habitGroups.add(toIdx, habitGroup)
            subHabits.add(toIdx, habitList)

            rebuildPositions()
            listener.onItemMoved(fromPosition, toPosition)
        }

        fun removeWithID(id: Long?) {
            idToPosition.remove(id)
            idToHabit.remove(id)
            idToHabitGroup.remove(id)
            scores.remove(id)
            notes.remove(id)
            checkmarks.remove(id)
        }

        fun removeWithPos(pos: Int) {
            positionTypes.removeAt(pos)
            positionIndices.removeAt(pos)
            positionToHabit.remove(pos)
        }

        /**
         * Creates a new CacheData without any content.
         */
        init {
            habits = LinkedList()
            habitGroups = LinkedList()
            subHabits = LinkedList()
            positionTypes = LinkedList()
            positionIndices = LinkedList()
            idToPosition = HashMap()
            positionToHabit = HashMap()
            positionToHabitGroup = HashMap()
            checkmarks = HashMap()
            scores = HashMap()
            notes = HashMap()
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
            val today = getTodayWithOffset()
            val dateFrom = today.minus(checkmarkCount - 1)
            if (runner != null) runner!!.publishProgress(this, -1)
            for ((position, type) in newData.positionTypes.withIndex()) {
                if (isCancelled) return
                if (type == STANDALONE_HABIT || type == SUB_HABIT) {
                    val habit = newData.positionToHabit[position]!!
                    if (targetID != null && targetID != habit.id) continue
                    newData.scores[habit.id] = habit.scores[today].value
                    val list: MutableList<Int> = ArrayList()
                    val notes: MutableList<String> = ArrayList()
                    for ((_, value, note) in habit.computedEntries.getByInterval(dateFrom, today)) {
                        list.add(value)
                        notes.add(note)
                    }
                    val entries = list.toTypedArray()
                    newData.checkmarks[habit.id] = ArrayUtils.toPrimitive(entries)
                    newData.notes[habit.id] = notes.toTypedArray()
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
            if (habit.groupId == null) {
                data.habits.add(position, habit)
                data.positionTypes.add(position, STANDALONE_HABIT)
                data.positionIndices.add(position, position)
            } else {
                val hgrPos = data.idToPosition[habit.groupId]!!
                val hgrIdx = data.positionIndices[hgrPos]
                val habitIndex = newData.positionIndices[position]
                data.subHabits[hgrIdx].add(habitIndex, habit)
                data.positionTypes.add(position, SUB_HABIT)
                data.positionIndices.add(position, habitIndex)
            }
            data.incrementPositions(position, data.positionTypes.size - 1)
            data.positionToHabit[position] = habit
            data.idToPosition[id] = position
            data.idToHabit[id] = habit
            data.scores[id] = newData.scores[id]!!
            data.checkmarks[id] = newData.checkmarks[id]!!
            data.notes[id] = newData.notes[id]!!
            listener.onItemInserted(position)
        }

        @Synchronized
        private fun performInsert(habitGroup: HabitGroup, position: Int) {
            if (!data.isValidInsert(habitGroup, position)) return
            val id = habitGroup.id
            val prevIdx = newData.positionIndices[position]
            val habitList = newData.subHabits[prevIdx]
            val idx = if (data.positionIndices.size > position) {
                data.positionIndices[position]
            } else {
                data.habitGroups.size
            }

            data.habitGroups.add(idx, habitGroup)
            data.subHabits.add(prevIdx, habitList)
            data.scores[id] = newData.scores[id]!!
            for (h in habitList) {
                data.scores[h.id] = newData.scores[h.id]!!
                data.checkmarks[h.id] = newData.checkmarks[h.id]!!
                data.notes[h.id] = newData.notes[h.id]!!
            }
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
                if (!Arrays.equals(oldCheckmarks, newCheckmarks)) unchanged = false
                if (!Arrays.equals(oldNoteIndicators, newNoteIndicators)) unchanged = false
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
                val prevPosition = data.idToPosition[id] ?: -1
                val newPosition = if (type == STANDALONE_HABIT) {
                    currentPosition
                } else {
                    val hgrPos = data.idToPosition[habit.groupId]!!
                    val hgrIdx = data.positionIndices[hgrPos]
                    newData.subHabits[hgrIdx].indexOf(habit) + hgrPos + 1
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
                val prevPosition = data.idToPosition[id] ?: -1
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
            val before: Set<Long?> = (data.idToHabit.keys).union(data.idToHabitGroup.keys)
            val after: Set<Long?> = (newData.idToHabit.keys).union(newData.idToHabitGroup.keys)
            val removed: MutableSet<Long?> = TreeSet(before)
            removed.removeAll(after)
            for (id in removed.sortedBy { data.idToPosition[it] }) remove(id!!)
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
