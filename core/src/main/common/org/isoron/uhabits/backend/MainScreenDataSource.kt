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
import org.isoron.platform.time.*
import org.isoron.uhabits.models.*
import org.isoron.uhabits.models.Checkmark.Companion.UNCHECKED

class MainScreenDataSource(val preferences: Preferences,
                           val habits: MutableMap<Int, Habit>,
                           val checkmarks: MutableMap<Habit, CheckmarkList>,
                           val scores: MutableMap<Habit, ScoreList>) {

    val maxNumberOfButtons = 60
    private val today = LocalDate(2019, 3, 30) /* TODO */

    data class Data(val habits: List<Habit>,
                    val scores: Map<Habit, Score>,
                    val checkmarks: Map<Habit, List<Checkmark>>)

    val observable = Observable<Listener>()

    interface Listener {
        fun onDataChanged(newData: Data)
    }

    fun requestData() {
        var filtered = habits.values.toList()

        if (!preferences.showArchived) {
            filtered = filtered.filter { !it.isArchived }
        }

        val checkmarks = filtered.associate { habit ->
            val allValues = checkmarks.getValue(habit).getUntil(today)
            if (allValues.size <= maxNumberOfButtons) habit to allValues
            else habit to allValues.subList(0, maxNumberOfButtons)
        }

        if (!preferences.showCompleted) {
            filtered = filtered.filter { habit ->
                (habit.type == HabitType.BOOLEAN_HABIT && checkmarks.getValue(habit)[0].value == UNCHECKED) ||
                (habit.type == HabitType.NUMERICAL_HABIT && checkmarks.getValue(habit)[0].value * 1000 < habit.target)
            }
        }

        val scores = filtered.associate { habit ->
            habit to scores[habit]!!.getAt(today)
        }

        observable.notifyListeners { listener ->
            val data = Data(filtered, scores, checkmarks)
            listener.onDataChanged(data)
        }
    }
}