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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackendTest {
    @Test
    fun testBackend() {
        val backend = Backend()
        assertEquals(backend.getHabitList().size, 0)

        backend.createHabit("Brush teeth")
        backend.createHabit("Wake up early")

        var result = backend.getHabitList()
        assertEquals(result.size, 2)
        assertEquals(result[1]!!["name"], "Brush teeth")
        assertEquals(result[2]!!["name"], "Wake up early")

        backend.deleteHabit(1)
        result = backend.getHabitList()
        assertEquals(result.size, 1)
        assertTrue(2 in result.keys)

        backend.updateHabit(2, "Wake up late")
        result = backend.getHabitList()
        assertEquals(result[2]!!["name"], "Wake up late")
    }
}