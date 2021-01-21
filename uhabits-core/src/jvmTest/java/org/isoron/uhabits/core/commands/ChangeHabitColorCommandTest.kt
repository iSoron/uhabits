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
package org.isoron.uhabits.core.commands

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.junit.Before
import org.junit.Test
import java.util.LinkedList

class ChangeHabitColorCommandTest : BaseUnitTest() {
    private lateinit var command: ChangeHabitColorCommand
    private lateinit var selected: LinkedList<Habit>

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        selected = LinkedList()
        for (i in 0..2) {
            val habit = fixtures.createShortHabit()
            habit.color = PaletteColor(i + 1)
            selected.add(habit)
            habitList.add(habit)
        }
        command = ChangeHabitColorCommand(habitList, selected, PaletteColor(0))
    }

    @Test
    fun testExecute() {
        checkOriginalColors()
        command.run()
        checkNewColors()
    }

    private fun checkNewColors() {
        for (habit in selected) {
            assertThat(habit.color, equalTo(PaletteColor(0)))
        }
    }

    private fun checkOriginalColors() {
        var k = 0
        for (habit in selected)
            assertThat(habit.color, equalTo(PaletteColor(++k)))
    }
}
