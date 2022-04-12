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

import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.utils.DateUtils
import java.util.ArrayList
import java.util.Calendar
import javax.annotation.concurrent.ThreadSafe
import kotlin.collections.set
import kotlin.math.max
import kotlin.math.min

@ThreadSafe
open class EntryList {

    private val entriesByTimestamp: HashMap<Timestamp, Entry> = HashMap()

    /**
     * Returns the entry corresponding to the given timestamp. If no entry with such timestamp
     * has been previously added, returns Entry(timestamp, UNKNOWN).
     */
    @Synchronized
    open fun get(timestamp: Timestamp): Entry {
        return entriesByTimestamp[timestamp] ?: Entry(timestamp, UNKNOWN)
    }

    /**
     * Returns one entry for each day in the given interval. The first element corresponds to the
     * newest entry, and the last element corresponds to the oldest. The interval endpoints are
     * included.
     */
    @Synchronized
    open fun getByInterval(from: Timestamp, to: Timestamp): List<Entry> {
        val result = mutableListOf<Entry>()
        if (from.isNewerThan(to)) return result
        var current = to
        while (current >= from) {
            result.add(get(current))
            current = current.minus(1)
        }
        return result
    }

    /**
     * Adds the given entry to the list. If another entry with the same timestamp already exists,
     * replaces it.
     */
    @Synchronized
    open fun add(entry: Entry) {
        entriesByTimestamp[entry.timestamp] = entry
    }

    /**
     * Returns all entries whose values are known, sorted by timestamp. The first element
     * corresponds to the newest entry, and the last element corresponds to the oldest.
     */
    @Synchronized
    open fun getKnown(): List<Entry> {
        return entriesByTimestamp.values.sortedBy { it.timestamp }.reversed()
    }

    /**
     * Replaces all entries in this list by entries computed automatically from another list.
     *
     * For boolean habits, this function creates additional entries (with value YES_AUTO) according
     * to the frequency of the habit. For numerical habits, this function simply copies all entries.
     */
    @Synchronized
    open fun recomputeFrom(
        originalEntries: EntryList,
        frequency: Frequency,
        isNumerical: Boolean,
    ) {
        clear()
        val original = originalEntries.getKnown()
        if (isNumerical) {
            original.forEach { add(it) }
        } else {
            val intervals = buildIntervals(frequency, original)
            snapIntervalsTogether(intervals)
            val computed = buildEntriesFromInterval(original, intervals)
            computed.filter { it.value != UNKNOWN || it.notes.isNotEmpty() }.forEach { add(it) }
        }
    }

    /**
     * Removes all known entries.
     */
    @Synchronized
    open fun clear() {
        entriesByTimestamp.clear()
    }

    /**
     * Returns the total number of successful entries for each month, grouped by day of week.
     * <p>
     * The checkmarks are returned in a HashMap. The key is the timestamp for
     * the first day of the month, at midnight (00:00). The value is an integer
     * array with 7 entries. The first entry contains the total number of
     * successful checkmarks during the specified month that occurred on a Saturday. The
     * second entry corresponds to Sunday, and so on. If there are no
     * successful checkmarks during a certain month, the value is null.
     *
     * @return total number of checkmarks by month versus day of week
     */
    @Synchronized
    fun computeWeekdayFrequency(isNumerical: Boolean): HashMap<Timestamp, Array<Int>> {
        val entries = getKnown()
        val map = hashMapOf<Timestamp, Array<Int>>()
        for ((originalTimestamp, value) in entries) {
            val weekday = originalTimestamp.weekday
            val truncatedTimestamp = Timestamp(
                originalTimestamp.toCalendar().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }.timeInMillis
            )

            var list = map[truncatedTimestamp]
            if (list == null) {
                list = arrayOf(0, 0, 0, 0, 0, 0, 0)
                map[truncatedTimestamp] = list
            }

            if (isNumerical) {
                list[weekday] += value
            } else if (value == YES_MANUAL) {
                list[weekday] += 1
            }
        }
        return map
    }

    data class Interval(val begin: Timestamp, val center: Timestamp, val end: Timestamp) {
        val length: Int
            get() = begin.daysUntil(end) + 1
    }

    companion object {
        /**
         * Converts a list of intervals into a list of entries. Entries that fall outside of any
         * interval receive value UNKNOWN. Entries that fall within an interval but do not appear
         * in [original] receive value YES_AUTO. Entries provided in [original] are copied over.
         *
         * The intervals should be sorted by timestamp. The first element in the list should
         * correspond to the newest interval.
         */
        fun buildEntriesFromInterval(
            original: List<Entry>,
            intervals: List<Interval>,
        ): List<Entry> {
            val result = arrayListOf<Entry>()
            if (original.isEmpty()) return result

            var from = original[0].timestamp
            var to = original[0].timestamp

            for (e in original) {
                if (e.timestamp < from) from = e.timestamp
                if (e.timestamp > to) to = e.timestamp
            }
            for (interval in intervals) {
                if (interval.begin < from) from = interval.begin
                if (interval.end > to) to = interval.end
            }

            // Create unknown entries
            var current = to
            while (current >= from) {
                result.add(Entry(current, UNKNOWN))
                current = current.minus(1)
            }

            // Create YES_AUTO entries
            intervals.forEach { interval ->
                current = interval.end
                while (current >= interval.begin) {
                    val offset = current.daysUntil(to)
                    result[offset] = Entry(current, YES_AUTO)
                    current = current.minus(1)
                }
            }

            // Copy original entries
            original.forEach { entry ->
                val offset = entry.timestamp.daysUntil(to)
                if (result[offset].value == UNKNOWN || entry.value == SKIP || entry.value == YES_MANUAL) {
                    result[offset] = entry
                }
            }

            return result
        }

        /**
         * Starting from the second newest interval, this function tries to slide the
         * intervals backwards into the past, so that gaps are eliminated and
         * streaks are maximized.
         *
         * The intervals should be sorted by timestamp. The first element in the list should
         * correspond to the newest interval.
         */
        fun snapIntervalsTogether(intervals: ArrayList<Interval>) {
            for (i in 1 until intervals.size) {
                val curr = intervals[i]
                val next = intervals[i - 1]
                val gapNextToCurrent = next.begin.daysUntil(curr.end)
                val gapCenterToEnd = curr.center.daysUntil(curr.end)
                if (gapNextToCurrent >= 0) {
                    val shift = min(gapCenterToEnd, gapNextToCurrent + 1)
                    intervals[i] = Interval(
                        curr.begin.minus(shift),
                        curr.center,
                        curr.end.minus(shift)
                    )
                }
            }
        }

        fun buildIntervals(
            freq: Frequency,
            entries: List<Entry>,
        ): ArrayList<Interval> {
            val filtered = entries.filter { it.value == YES_MANUAL }
            val num = freq.numerator
            val den = freq.denominator
            val intervals = arrayListOf<Interval>()
            for (i in num - 1 until filtered.size) {
                val (begin, _) = filtered[i]
                val (center, _) = filtered[i - num + 1]
                var size = den
                if (den == 30 || den == 31) {
                    val beginDate = begin.toLocalDate()
                    size = if (beginDate.day == beginDate.monthLength) {
                        beginDate.plus(1).monthLength
                    } else {
                        beginDate.monthLength
                    }
                }
                if (begin.daysUntil(center) < size) {
                    val end = begin.plus(size - 1)
                    intervals.add(Interval(begin, center, end))
                }
            }
            return intervals
        }
    }
}

/**
 * Given a list of entries, truncates the timestamp of each entry (according to the field given),
 * groups the entries according to this truncated timestamp, then creates a new entry (t,v) for
 * each group, where t is the truncated timestamp and v is the sum of the values of all entries in
 * the group.
 *
 * For numerical habits, non-positive entry values are converted to zero. For boolean habits, each
 * YES_MANUAL value is converted to 1000 and all other values are converted to zero.
 *
 * SKIP values are converted to zero (if they weren't, each SKIP day would count as 0.003).
 *
 * The returned list is sorted by timestamp, with the newest entry coming first and the oldest entry
 * coming last. If the original list has gaps in it (for example, weeks or months without any
 * entries), then the list produced by this method will also have gaps.
 *
 * The argument [firstWeekday] is only relevant when truncating by week.
 */
fun List<Entry>.groupedSum(
    truncateField: DateUtils.TruncateField,
    firstWeekday: Int = Calendar.SATURDAY,
    isNumerical: Boolean,
): List<Entry> {
    return this.map { (timestamp, value) ->
        if (isNumerical) {
            if (value == SKIP)
                Entry(timestamp, 0)
            else
                Entry(timestamp, max(0, value))
        } else {
            Entry(timestamp, if (value == YES_MANUAL) 1000 else 0)
        }
    }.groupBy { entry ->
        entry.timestamp.truncate(
            truncateField,
            firstWeekday,
        )
    }.entries.map { (timestamp, entries) ->
        Entry(timestamp, entries.sumOf { it.value })
    }.sortedBy { (timestamp, _) ->
        -timestamp.unixTime
    }
}

/**
 * Counts the number of days with vaLue SKIP in the given period.
 */
fun List<Entry>.countSkippedDays(
    truncateField: DateUtils.TruncateField,
    firstWeekday: Int = Calendar.SATURDAY
): List<Entry> {
    return this.map { (timestamp, value) ->
        if (value == SKIP) {
            Entry(timestamp, 1)
        } else {
            Entry(timestamp, 0)
        }
    }.groupBy { entry ->
        entry.timestamp.truncate(
            truncateField,
            firstWeekday,
        )
    }.entries.map { (timestamp, entries) ->
        Entry(timestamp, entries.sumOf { it.value })
    }.sortedBy { (timestamp, _) ->
        -timestamp.unixTime
    }
}
