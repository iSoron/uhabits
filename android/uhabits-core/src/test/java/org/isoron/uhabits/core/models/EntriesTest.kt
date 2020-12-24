/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

import org.hamcrest.MatcherAssert.*
import org.hamcrest.core.IsEqual.*
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.utils.*
import org.junit.*
import java.util.*
import kotlin.test.*

class EntriesTest {
    @Test
    fun testEmptyList() {
        val entries = Entries()
        val today = DateUtils.getToday()

        assertEquals(Entry(today.minus(0), UNKNOWN), entries.get(today.minus(0)))
        assertEquals(Entry(today.minus(2), UNKNOWN), entries.get(today.minus(2)))
        assertEquals(Entry(today.minus(5), UNKNOWN), entries.get(today.minus(5)))

        entries.add(Entry(today.minus(0), 10))
        entries.add(Entry(today.minus(0), 15)) // replace previous one
        entries.add(Entry(today.minus(5), 20))
        entries.add(Entry(today.minus(8), 30))
        assertEquals(Entry(today.minus(0), 15), entries.get(today.minus(0)))
        assertEquals(Entry(today.minus(5), 20), entries.get(today.minus(5)))
        assertEquals(Entry(today.minus(8), 30), entries.get(today.minus(8)))

        val known = entries.getKnown()
        assertEquals(3, known.size)
        assertEquals(Entry(today.minus(0), 15), known[0])
        assertEquals(Entry(today.minus(5), 20), known[1])
        assertEquals(Entry(today.minus(8), 30), known[2])

        val actual = entries.getByInterval(today.minus(5), today)
        assertEquals(6, actual.size)
        assertEquals(Entry(today.minus(0), 15), actual[0])
        assertEquals(Entry(today.minus(1), UNKNOWN), actual[1])
        assertEquals(Entry(today.minus(2), UNKNOWN), actual[2])
        assertEquals(Entry(today.minus(3), UNKNOWN), actual[3])
        assertEquals(Entry(today.minus(4), UNKNOWN), actual[4])
        assertEquals(Entry(today.minus(5), 20), actual[5])
    }

    @Test
    fun testComputeBoolean() {
        val today = DateUtils.getToday()

        val original = Entries()
        original.add(Entry(today.minus(4), YES_MANUAL))
        original.add(Entry(today.minus(9), YES_MANUAL))
        original.add(Entry(today.minus(10), YES_MANUAL))

        val computed = Entries()
        computed.computeFrom(original, Frequency(1, 3), isNumerical = false)

        val expected = listOf(
                Entry(today.minus(2), YES_AUTO),
                Entry(today.minus(3), YES_AUTO),
                Entry(today.minus(4), YES_MANUAL),
                Entry(today.minus(7), YES_AUTO),
                Entry(today.minus(8), YES_AUTO),
                Entry(today.minus(9), YES_MANUAL),
                Entry(today.minus(10), YES_MANUAL),
                Entry(today.minus(11), YES_AUTO),
                Entry(today.minus(12), YES_AUTO),
        )
        assertEquals(expected, computed.getKnown())

        // Second call should replace all previously added entries
        computed.computeFrom(Entries(), Frequency(1, 3), isNumerical = false)
        assertEquals(listOf(), computed.getKnown())

    }

    @Test
    fun testComputeNumerical() {
        val today = DateUtils.getToday()

        val original = Entries()
        original.add(Entry(today.minus(4), 100))
        original.add(Entry(today.minus(9), 200))
        original.add(Entry(today.minus(10), 300))

        val computed = Entries()
        computed.computeFrom(original, Frequency.DAILY, isNumerical = true)

        val expected = listOf(
                Entry(today.minus(4), 100),
                Entry(today.minus(9), 200),
                Entry(today.minus(10), 300),
        )
        assertEquals(expected, computed.getKnown())
    }

    @Test
    fun testGroupBy() {
        val offsets = intArrayOf(
                0, 5, 9, 15, 17, 21, 23, 27, 28, 35, 41, 45, 47, 53, 56, 62, 70, 73, 78,
                83, 86, 94, 101, 106, 113, 114, 120, 126, 130, 133, 141, 143, 148, 151, 157, 164,
                166, 171, 173, 176, 179, 183, 191, 259, 264, 268, 270, 275, 282, 284, 289, 295,
                302, 306, 310, 315, 323, 325, 328, 335, 343, 349, 351, 353, 357, 359, 360, 367,
                372, 376, 380, 385, 393, 400, 404, 412, 415, 418, 422, 425, 433, 437, 444, 449,
                455, 460, 462, 465, 470, 471, 479, 481, 485, 489, 494, 495, 500, 501, 503, 507)

        val values = intArrayOf(
                230, 306, 148, 281, 134, 285, 104, 158, 325, 236, 303, 210, 118, 124,
                301, 201, 156, 376, 347, 367, 396, 134, 160, 381, 155, 354, 231, 134, 164, 354,
                236, 398, 199, 221, 208, 397, 253, 276, 214, 341, 299, 221, 353, 250, 341, 168,
                374, 205, 182, 217, 297, 321, 104, 237, 294, 110, 136, 229, 102, 271, 250, 294,
                158, 319, 379, 126, 282, 155, 288, 159, 215, 247, 207, 226, 244, 158, 371, 219,
                272, 228, 350, 153, 356, 279, 394, 202, 213, 214, 112, 248, 139, 245, 165, 256,
                370, 187, 208, 231, 341, 312)

        val reference = Timestamp.from(2014, Calendar.JUNE, 1)
        val entries = Entries()
        offsets.indices.forEach {
            entries.add(Entry(reference.minus(offsets[it]), values[it]))
        }

        val byMonth = entries.groupBy(
                field = DateUtils.TruncateField.MONTH,
                firstWeekday = Calendar.SATURDAY,
                isNumerical = true,
        )
        assertThat(byMonth.size, equalTo(17))
        assertThat(byMonth[0], equalTo(Entry(Timestamp.from(2014, Calendar.JUNE, 1), 230)))
        assertThat(byMonth[6], equalTo(Entry(Timestamp.from(2013, Calendar.DECEMBER, 1), 1988)))
        assertThat(byMonth[12], equalTo(Entry(Timestamp.from(2013, Calendar.MAY, 1), 1271)))

        val byQuarter = entries.groupBy(
                field = DateUtils.TruncateField.QUARTER,
                firstWeekday = Calendar.SATURDAY,
                isNumerical = true,
        )
        assertThat(byQuarter.size, equalTo(6))
        assertThat(byQuarter[0], equalTo(Entry(Timestamp.from(2014, Calendar.APRIL, 1), 3263)))
        assertThat(byQuarter[3], equalTo(Entry(Timestamp.from(2013, Calendar.JULY, 1), 3838)))
        assertThat(byQuarter[5], equalTo(Entry(Timestamp.from(2013, Calendar.JANUARY, 1), 4975)))

        val byYear = entries.groupBy(
                field = DateUtils.TruncateField.YEAR,
                firstWeekday = Calendar.SATURDAY,
                isNumerical = true,
        )
        assertThat(byYear.size, equalTo(2))
        assertThat(byYear[0], equalTo(Entry(Timestamp.from(2014, Calendar.JANUARY, 1), 8227)))
        assertThat(byYear[1], equalTo(Entry(Timestamp.from(2013, Calendar.JANUARY, 1), 16172)))
    }

    @Test
    fun testAddFromInterval() {
        val entries = listOf(
                Entry(day(1), YES_MANUAL),
                Entry(day(2), NO),
                Entry(day(5), YES_MANUAL),
                Entry(day(10), YES_MANUAL),
        )
        val intervals = listOf(
                Entries.Interval(day(2), day(2), day(1)),
                Entries.Interval(day(6), day(5), day(4)),
                Entries.Interval(day(10), day(8), day(8)),
        )
        val expected = listOf(
                Entry(day(1), YES_MANUAL),
                Entry(day(2), NO),
                Entry(day(3), UNKNOWN),
                Entry(day(4), YES_AUTO),
                Entry(day(5), YES_MANUAL),
                Entry(day(6), YES_AUTO),
                Entry(day(7), UNKNOWN),
                Entry(day(8), YES_AUTO),
                Entry(day(9), YES_AUTO),
                Entry(day(10), YES_MANUAL),
        )
        val actual = Entries.buildEntriesFromInterval(entries, intervals)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun testSnapIntervalsTogether1() {
        val original = arrayListOf(
                Entries.Interval(day(8), day(8), day(2)),
                Entries.Interval(day(12), day(12), day(6)),
                Entries.Interval(day(20), day(20), day(14)),
                Entries.Interval(day(27), day(27), day(21)),
        )
        val expected = arrayListOf(
                Entries.Interval(day(8), day(8), day(2)),
                Entries.Interval(day(15), day(12), day(9)),
                Entries.Interval(day(22), day(20), day(16)),
                Entries.Interval(day(29), day(27), day(23)),
        )
        Entries.snapIntervalsTogether(original)
        assertThat(original, equalTo(expected))
    }

    @Test
    fun testSnapIntervalsTogether2() {
        val original = arrayListOf(
                Entries.Interval(day(6), day(4), day(0)),
                Entries.Interval(day(11), day(8), day(5)),
        )
        val expected = arrayListOf(
                Entries.Interval(day(6), day(4), day(0)),
                Entries.Interval(day(13), day(8), day(7)),
        )
        Entries.snapIntervalsTogether(original)
        assertThat(original, equalTo(expected))
    }

    @Test
    fun testBuildIntervals1() {
        val entries = listOf(
                Entry(day(8), YES_MANUAL),
                Entry(day(18), YES_MANUAL),
                Entry(day(23), YES_MANUAL),
        )
        val expected = listOf(
                Entries.Interval(day(8), day(8), day(2)),
                Entries.Interval(day(18), day(18), day(12)),
                Entries.Interval(day(23), day(23), day(17)),
        )
        val actual = Entries.buildIntervals(Frequency.WEEKLY, entries)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun testBuildIntervals2() {
        val entries = listOf(
                Entry(day(8), YES_MANUAL),
                Entry(day(18), YES_MANUAL),
                Entry(day(23), YES_MANUAL),
        )
        val expected = listOf(
                Entries.Interval(day(8), day(8), day(8)),
                Entries.Interval(day(18), day(18), day(18)),
                Entries.Interval(day(23), day(23), day(23)),
        )
        val actual = Entries.buildIntervals(Frequency.DAILY, entries)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun testBuildIntervals3() {
        val entries = listOf(
                Entry(day(8), YES_MANUAL),
                Entry(day(15), YES_MANUAL),
                Entry(day(18), YES_MANUAL),
                Entry(day(22), YES_MANUAL),
                Entry(day(23), YES_MANUAL),
        )
        val expected = listOf(
                Entries.Interval(day(18), day(15), day(12)),
                Entries.Interval(day(22), day(18), day(16)),
                Entries.Interval(day(23), day(22), day(17)),
        )
        val actual = Entries.buildIntervals(Frequency.TWO_TIMES_PER_WEEK, entries)
        assertThat(actual, equalTo(expected))
    }


    @Test
    fun testBuildIntervals4() {
        val entries = listOf(
                Entry(day(10), YES_MANUAL),
                Entry(day(20), Entry.SKIP),
                Entry(day(30), YES_MANUAL),
        )
        val expected = listOf(
                Entries.Interval(day(10), day(10), day(8)),
                Entries.Interval(day(30), day(30), day(28)),
        )
        val actual = Entries.buildIntervals(Frequency(1, 3), entries)
        assertThat(actual, equalTo(expected))
    }

    fun day(offset: Int) = DateUtils.getToday().minus(offset)

}
