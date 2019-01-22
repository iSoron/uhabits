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

package org.isoron.uhabits

import org.isoron.uhabits.models.Color
import org.isoron.uhabits.models.Frequency
import org.isoron.uhabits.models.Habit
import org.isoron.uhabits.models.HabitType
import kotlin.random.Random

class Backend {
    var nextId = 1
    var habits = mutableListOf<Habit>()

    fun getHabitList(): Map<Int, Map<String, *>> {
        return habits.map { h ->
            h.id to mapOf("name" to h.name,
                          "color" to h.color.paletteIndex)
        }.toMap()
    }

    fun createHabit(name: String) {
        val c = (nextId / 4) % 5
        habits.add(Habit(nextId, name, "", Frequency(1, 1), Color(c),
                         false, habits.size, "", 0, HabitType.YES_NO_HABIT))
        nextId += 1
    }

    fun deleteHabit(id: Int) {
        val h = habits.find { h -> h.id == id }
        if (h != null) habits.remove(h)
    }

    fun updateHabit(id: Int, name: String) {
        val h = habits.find { h -> h.id == id }
        h?.name = name
    }
}
