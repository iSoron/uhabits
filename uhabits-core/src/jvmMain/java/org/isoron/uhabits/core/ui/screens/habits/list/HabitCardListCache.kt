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
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitList.Order
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.tasks.Task
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
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
    private val commandRunner: CommandRunner,
    taskRunner: TaskRunner,
    logging: Logging,
) : CommandRunner.Listener {

    private val logger = logging.getLogger("HabitCardListCache")

    private var checkmarkCount = 0
    private var currentFetchTask: Task? = null
    private var listener: Listener
    private val data: CacheData
    private var filteredHabits: HabitList
    private val taskRunner: TaskRunner

    @Synchronized
    fun cancelTasks() {
        currentFetchTask?.cancel()
    }

    @Synchronized
    fun getCheckmarks(habitId: Long): IntArray {
        return data.checkmarks[habitId]!!
    }

    @Synchronized
    fun getNotes(habitId: Long): Array<String> {
        return data.notes[habitId]!!
    }

    @Synchronized
    fun hasNoHabit(): Boolean {
        return allHabits.isEmpty
    }

    /**
     * Returns the habits that occupies a certain position on the list.
     *
     * @param position the position of the habit
     * @return the habit at given position or null if position is invalid
     */
    @Synchronized
    fun getHabitByPosition(position: Int): Habit? {
        return if (position < 0 || position >= data.habits.size) null else data.habits[position]
    }

    @get:Synchronized
    val habitCount: Int
        get() = data.habits.size

    @get:Synchronized
    @set:Synchronized
    var primaryOrder: Order
        get() = filteredHabits.primaryOrder
        set(order) {
            allHabits.primaryOrder = order
            filteredHabits.primaryOrder = order
            refreshAllHabits()
        }

    @get:Synchronized
    @set:Synchronized
    var secondaryOrder: Order
        get() = filteredHabits.secondaryOrder
        set(order) {
            allHabits.secondaryOrder = order
            filteredHabits.secondaryOrder = order
            refreshAllHabits()
        }

    @Synchronized
    fun getScore(habitId: Long): Double {
        return data.scores[habitId]!!
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
        val h = data.idToHabit[id] ?: return
        val position = data.habits.indexOf(h)
        data.habits.removeAt(position)
        data.idToHabit.remove(id)
        data.checkmarks.remove(id)
        data.notes.remove(id)
        data.scores.remove(id)
        listener.onItemRemoved(position)
    }

    @Synchronized
    fun reorder(from: Int, to: Int) {
        val fromHabit = data.habits[from]
        data.habits.removeAt(from)
        data.habits.add(to, fromHabit)
        listener.onItemMoved(from, to)
    }

    @Synchronized
    fun setCheckmarkCount(checkmarkCount: Int) {
        this.checkmarkCount = checkmarkCount
    }

    @Synchronized
    fun setFilter(matcher: HabitMatcher) {
        filteredHabits = allHabits.getFiltered(matcher)
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
        val habits: MutableList<Habit>
        val checkmarks: HashMap<Long?, IntArray>
        val scores: HashMap<Long?, Double>
        val notes: HashMap<Long?, Array<String>>

        @Synchronized
        fun copyCheckmarksFrom(oldData: CacheData) {
            val empty = IntArray(checkmarkCount)
            for (id in idToHabit.keys) {
                if (oldData.checkmarks.containsKey(id)) checkmarks[id] =
                    oldData.checkmarks[id]!! else checkmarks[id] = empty
            }
        }

        @Synchronized
        fun copyNoteIndicatorsFrom(oldData: CacheData) {
            val empty = (0..checkmarkCount).map { "" }.toTypedArray()
            for (id in idToHabit.keys) {
                if (oldData.notes.containsKey(id)) notes[id] =
                    oldData.notes[id]!! else notes[id] = empty
            }
        }

        @Synchronized
        fun copyScoresFrom(oldData: CacheData) {
            for (id in idToHabit.keys) {
                if (oldData.scores.containsKey(id)) scores[id] =
                    oldData.scores[id]!! else scores[id] = 0.0
            }
        }

        @Synchronized
        fun fetchHabits() {
            for (h in filteredHabits) {
                if (h.id == null) continue
                habits.add(h)
                idToHabit[h.id] = h
            }
        }

        /**
         * Creates a new CacheData without any content.
         */
        init {
            habits = LinkedList()
            checkmarks = HashMap()
            scores = HashMap()
            notes = HashMap()
        }
    }

    private inner class RefreshTask : Task {
        private val newData: CacheData
        private val targetId: Long?
        private var isCancelled = false
        private var runner: TaskRunner? = null

        constructor() {
            newData = CacheData()
            targetId = null
            isCancelled = false
        }

        constructor(targetId: Long) {
            newData = CacheData()
            this.targetId = targetId
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
                if (targetId != null && targetId != habit.id) continue
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
            val id = habit.id
            data.habits.add(position, habit)
            data.idToHabit[id] = habit
            data.scores[id] = newData.scores[id]!!
            data.checkmarks[id] = newData.checkmarks[id]!!
            data.notes[id] = newData.notes[id]!!
            listener.onItemInserted(position)
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
                logger.error("performMove: $toPosition is strictly higher than ${data.habits.size}")
                data.habits.size
            } else {
                toPosition
            }

            data.habits.add(checkedToPosition, habit)
            listener.onItemMoved(fromPosition, checkedToPosition)
        }

        @Synchronized
        private fun performUpdate(id: Long, position: Int) {
            val oldScore = data.scores[id]!!
            val oldCheckmarks = data.checkmarks[id]
            val oldNoteIndicators = data.notes[id]
            val newScore = newData.scores[id]!!
            val newCheckmarks = newData.checkmarks[id]!!
            val newNoteIndicators = newData.notes[id]!!
            var unchanged = true
            if (oldScore != newScore) unchanged = false
            if (!Arrays.equals(oldCheckmarks, newCheckmarks)) unchanged = false
            if (!Arrays.equals(oldNoteIndicators, newNoteIndicators)) unchanged = false
            if (unchanged) return
            data.scores[id] = newScore
            data.checkmarks[id] = newCheckmarks
            data.notes[id] = newNoteIndicators
            listener.onItemChanged(position)
        }

        @Synchronized
        private fun processPosition(currentPosition: Int) {
            val habit = newData.habits[currentPosition]
            val id = habit.id
            val prevPosition = data.habits.indexOf(habit)
            if (prevPosition < 0) {
                performInsert(habit, currentPosition)
            } else {
                if (prevPosition != currentPosition) performMove(
                    habit,
                    prevPosition,
                    currentPosition
                )
                if (id == null) throw NullPointerException()
                performUpdate(id, currentPosition)
            }
        }

        @Synchronized
        private fun processRemovedHabits() {
            val before: Set<Long?> = data.idToHabit.keys
            val after: Set<Long?> = newData.idToHabit.keys
            val removed: MutableSet<Long?> = TreeSet(before)
            removed.removeAll(after)
            for (id in removed) remove(id!!)
        }
    }

    init {
        filteredHabits = allHabits
        this.taskRunner = taskRunner
        listener = object : Listener {}
        data = CacheData()
    }
}
