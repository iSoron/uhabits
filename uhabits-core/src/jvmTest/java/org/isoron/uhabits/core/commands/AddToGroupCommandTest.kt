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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.ModelObservable
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddToGroupCommandTest : BaseUnitTest() {
    @Test
    fun testRun() {
        val habit = fixtures.createEmptyHabit()
        val listener: ModelObservable.Listener = mock()
        habit.observable.addListener(listener)
        val group = groupFixtures.createEmptyHabitGroup()
        habitList.add(habit)

        val command = AddToGroupCommand(habitList, group, listOf(habit))
        command.run()

        assertThat(habit.groupId, equalTo(group.id))
        assertThat(habit.group, equalTo(group))
        assertTrue(group.habitList.contains(habit))
        assertFalse(habitList.contains(habit))
        verify(listener).onModelChange()
    }

    @Test
    fun testMoveBetweenGroups() {
        val habit = fixtures.createEmptyHabit()
        val listener: ModelObservable.Listener = mock()
        habit.observable.addListener(listener)
        val group1 = groupFixtures.createEmptyHabitGroup()
        val group2 = groupFixtures.createEmptyHabitGroup()

        habit.group = group1
        habit.groupId = group1.id
        group1.habitList.add(habit)

        val command = AddToGroupCommand(habitList, group2, listOf(habit))
        command.run()

        assertThat(habit.groupId, equalTo(group2.id))
        assertThat(habit.group, equalTo(group2))
        assertTrue(group2.habitList.contains(habit))
        assertFalse(group1.habitList.contains(habit))
        verify(listener).onModelChange()
    }
}
