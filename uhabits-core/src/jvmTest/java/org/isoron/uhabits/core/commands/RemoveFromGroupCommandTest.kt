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
import org.isoron.uhabits.core.models.ModelObservable
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemoveFromGroupCommandTest : BaseUnitTest() {
    @Test
    fun testRun() {
        val group = groupFixtures.createEmptyHabitGroup()
        val habit = fixtures.createEmptyHabit()
        val listener: ModelObservable.Listener = mock()
        habit.observable.addListener(listener)
        habit.group = group
        habit.groupId = group.id
        group.habitList.add(habit)

        val command = RemoveFromGroupCommand(habitList, listOf(habit))
        command.run()

        assertNull(habit.groupId)
        assertNull(habit.group)
        assertFalse(group.habitList.contains(habit))
        assertTrue(habitList.contains(habit))
        verify(listener).onModelChange()
    }
}
