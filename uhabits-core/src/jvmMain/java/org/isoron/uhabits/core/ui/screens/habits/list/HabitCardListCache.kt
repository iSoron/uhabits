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
    fun getCheckmarks(habitUUID: String): IntArray {
        return data.checkmarks[habitUUID]!!
    }

    @Synchronized
    fun getNotes(habitUUID: String): Array<String> {
        return data.notes[habitUUID]!!
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
    fun getUUIDByPosition(position: Int): String? {
        return if (data.positionTypes[position] == STANDALONE_HABIT || data.positionTypes[position] == SUB_HABIT) {
            data.positionToHabit[position]!!.uuid
        } else {
            data.positionToHabitGroup[position]!!.uuid
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
    fun getScore(uuid: String): Double {
        return data.scores[uuid]!!
    }

    @Synchronized
    fun onAttached() {
        refreshAllHabits()
        commandRunner.addListener(this)
    }

    @Synchronized
    override fun onCommandFinished(command: Command) {
        if (command is CreateRepetitionCommand) {
            command.habit.uuid?.let { refreshHabit(it) }
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
    fun refreshHabit(uuid: String) {
        taskRunner.execute(RefreshTask(uuid))
    }

    @Synchronized
    fun remove(uuid: String) {
        val position = data.uuidToPosition[uuid] ?: return
        val type = data.positionTypes[position]
        if (type == STANDALONE_HABIT) {
            val h = data.uuidToHabit[uuid]
            if (h != null) {
                val pos = data.habits.indexOf(h)
                data.habits.removeAt(pos)
                data.removeWithUUID(uuid)
                data.positionTypes.removeAt(pos)
                data.decrementPositions(pos + 1, data.positionTypes.size)
                listener.onItemRemoved(pos)
            }
        } else if (type == SUB_HABIT) {
            val h = data.uuidToHabit[uuid]
            if (h != null) {
                val pos = data.uuidToPosition[uuid]!!
                val hgrUUID = h.parentUUID
                val hgr = data.uuidToHabitGroup[hgrUUID]
                val hgrIdx = data.habitGroups.indexOf(hgr)
                data.subHabits[hgrIdx].remove(h)
                data.removeWithUUID(uuid)
                data.positionTypes.removeAt(pos)
                data.decrementPositions(pos + 1, data.positionTypes.size)
                listener.onItemRemoved(pos)
            }
        } else if (type == HABIT_GROUP) {
            val hgr = data.uuidToHabitGroup[uuid]
            if (hgr != null) {
                val pos = data.uuidToPosition[uuid]!!
                val hgrIdx = data.habitGroups.indexOf(hgr)

                for (habit in data.subHabits[hgrIdx].reversed()) {
                    val habitPos = data.uuidToPosition[habit.uuid]!!
                    data.removeWithUUID(habit.uuid)
                    listener.onItemRemoved(habitPos)
                }
                data.subHabits.removeAt(hgrIdx)
                data.habitGroups.removeAt(hgrIdx)
                data.removeWithUUID(hgr.uuid)
                data.rebuildPositions()
                listener.onItemRemoved(pos)
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
        val uuidToHabit: HashMap<String?, Habit> = HashMap()
        val uuidToHabitGroup: HashMap<String?, HabitGroup> = HashMap()
        val habits: MutableList<Habit>
        val habitGroups: MutableList<HabitGroup>
        val subHabits: MutableList<MutableList<Habit>>
        val uuidToPosition: HashMap<String?, Int>
        val positionTypes: MutableList<Int>
        val positionToHabit: HashMap<Int, Habit>
        val positionToHabitGroup: HashMap<Int, HabitGroup>
        val checkmarks: HashMap<String?, IntArray>
        val scores: HashMap<String?, Double>
        val notes: HashMap<String?, Array<String>>

        @Synchronized
        fun copyCheckmarksFrom(oldData: CacheData) {
            val empty = IntArray(checkmarkCount)
            for (uuid in uuidToHabit.keys) {
                if (oldData.checkmarks.containsKey(uuid)) {
                    checkmarks[uuid] =
                        oldData.checkmarks[uuid]!!
                } else {
                    checkmarks[uuid] = empty
                }
            }
        }

        @Synchronized
        fun copyNoteIndicatorsFrom(oldData: CacheData) {
            val empty = (0..checkmarkCount).map { "" }.toTypedArray()
            for (uuid in uuidToHabit.keys) {
                if (oldData.notes.containsKey(uuid)) {
                    notes[uuid] =
                        oldData.notes[uuid]!!
                } else {
                    notes[uuid] = empty
                }
            }
        }

        @Synchronized
        fun copyScoresFrom(oldData: CacheData) {
            for (uuid in uuidToHabit.keys) {
                if (oldData.scores.containsKey(uuid)) {
                    scores[uuid] =
                        oldData.scores[uuid]!!
                } else {
                    scores[uuid] = 0.0
                }
            }
            for (uuid in uuidToHabitGroup.keys) {
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
            uuidToPosition.clear()
            positionTypes.clear()
            var position = 0
            for (h in habits) {
                uuidToHabit[h.uuid] = h
                uuidToPosition[h.uuid] = position
                positionToHabit[position] = h
                positionTypes.add(STANDALONE_HABIT)
                position++
            }

            for ((idx, hgr) in habitGroups.withIndex()) {
                uuidToHabitGroup[hgr.uuid] = hgr
                uuidToPosition[hgr.uuid] = position
                positionToHabitGroup[position] = hgr
                positionTypes.add(HABIT_GROUP)
                val habitList = subHabits[idx]
                position++

                for (h in habitList) {
                    uuidToHabit[h.uuid] = h
                    uuidToPosition[h.uuid] = position
                    positionToHabit[position] = h
                    positionTypes.add(SUB_HABIT)
                    position++
                }
            }
        }

        @Synchronized
        fun isValidInsert(habit: Habit, position: Int): Boolean {
            if (habit.parentUUID == null) {
                return position <= habits.size
            } else {
                val parent = uuidToHabitGroup[habit.parentUUID] ?: return false
                val parentPosition = uuidToPosition[habit.parentUUID]!!
                val parentIndex = habitGroups.indexOf(parent)
                val nextGroup = habitGroups.getOrNull(parentIndex + 1)
                val nextGroupPosition = uuidToPosition[nextGroup?.uuid]
                return (position > parentPosition && position <= positionTypes.size) && (nextGroupPosition == null || position <= nextGroupPosition)
            }
        }

        @Synchronized
        fun isValidInsert(habitGroup: HabitGroup, position: Int): Boolean {
            return (position == positionTypes.size) || (positionTypes[position] == HABIT_GROUP)
        }

        @Synchronized
        fun incrementPositions(from: Int, to: Int) {
            for (pos in positionToHabit.keys.sortedByDescending { it }) {
                if (pos in from..to) {
                    positionToHabit[pos + 1] = positionToHabit[pos]!!
                    positionToHabit.remove(pos)
                }
            }
            for (pos in positionToHabitGroup.keys.sortedByDescending { it }) {
                if (pos in from..to) {
                    positionToHabitGroup[pos + 1] = positionToHabitGroup[pos]!!
                    positionToHabitGroup.remove(pos)
                }
            }
            for ((key, pos) in uuidToPosition.entries) {
                if (pos in from..to) {
                    uuidToPosition[key] = pos + 1
                }
            }
        }

        @Synchronized
        fun decrementPositions(fromPosition: Int, toPosition: Int) {
            for (pos in positionToHabit.keys.sortedBy { it }) {
                if (pos in fromPosition..toPosition) {
                    positionToHabit[pos - 1] = positionToHabit[pos]!!
                    positionToHabit.remove(pos)
                }
            }
            for (pos in positionToHabitGroup.keys.sortedBy { it }) {
                if (pos in fromPosition..toPosition) {
                    positionToHabitGroup[pos - 1] = positionToHabitGroup[pos]!!
                    positionToHabitGroup.remove(pos)
                }
            }
            for ((key, pos) in uuidToPosition.entries) {
                if (pos in fromPosition..toPosition) {
                    uuidToPosition[key] = pos - 1
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
                positionTypes.removeAt(fromPosition)
                if (fromPosition < checkedToPosition) {
                    decrementPositions(fromPosition + 1, checkedToPosition)
                } else {
                    incrementPositions(checkedToPosition, fromPosition - 1)
                }
                habits.add(checkedToPosition, habit)
                positionTypes.add(checkedToPosition, STANDALONE_HABIT)
            } else {
                val hgr = uuidToHabitGroup[habit.parentUUID]
                val hgrIdx = habitGroups.indexOf(hgr)
                val fromIdx = subHabits[hgrIdx].indexOf(habit)
                subHabits[hgrIdx].removeAt(fromIdx)
                positionTypes.removeAt(fromPosition)
                if (fromPosition < checkedToPosition) {
                    decrementPositions(fromPosition + 1, checkedToPosition)
                } else {
                    incrementPositions(checkedToPosition, fromPosition - 1)
                }
                val toIdx = checkedToPosition - uuidToPosition[hgr!!.uuid]!! - 1
                subHabits[hgrIdx].add(toIdx, habit)
                positionTypes.add(checkedToPosition, SUB_HABIT)
            }

            positionToHabit[checkedToPosition] = habit
            uuidToPosition[habit.uuid] = checkedToPosition
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
            val fromIdx = habitGroups.indexOf(habitGroup)
            val habitList = subHabits[fromIdx]
            val toIdx = habitGroups.indexOf(positionToHabitGroup[toPosition]) - (if (fromPosition < toPosition) 1 else 0)

            habitGroups.removeAt(fromIdx)
            subHabits.removeAt(fromIdx)

            habitGroups.add(toIdx, habitGroup)
            subHabits.add(toIdx, habitList)

            rebuildPositions()
            listener.onItemMoved(fromPosition, toPosition)
        }

        fun removeWithUUID(uuid: String?) {
            uuidToPosition.remove(uuid)
            uuidToHabit.remove(uuid)
            uuidToHabitGroup.remove(uuid)
            scores.remove(uuid)
            notes.remove(uuid)
            checkmarks.remove(uuid)
        }

        /**
         * Creates a new CacheData without any content.
         */
        init {
            habits = LinkedList()
            habitGroups = LinkedList()
            subHabits = LinkedList()
            positionTypes = LinkedList()
            uuidToPosition = HashMap()
            positionToHabit = HashMap()
            positionToHabitGroup = HashMap()
            checkmarks = HashMap()
            scores = HashMap()
            notes = HashMap()
        }
    }

    private inner class RefreshTask : Task {
        private val newData: CacheData
        private val targetUUID: String?
        private var isCancelled = false
        private var runner: TaskRunner? = null

        constructor() {
            newData = CacheData()
            targetUUID = null
            isCancelled = false
        }

        constructor(targetUUID: String) {
            newData = CacheData()
            this.targetUUID = targetUUID
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
                    if (targetUUID != null && targetUUID != habit.uuid) continue
                    newData.scores[habit.uuid] = habit.scores[today].value
                    val list: MutableList<Int> = ArrayList()
                    val notes: MutableList<String> = ArrayList()
                    for ((_, value, note) in habit.computedEntries.getByInterval(dateFrom, today)) {
                        list.add(value)
                        notes.add(note)
                    }
                    val entries = list.toTypedArray()
                    newData.checkmarks[habit.uuid] = ArrayUtils.toPrimitive(entries)
                    newData.notes[habit.uuid] = notes.toTypedArray()
                    runner!!.publishProgress(this, position)
                } else if (type == HABIT_GROUP) {
                    val habitGroup = newData.positionToHabitGroup[position]!!
                    if (targetUUID != null && targetUUID != habitGroup.uuid) continue
                    newData.scores[habitGroup.uuid] = habitGroup.scores[today].value
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
            val uuid = habit.uuid
            if (habit.parentUUID == null) {
                data.habits.add(position, habit)
                data.positionTypes.add(position, STANDALONE_HABIT)
            } else {
                val hgr = data.uuidToHabitGroup[habit.parentUUID]
                val hgrIdx = data.habitGroups.indexOf(hgr)
                val habitIndex = newData.subHabits[hgrIdx].indexOf(habit)
                data.subHabits[hgrIdx].add(habitIndex, habit)
                data.positionTypes.add(position, SUB_HABIT)
            }
            data.incrementPositions(position, data.positionTypes.size - 1)
            data.positionToHabit[position] = habit
            data.uuidToPosition[uuid] = position
            data.uuidToHabit[uuid] = habit
            data.scores[uuid] = newData.scores[uuid]!!
            data.checkmarks[uuid] = newData.checkmarks[uuid]!!
            data.notes[uuid] = newData.notes[uuid]!!
            listener.onItemInserted(position)
        }

        @Synchronized
        private fun performInsert(habitGroup: HabitGroup, position: Int) {
            if (!data.isValidInsert(habitGroup, position)) return
            val uuid = habitGroup.uuid
            val prevIdx = newData.habitGroups.indexOf(habitGroup)
            val habitList = newData.subHabits[prevIdx]
            var idx = data.habitGroups.indexOf(data.positionToHabitGroup[position])
            if (idx < 0) idx = data.habitGroups.size

            data.habitGroups.add(idx, habitGroup)
            data.subHabits.add(prevIdx, habitList)
            data.scores[uuid] = newData.scores[uuid]!!
            for (h in habitList) {
                data.scores[h.uuid] = newData.scores[h.uuid]!!
                data.checkmarks[h.uuid] = newData.checkmarks[h.uuid]!!
                data.notes[h.uuid] = newData.notes[h.uuid]!!
            }
            data.rebuildPositions()
            listener.onItemInserted(position)
        }

        @Synchronized
        private fun performUpdate(uuid: String, position: Int) {
            var unchanged = true
            val oldScore = data.scores[uuid]!!
            val newScore = newData.scores[uuid]!!
            if (oldScore != newScore) unchanged = false

            if (data.positionTypes[position] != HABIT_GROUP) {
                val oldCheckmarks = data.checkmarks[uuid]
                val newCheckmarks = newData.checkmarks[uuid]!!
                val oldNoteIndicators = data.notes[uuid]
                val newNoteIndicators = newData.notes[uuid]!!
                if (!Arrays.equals(oldCheckmarks, newCheckmarks)) unchanged = false
                if (!Arrays.equals(oldNoteIndicators, newNoteIndicators)) unchanged = false
                if (unchanged) return
                data.checkmarks[uuid] = newCheckmarks
                data.notes[uuid] = newNoteIndicators
            }

            if (unchanged) return
            data.scores[uuid] = newScore
            listener.onItemChanged(position)
        }

        @Synchronized
        private fun processPosition(currentPosition: Int) {
            val type = newData.positionTypes[currentPosition]

            if (type == STANDALONE_HABIT || type == SUB_HABIT) {
                val habit = newData.positionToHabit[currentPosition]!!
                val uuid = habit.uuid ?: throw NullPointerException()
                val prevPosition = data.uuidToPosition[uuid] ?: -1
                val newPosition = if (type == STANDALONE_HABIT) {
                    currentPosition
                } else {
                    val hgr = data.uuidToHabitGroup[habit.parentUUID]
                    val hgrIdx = data.habitGroups.indexOf(hgr)
                    newData.subHabits[hgrIdx].indexOf(habit) + data.uuidToPosition[hgr!!.uuid]!! + 1
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
                    performUpdate(uuid, currentPosition)
                }
            } else if (type == HABIT_GROUP) {
                val habitGroup = newData.positionToHabitGroup[currentPosition]!!
                val uuid = habitGroup.uuid ?: throw NullPointerException()
                val prevPosition = data.uuidToPosition[uuid] ?: -1
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
                    performUpdate(uuid, currentPosition)
                }
            }
        }

        @Synchronized
        private fun processRemovedHabits() {
            val before: Set<String?> = (data.uuidToHabit.keys).union(data.uuidToHabitGroup.keys)
            val after: Set<String?> = (newData.uuidToHabit.keys).union(newData.uuidToHabitGroup.keys)
            val removed: MutableSet<String?> = TreeSet(before)
            removed.removeAll(after)
            for (uuid in removed.sortedBy { uuid -> data.uuidToPosition[uuid] }) remove(uuid!!)
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
