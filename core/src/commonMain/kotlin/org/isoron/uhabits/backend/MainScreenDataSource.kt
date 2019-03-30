/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.backend

import org.isoron.platform.concurrency.*
import org.isoron.platform.gui.*
import org.isoron.platform.time.*
import org.isoron.uhabits.models.*

class MainScreenDataSource(val habits: MutableMap<Int, Habit>,
                           val checkmarks: MutableMap<Habit, CheckmarkList>,
                           val taskRunner: TaskRunner) {

    private val today = LocalDate(2019, 3, 30)

    data class Data(val ids: List<Int>,
                    val scores: List<Double>,
                    val names: List<String>,
                    val colors: List<PaletteColor>,
                    val checkmarks: List<List<Int>>)

    private val listeners = mutableListOf<Listener>()

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    interface Listener {
        fun onDataChanged(newData: Data)
    }

    fun requestData() {
        taskRunner.runInBackground {
            val filteredHabits = habits.values.filter { h -> !h.isArchived }
            val ids = filteredHabits.map { it.id }
            val scores = filteredHabits.map { 0.0 }
            val names = filteredHabits.map { it.name }
            val colors = filteredHabits.map { it.color }
            val ck = filteredHabits.map { habit ->
                val allValues = checkmarks[habit]!!.getValuesUntil(today)
                if (allValues.size <= 7) allValues
                else allValues.subList(0, 7)
            }
            val data = Data(ids, scores, names, colors, ck)
            taskRunner.runInForeground {
                listeners.forEach { listener ->
                    listener.onDataChanged(data)
                }
            }
        }
    }
}