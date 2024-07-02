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
    private val allHabits: HabitList,
    private val allHabitGroups: HabitGroupList,
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
        return allHabits.isEmpty
    }

    @Synchronized
    fun hasNoHabitGroup(): Boolean {
        return allHabitGroups.isEmpty
    }

    /**
     * Returns the habits that occupies a certain position on the list.
     *
     * @param position the position of the list of habits and groups
     * @return the habit at given position or null if position is invalid
     */
    @Synchronized
    fun getHabitByPosition(position: Int): Habit? {
        return if (position < 0 || position >= data.habits.size) {
            null
        } else {
            data.habits[position]
        }
    }

    /**
     * Returns the habit groups that occupies a certain position on the list.
     *
     * @param position the position of the list of habits and groups
     * @return the habit group at given position or null if position is invalid
     */
    @Synchronized
    fun getHabitGroupByPosition(position: Int): HabitGroup? {
        return if (position < data.habits.size || position >= data.habits.size + data.habitGroups.size) {
            null
        } else {
            data.habitGroups[position - data.habits.size]
        }
    }

    @get:Synchronized
    val itemCount: Int
        get() = habitCount + habitGroupCount

    @get:Synchronized
    val habitCount: Int
        get() = data.habits.size

    @get:Synchronized
    val habitGroupCount: Int
        get() = data.habitGroups.size

    @get:Synchronized
    @set:Synchronized
    var primaryOrder: Order
        get() = filteredHabits.primaryOrder
        set(order) {
            allHabits.primaryOrder = order
            filteredHabits.primaryOrder = order
            allHabitGroups.primaryOrder = order
            filteredHabitGroups.primaryOrder = order
            refreshAllHabits()
        }

    @get:Synchronized
    @set:Synchronized
    var secondaryOrder: Order
        get() = filteredHabits.secondaryOrder
        set(order) {
            allHabits.secondaryOrder = order
            filteredHabits.secondaryOrder = order
            allHabitGroups.secondaryOrder = order
            filteredHabitGroups.secondaryOrder = order
            refreshAllHabits()
        }

    @Synchronized
    fun getScore(habitUUID: String): Double {
        return data.scores[habitUUID]!!
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
        val h = data.uuidToHabit[uuid]
        if (h != null) {
            val position = data.habits.indexOf(h)
            data.habits.removeAt(position)
            data.uuidToHabit.remove(uuid)
            data.checkmarks.remove(uuid)
            data.notes.remove(uuid)
            data.scores.remove(uuid)
            listener.onItemRemoved(position)
        } else {
            val hgr = data.uuidToHabitGroup[uuid]
            if (hgr != null) {
                val position = data.habitGroups.indexOf(hgr)
                data.habitGroups.removeAt(position)
                data.uuidToHabitGroup.remove(uuid)
                listener.onItemRemoved(position + data.habits.size)
            }
        }
    }

    @Synchronized
    fun reorder(from: Int, to: Int) {
        if (data.habits.size in (from + 1)..to || data.habits.size in (to + 1)..from) {
            logger.error("reorder: from and to are in different sections")
            return
        }
        if (from < data.habits.size) {
            val fromHabit = data.habits[from]
            data.habits.removeAt(from)
            data.habits.add(to, fromHabit)
        } else {
            val fromHabitGroup = data.habitGroups[from]
            data.habitGroups.removeAt(from - data.habits.size)
            data.habitGroups.add(to - data.habits.size, fromHabitGroup)
        }
        listener.onItemMoved(from, to)
    }

    @Synchronized
    fun setCheckmarkCount(checkmarkCount: Int) {
        this.checkmarkCount = checkmarkCount
    }

    @Synchronized
    fun setFilter(matcher: HabitMatcher) {
        filteredHabits = allHabits.getFiltered(matcher)
        filteredHabitGroups = allHabitGroups.getFiltered(matcher)
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
                uuidToHabit[h.uuid] = h
            }

            for (hgr in filteredHabitGroups) {
                if (hgr.uuid == null) continue
                habitGroups.add(hgr)
                uuidToHabitGroup[hgr.uuid] = hgr
            }
        }

        /**
         * Creates a new CacheData without any content.
         */
        init {
            habits = LinkedList()
            habitGroups = LinkedList()
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
            newData.copyScoresFrom(data)
            newData.copyCheckmarksFrom(data)
            newData.copyNoteIndicatorsFrom(data)
            val today = getTodayWithOffset()
            val dateFrom = today.minus(checkmarkCount - 1)
            if (runner != null) runner!!.publishProgress(this, -1)
            for (position in newData.habits.indices) {
                if (isCancelled) return
                val habit = newData.habits[position]
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
            }

            for (position in newData.habitGroups.indices) {
                if (isCancelled) return
                val hgr = newData.habitGroups[position]
                if (targetUUID != null && targetUUID != hgr.uuid) continue
                newData.scores[hgr.uuid] = hgr.scores[today].value
                runner!!.publishProgress(this, position + newData.habits.size)
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
            val uuid = habit.uuid
            data.habits.add(position, habit)
            data.uuidToHabit[uuid] = habit
            data.scores[uuid] = newData.scores[uuid]!!
            data.checkmarks[uuid] = newData.checkmarks[uuid]!!
            data.notes[uuid] = newData.notes[uuid]!!
            listener.onItemInserted(position)
        }

        @Synchronized
        private fun performInsert(habitGroup: HabitGroup, position: Int) {
            val newPosition = if (position < data.habits.size) {
                data.habits.size
            } else {
                position
            }
            val uuid = habitGroup.uuid
            data.habitGroups.add(newPosition - data.habits.size, habitGroup)
            data.uuidToHabitGroup[uuid] = habitGroup
            data.scores[uuid] = newData.scores[uuid]!!
            listener.onItemInserted(newPosition)
        }

        @Synchronized
        private fun performMove(
            habit: Habit,
            fromPosition: Int,
            toPosition: Int
        ) {
            data.habits.removeAt(fromPosition)

            // Workaround for https://github.com/iSoron/uhabits/issues/968
            val checkedToPosition = if (toPosition > data.habits.size) {
                logger.error("performMove: $toPosition for habit is strictly higher than ${data.habits.size}")
                data.habits.size
            } else {
                toPosition
            }

            data.habits.add(checkedToPosition, habit)
            listener.onItemMoved(fromPosition, checkedToPosition)
        }

        private fun performMove(
            habitGroup: HabitGroup,
            fromPosition: Int,
            toPosition: Int
        ) {
            if (fromPosition < data.habits.size || fromPosition > data.habits.size + data.habitGroups.size) {
                logger.error("performMove: $fromPosition for habit group is out of bounds")
                return
            }
            data.habitGroups.removeAt(fromPosition - data.habits.size)

            // Workaround for https://github.com/iSoron/uhabits/issues/968
            val checkedToPosition = if (toPosition < data.habits.size) {
                logger.error("performMove: $toPosition for habit group is strictly lower than ${data.habits.size}")
                data.habits.size
            } else if (toPosition > data.habits.size + data.habitGroups.size) {
                logger.error("performMove: $toPosition for habit group is strictly higher than ${data.habits.size + data.habitGroups.size}")
                data.habits.size + data.habitGroups.size
            } else {
                toPosition
            }

            data.habitGroups.add(checkedToPosition - data.habits.size, habitGroup)
            listener.onItemMoved(fromPosition, checkedToPosition)
        }

        @Synchronized
        private fun performUpdate(uuid: String, position: Int) {
            var unchanged = true
            val oldScore = data.scores[uuid]!!
            val newScore = newData.scores[uuid]!!
            if (oldScore != newScore) unchanged = false

            if (position < data.habits.size) {
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
            if (currentPosition < newData.habits.size) {
                val habit = newData.habits[currentPosition]
                val uuid = habit.uuid
                val prevPosition = data.habits.indexOf(habit)
                if (prevPosition < 0) {
                    performInsert(habit, currentPosition)
                } else {
                    if (prevPosition != currentPosition) {
                        performMove(
                            habit,
                            prevPosition,
                            currentPosition
                        )
                    }
                    if (uuid == null) throw NullPointerException()
                    performUpdate(uuid, currentPosition)
                }
            } else {
                val habitGroup = newData.habitGroups[currentPosition - data.habits.size]
                val uuid = habitGroup.uuid
                val prevPosition = data.habitGroups.indexOf(habitGroup) + data.habits.size
                if (prevPosition < data.habits.size) {
                    performInsert(habitGroup, currentPosition)
                } else {
                    if (prevPosition != currentPosition) {
                        performMove(
                            habitGroup,
                            prevPosition,
                            currentPosition
                        )
                    }
                    if (uuid == null) throw NullPointerException()
                    performUpdate(uuid, currentPosition)
                }
            }
        }

        @Synchronized
        private fun processRemovedHabits() {
            val before: Set<String?> = data.uuidToHabit.keys
            val after: Set<String?> = newData.uuidToHabit.keys
            val removed: MutableSet<String?> = TreeSet(before)
            removed.removeAll(after)
            for (uuid in removed) remove(uuid!!)
            processRemovedHabitGroups()
        }

        @Synchronized
        private fun processRemovedHabitGroups() {
            val before: Set<String?> = data.uuidToHabitGroup.keys
            val after: Set<String?> = newData.uuidToHabitGroup.keys
            val removed: MutableSet<String?> = TreeSet(before)
            removed.removeAll(after)
            for (uuid in removed) remove(uuid!!)
        }
    }

    init {
        filteredHabits = allHabits
        filteredHabitGroups = allHabitGroups
        this.taskRunner = taskRunner
        listener = object : Listener {}
        data = CacheData()
    }
}
