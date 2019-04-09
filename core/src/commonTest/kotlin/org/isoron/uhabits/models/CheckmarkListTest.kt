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

package org.isoron.uhabits.models

import org.isoron.platform.time.*
import org.isoron.uhabits.models.Checkmark.Companion.CHECKED_AUTOMATIC
import org.isoron.uhabits.models.Checkmark.Companion.CHECKED_MANUAL
import org.isoron.uhabits.models.Checkmark.Companion.UNCHECKED
import kotlin.test.*

class CheckmarkListTest {

    private val today = LocalDate(2019, 1, 30)

    private fun day(offset: Int): LocalDate {
        return today.minus(offset)
    }

    @Test
    fun buildIntervalsWeekly() {
        val checks = listOf(Checkmark(day(23), CHECKED_MANUAL),
                            Checkmark(day(18), CHECKED_MANUAL),
                            Checkmark(day(8), CHECKED_MANUAL))
        val expected = listOf(
                CheckmarkList.Interval(day(23), day(23), day(17)),
                CheckmarkList.Interval(day(18), day(18), day(12)),
                CheckmarkList.Interval(day(8), day(8), day(2)))
        val actual = CheckmarkList.buildIntervals(checks,
                                                  Frequency.WEEKLY)
        assertEquals(expected, actual)
    }

    @Test
    fun buildIntervalsDaily() {
        val checks = listOf(Checkmark(day(23), CHECKED_MANUAL),
                            Checkmark(day(18), CHECKED_MANUAL),
                            Checkmark(day(8), CHECKED_MANUAL))
        val expected = listOf(
                CheckmarkList.Interval(day(23), day(23), day(23)),
                CheckmarkList.Interval(day(18), day(18), day(18)),
                CheckmarkList.Interval(day(8), day(8), day(8)))
        val actual = CheckmarkList.buildIntervals(checks,
                                                  Frequency.DAILY)
        assertEquals(expected, actual)
    }

    @Test
    fun buildIntervalsTwoPerWeek() {
        val checks = listOf(Checkmark(day(23), CHECKED_MANUAL),
                            Checkmark(day(22), CHECKED_MANUAL),
                            Checkmark(day(18), CHECKED_MANUAL),
                            Checkmark(day(15), CHECKED_MANUAL),
                            Checkmark(day(8), CHECKED_MANUAL))
        val expected = listOf(
                CheckmarkList.Interval(day(23), day(22), day(17)),
                CheckmarkList.Interval(day(22), day(18), day(16)),
                CheckmarkList.Interval(day(18), day(15), day(12)))
        val actual = CheckmarkList.buildIntervals(checks,
                                                  Frequency.TWO_TIMES_PER_WEEK)
        assertEquals(expected, actual)
    }

    @Test
    fun testSnapIntervalsTogether() {
        val original = mutableListOf(
                CheckmarkList.Interval(day(40), day(40), day(34)),
                CheckmarkList.Interval(day(25), day(25), day(19)),
                CheckmarkList.Interval(day(16), day(16), day(10)),
                CheckmarkList.Interval(day(8), day(8), day(2)))
        val expected = listOf(
                CheckmarkList.Interval(day(40), day(40), day(34)),
                CheckmarkList.Interval(day(25), day(25), day(19)),
                CheckmarkList.Interval(day(18), day(16), day(12)),
                CheckmarkList.Interval(day(11), day(8), day(5)))
        CheckmarkList.snapIntervalsTogether(original)
        assertEquals(expected, original)
    }

    @Test
    fun testBuildCheckmarksFromIntervals() {
        val checks = listOf(Checkmark(day(10), CHECKED_MANUAL),
                            Checkmark(day(5), CHECKED_MANUAL),
                            Checkmark(day(2), CHECKED_MANUAL),
                            Checkmark(day(1), CHECKED_MANUAL))
        val intervals = listOf(CheckmarkList.Interval(day(10), day(8), day(8)),
                               CheckmarkList.Interval(day(6), day(5), day(4)),
                               CheckmarkList.Interval(day(2), day(2), day(1)))
        val expected = listOf(Checkmark(day(1), CHECKED_MANUAL),
                              Checkmark(day(2), CHECKED_MANUAL),
                              Checkmark(day(3), UNCHECKED),
                              Checkmark(day(4), CHECKED_AUTOMATIC),
                              Checkmark(day(5), CHECKED_MANUAL),
                              Checkmark(day(6), CHECKED_AUTOMATIC),
                              Checkmark(day(7), UNCHECKED),
                              Checkmark(day(8), CHECKED_AUTOMATIC),
                              Checkmark(day(9), CHECKED_AUTOMATIC),
                              Checkmark(day(10), CHECKED_MANUAL))
        val actual = CheckmarkList.buildCheckmarksFromIntervals(checks, intervals)
        assertEquals(expected, actual)
    }

    @Test
    fun testBuildCheckmarksFromIntervals2() {
        val reps = listOf(Checkmark(day(0), CHECKED_MANUAL))
        val intervals = listOf(CheckmarkList.Interval(day(5), day(0), day(0)))
        val expected = listOf(Checkmark(day(0), CHECKED_MANUAL),
                              Checkmark(day(1), CHECKED_AUTOMATIC),
                              Checkmark(day(2), CHECKED_AUTOMATIC),
                              Checkmark(day(3), CHECKED_AUTOMATIC),
                              Checkmark(day(4), CHECKED_AUTOMATIC),
                              Checkmark(day(5), CHECKED_AUTOMATIC))
        val actual = CheckmarkList.buildCheckmarksFromIntervals(reps, intervals)
        assertEquals(expected, actual)
    }

    @Test
    fun computeAutomaticCheckmarks() {
        val checks = listOf(Checkmark(day(10), CHECKED_MANUAL),
                            Checkmark(day(5), CHECKED_MANUAL),
                            Checkmark(day(2), CHECKED_MANUAL),
                            Checkmark(day(1), CHECKED_MANUAL))
        val expected = listOf(Checkmark(day(-1), CHECKED_AUTOMATIC),
                              Checkmark(day(0), CHECKED_AUTOMATIC),
                              Checkmark(day(1), CHECKED_MANUAL),
                              Checkmark(day(2), CHECKED_MANUAL),
                              Checkmark(day(3), CHECKED_AUTOMATIC),
                              Checkmark(day(4), CHECKED_AUTOMATIC),
                              Checkmark(day(5), CHECKED_MANUAL),
                              Checkmark(day(6), CHECKED_AUTOMATIC),
                              Checkmark(day(7), CHECKED_AUTOMATIC),
                              Checkmark(day(8), CHECKED_AUTOMATIC),
                              Checkmark(day(9), CHECKED_AUTOMATIC),
                              Checkmark(day(10), CHECKED_MANUAL))
        val actual = CheckmarkList.computeCheckmarks(checks, Frequency(1, 3))
        assertEquals(expected, actual)
    }

    @Test
    fun testGetUntil() {
        val list = CheckmarkList(Frequency(1, 2), HabitType.BOOLEAN_HABIT)
        list.setManualCheckmarks(listOf(Checkmark(day(4), CHECKED_MANUAL),
                                        Checkmark(day(7), CHECKED_MANUAL)))
        val expected = listOf(Checkmark(day(0), UNCHECKED),
                              Checkmark(day(1), UNCHECKED),
                              Checkmark(day(2), UNCHECKED),
                              Checkmark(day(3), CHECKED_AUTOMATIC),
                              Checkmark(day(4), CHECKED_MANUAL),
                              Checkmark(day(5), UNCHECKED),
                              Checkmark(day(6), CHECKED_AUTOMATIC),
                              Checkmark(day(7), CHECKED_MANUAL))
        assertEquals(expected, list.getUntil(day(0)))

        val expected2 = listOf(Checkmark(day(3), CHECKED_AUTOMATIC),
                               Checkmark(day(4), CHECKED_MANUAL),
                               Checkmark(day(5), UNCHECKED),
                               Checkmark(day(6), CHECKED_AUTOMATIC),
                               Checkmark(day(7), CHECKED_MANUAL))
        assertEquals(expected2, list.getUntil(day(3)))
    }

    @Test
    fun testGetValuesUntil2() {
        val list = CheckmarkList(Frequency(1, 2), HabitType.BOOLEAN_HABIT)
        val expected = listOf<Checkmark>()
        assertEquals(expected, list.getUntil(day(0)))
    }
}