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

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsEqual.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class HabitTest : BaseUnitTest() {
    @get:Rule
    val exception = ExpectedException.none()!!

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun testUuidGeneration() {
        val uuid1 = modelFactory.buildHabit().uuid!!
        val uuid2 = modelFactory.buildHabit().uuid!!
        assertNotEquals(uuid1, uuid2)
    }

    @Test
    fun test_copyAttributes() {
        val model = modelFactory.buildHabit()
        model.isArchived = true
        model.color = PaletteColor(0)
        model.frequency = Frequency(10, 20)
        model.reminder = Reminder(8, 30, WeekdayList(1))
        val habit = modelFactory.buildHabit()
        habit.copyFrom(model)
        assertTrue(habit.isArchived == model.isArchived)
        assertThat(habit.isArchived, `is`(model.isArchived))
        assertThat(habit.color, `is`(model.color))
        assertThat(habit.frequency, equalTo(model.frequency))
        assertThat(habit.reminder, equalTo(model.reminder))
    }

    @Test
    fun test_hasReminder() {
        val h = modelFactory.buildHabit()
        assertThat(h.hasReminder(), `is`(false))
        h.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        assertThat(h.hasReminder(), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun test_isCompleted() {
        val h = modelFactory.buildHabit()
        assertFalse(h.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), Entry.YES_MANUAL))
        h.recompute()
        assertTrue(h.isCompletedToday())
    }

    @Test
    @Throws(Exception::class)
    fun test_isEntered() {
        val h = modelFactory.buildHabit()
        assertFalse(h.isEnteredToday())
        h.originalEntries.add(Entry(getToday(), Entry.NO))
        h.recompute()
        assertTrue(h.isEnteredToday())
    }

    @Test
    @Throws(Exception::class)
    fun test_isCompleted_numerical() {
        val h = modelFactory.buildHabit()
        h.type = HabitType.NUMERICAL
        h.targetType = NumericalHabitType.AT_LEAST
        h.targetValue = 100.0
        assertFalse(h.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 200000))
        h.recompute()
        assertTrue(h.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 100000))
        h.recompute()
        assertTrue(h.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 50000))
        h.recompute()
        assertFalse(h.isCompletedToday())
        h.targetType = NumericalHabitType.AT_MOST
        h.originalEntries.add(Entry(getToday(), 200000))
        h.recompute()
        assertFalse(h.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 100000))
        h.recompute()
        assertTrue(h.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 50000))
        h.recompute()
        assertTrue(h.isCompletedToday())
    }

    @Test
    @Throws(Exception::class)
    fun testURI() {
        assertTrue(habitList.isEmpty)
        val h = modelFactory.buildHabit()
        habitList.add(h)
        assertThat(h.id, equalTo(0L))
        assertThat(h.uriString, equalTo("content://org.isoron.uhabits/habit/0"))
    }
}
