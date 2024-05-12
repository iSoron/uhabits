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
package org.isoron.uhabits.core.models

import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HabitMatcherTest : BaseUnitTest() {
    @Test
    fun testMatchesArchived() {
        val habit = fixtures.createEmptyHabit()
        habit.isArchived = true

        assertFalse(HabitMatcher(isArchivedAllowed = false).matches(habit))
        assertTrue(HabitMatcher(isArchivedAllowed = true).matches(habit))
    }

    @Test
    fun testMatchesReminder() {
        val habit = fixtures.createEmptyHabit()
        habit.reminder = null
        assertFalse(HabitMatcher(isReminderRequired = true).matches(habit))

        habit.reminder = Reminder(8, 0, WeekdayList.EVERY_DAY)
        assertTrue(HabitMatcher(isReminderRequired = true).matches(habit))
    }

    @Test
    fun testMatchesCompleted() {
        val habit = fixtures.createEmptyHabit()
        val today = Timestamp(FIXED_LOCAL_TIME)
        habit.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        habit.recompute()

        assertFalse(HabitMatcher(isCompletedAllowed = false).matches(habit))
        assertTrue(HabitMatcher(isCompletedAllowed = true).matches(habit))
    }

    @Test
    fun testMatchesCollapsed() {
        val habit = fixtures.createEmptyHabit()
        habit.collapsed = true
        assertFalse(HabitMatcher().matches(habit))

        habit.collapsed = false
        assertTrue(HabitMatcher().matches(habit))
    }
}
