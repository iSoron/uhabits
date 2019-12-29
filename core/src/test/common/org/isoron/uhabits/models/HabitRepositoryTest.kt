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

package org.isoron.uhabits.models

import org.isoron.*
import org.isoron.platform.gui.*
import kotlin.test.*

class HabitRepositoryTest() {
    @Test
    fun testCRUD() = asyncTest{
        val db = DependencyResolver.getDatabase()
        val original0 = Habit(id = 0,
                              name = "Wake up early",
                              description = "Did you wake up before 6am?",
                              frequency = Frequency(1, 1),
                              color = PaletteColor(3),
                              isArchived = false,
                              position = 0,
                              unit = "",
                              target = 0.0,
                              type = HabitType.BOOLEAN_HABIT)

        val original1 = Habit(id = 1,
                              name = "Exercise",
                              description = "Did you exercise for at least 20 minutes?",
                              frequency = Frequency(1, 2),
                              color = PaletteColor(4),
                              isArchived = false,
                              position = 1,
                              unit = "",
                              target = 0.0,
                              type = HabitType.BOOLEAN_HABIT)

        val original2 = Habit(id = 2,
                              name = "Learn Japanese",
                              description = "Did you study Japanese today?",
                              frequency = Frequency(1, 1),
                              color = PaletteColor(3),
                              isArchived = false,
                              position = 2,
                              unit = "",
                              target = 0.0,
                              type = HabitType.BOOLEAN_HABIT)

        val repository = HabitRepository(db)

        var habits = repository.findAll()
        assertEquals(0, repository.nextId())
        assertEquals(0, habits.size)

        repository.insert(original0)
        repository.insert(original1)
        repository.insert(original2)
        habits = repository.findAll()
        assertEquals(3, habits.size)
        assertEquals(original0, habits[0])
        assertEquals(original1, habits[1])
        assertEquals(original2, habits[2])

        assertEquals(3, repository.nextId())

        original0.description = "New description"
        repository.update(original0)
        habits = repository.findAll()
        assertEquals(original0, habits[0])

        repository.delete(original0)
        habits = repository.findAll()
        assertEquals(2, habits.size)
        assertEquals(original1, habits[1])
        assertEquals(original2, habits[2])
    }
}