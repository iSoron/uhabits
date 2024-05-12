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

import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.HabitGroup
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class RefreshParentGroupCommandTest : BaseUnitTest() {
    @Test
    fun testRun() {
        val group: HabitGroup = mock()
        val habit = fixtures.createEmptyHabit()
        habit.group = group
        habit.groupId = 1L

        val command = RefreshParentGroupCommand(habit, habitGroupList)
        command.run()

        verify(group).recompute()
        verify(habitGroupList).resort()
    }

    @Test
    fun testRunNotSubHabit() {
        val habit = fixtures.createEmptyHabit()
        habit.group = null
        habit.groupId = null

        val command = RefreshParentGroupCommand(habit, habitGroupList)
        command.run()

        verify(habitGroupList, never()).resort()
    }
}
