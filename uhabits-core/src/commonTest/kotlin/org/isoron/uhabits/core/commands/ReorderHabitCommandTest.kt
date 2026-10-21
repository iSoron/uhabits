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
package org.isoron.uhabits.core.commands

import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.PaletteColor
import kotlin.test.Test
import kotlin.test.assertEquals

class ReorderHabitCommandTest : BaseUnitTest() {

    @Test
    fun testExecute() {
        val a = fixtures.createEmptyHabit("A", PaletteColor(1), 0)
        val b = fixtures.createEmptyHabit("B", PaletteColor(2), 1)
        val c = fixtures.createEmptyHabit("C", PaletteColor(3), 2)
        val d = fixtures.createEmptyHabit("D", PaletteColor(4), 3)
        habitList.add(a)
        habitList.add(b)
        habitList.add(c)
        habitList.add(d)

        ReorderHabitCommand(habitList, d, ReorderTarget.Before(a), "").run()
        ReorderHabitCommand(habitList, b, ReorderTarget.After(c), "").run()
        ReorderHabitCommand(habitList, c, ReorderTarget.Before(a), "").run()

        assertEquals(d, habitList.getByPosition(0))
        assertEquals(c, habitList.getByPosition(1))
        assertEquals(a, habitList.getByPosition(2))
        assertEquals(b, habitList.getByPosition(3))
        assertEquals(0, d.position)
        assertEquals(1, c.position)
        assertEquals(2, a.position)
        assertEquals(3, b.position)
    }
}
