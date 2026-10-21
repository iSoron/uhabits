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

class ChangeHabitCategoryCommandTest : BaseUnitTest() {

    @Test
    fun testExecute() {
        val a = fixtures.createEmptyHabit("A", PaletteColor(1), 0)
        val b = fixtures.createEmptyHabit("B", PaletteColor(2), 1, "Zebra")
        val c = fixtures.createEmptyHabit("C", PaletteColor(3), 2, "Apple")
        habitList.add(a)
        habitList.add(b)
        habitList.add(c)

        assertEquals(a, habitList.getByPosition(0))
        assertEquals(c, habitList.getByPosition(1))
        assertEquals(b, habitList.getByPosition(2))

        ChangeHabitCategoryCommand(habitList, listOf(b), "Apple").run()

        assertEquals("Apple", b.category)
        assertEquals(a, habitList.getByPosition(0))
        assertEquals(b, habitList.getByPosition(1))
        assertEquals(c, habitList.getByPosition(2))

        ChangeHabitCategoryCommand(habitList, listOf(c), "").run()

        assertEquals("", c.category)
        assertEquals(a, habitList.getByPosition(0))
        assertEquals(c, habitList.getByPosition(1))
        assertEquals(b, habitList.getByPosition(2))
    }
}
