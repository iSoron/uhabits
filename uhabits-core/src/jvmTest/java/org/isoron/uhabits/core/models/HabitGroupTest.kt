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

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HabitGroupTest : BaseUnitTest() {

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun testUuidGeneration() {
        val uuid1 = modelFactory.buildHabitGroup().uuid!!
        val uuid2 = modelFactory.buildHabitGroup().uuid!!
        assertNotEquals(uuid1, uuid2)
    }

    @Test
    fun test_copyAttributes() {
        val model = modelFactory.buildHabitGroup()
        model.isArchived = true
        model.color = PaletteColor(0)
        model.reminder = Reminder(8, 30, WeekdayList(1))
        val habitGroup = modelFactory.buildHabitGroup()
        habitGroup.copyFrom(model)
        assertEquals(habitGroup.isArchived, model.isArchived)
        assertThat(habitGroup.isArchived, `is`(model.isArchived))
        assertThat(habitGroup.color, `is`(model.color))
        assertThat(habitGroup.reminder, equalTo(model.reminder))
    }

    @Test
    fun test_hasReminder() {
        val hgr = modelFactory.buildHabitGroup()
        assertThat(hgr.hasReminder(), `is`(false))
        hgr.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        assertThat(hgr.hasReminder(), `is`(true))
    }

    @Test
    @Throws(Exception::class)
    fun test_isCompleted() {
        val hgr = groupFixtures.createGroupWithEmptyHabits()
        assertFalse(hgr.isCompletedToday())
        hgr.habitList.getByPosition(0).originalEntries.add(Entry(getToday(), Entry.YES_MANUAL))
        hgr.recompute()
        assertTrue(hgr.isCompletedToday())
    }

    @Test
    @Throws(Exception::class)
    fun test_isEntered() {
        val hgr = groupFixtures.createGroupWithEmptyHabits()
        assertFalse(hgr.isEnteredToday())
        hgr.habitList.getByPosition(0).originalEntries.add(Entry(getToday(), Entry.NO))
        hgr.recompute()
        assertTrue(hgr.isEnteredToday())
    }

    @Test
    @Throws(Exception::class)
    fun test_isCompleted_numerical() {
        val hgr = groupFixtures.createGroupWithEmptyNumericalHabits()
        val h = hgr.habitList.getByPosition(0)
        assertFalse(hgr.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 4000))
        h.recompute()
        assertTrue(hgr.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 2000))
        h.recompute()
        assertTrue(hgr.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 1000))
        h.recompute()
        assertFalse(hgr.isCompletedToday())
        h.targetType = NumericalHabitType.AT_MOST
        h.originalEntries.add(Entry(getToday(), 4000))
        h.recompute()
        assertFalse(hgr.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 2000))
        h.recompute()
        assertTrue(hgr.isCompletedToday())
        h.originalEntries.add(Entry(getToday(), 1000))
        h.recompute()
        assertTrue(hgr.isCompletedToday())
    }

    @Test
    @Throws(Exception::class)
    fun testURI() {
        assertTrue(habitGroupList.isEmpty)
        val hgr = modelFactory.buildHabitGroup()
        habitGroupList.add(hgr)
        assertThat(hgr.id, equalTo(0L))
        assertThat(hgr.uriString, equalTo("content://org.isoron.uhabits/habitgroup/0"))
    }

    @Test
    @Throws(Exception::class)
    fun testScores() {
        val hgr = groupFixtures.createGroupWithNumericalHabits(numHabits = 2)
        hgr.recompute()
        val today = getToday()
        val expectedScore = hgr.habitList.map { it.scores[today].value }.average()
        assertEquals(expectedScore, hgr.scores[today].value)
    }

    @Test
    @Throws(Exception::class)
    fun testStreaks() {
        val hgr = groupFixtures.createGroupWithNumericalHabits(numHabits = 2)
        hgr.recompute()
        assertEquals(hgr.habitList.getByPosition(0).streaks.getBest(1), hgr.streaks.getBest(1))

        val hgr2 = groupFixtures.createGroupWithEmptyHabits(numHabits = 2)
        val h = hgr2.habitList.getByPosition(0)
        h.originalEntries.add(Entry(getToday(), 2))
        h.originalEntries.add(Entry(getToday().minus(1), 2))
        val h2 = hgr2.habitList.getByPosition(1)
        h2.originalEntries.add(Entry(getToday().minus(2), 2))
        hgr2.recompute()
        assertEquals(0, hgr2.streaks.getBest(1).size)
    }
}
