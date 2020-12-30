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

import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.utils.DateUtils
import java.util.ArrayList
import java.util.Calendar
import javax.annotation.concurrent.ThreadSafe
import kotlin.collections.set
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
     * Truncates the timestamps of all known entries, then aggregates their values. This function
     * is used to generate bar plots where each bar shows the number of repetitions in a given week,
     * month or year.
     *
     * For boolean habits, the value of the aggregated entry equals to the number of YES_MANUAL
     * entries. For numerical habits, the value is the total sum. The field [firstWeekday] is only
     * relevant when grouping by week.
     */
    @Synchronized
    open fun groupBy(
        original: List<Entry>,
        field: DateUtils.TruncateField,
        firstWeekday: Int,
        isNumerical: Boolean,
    ): List<Entry> {
        val truncated = original.map {
            Entry(it.timestamp.truncate(field, firstWeekday), it.value)
        }
        val timestamps = mutableListOf<Timestamp>()
        val values = mutableListOf<Int>()
        for (i in truncated.indices) {
            if (i == 0 || timestamps.last() != truncated[i].timestamp) {
                timestamps.add(truncated[i].timestamp)
                values.add(0)
            }
            if (isNumerical) {
                if (truncated[i].value > 0) {
                    values[values.lastIndex] += truncated[i].value
                }
            } else {
                if (truncated[i].value == YES_MANUAL) {
                    values[values.lastIndex] += 1000
                }
            }
        }
        return timestamps.indices.map { Entry(timestamps[it], values[it]) }
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
            computed.filter { it.value != UNKNOWN }.forEach { add(it) }
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

    /**
     * Returns the values of the entries that fall inside a certain interval of time. The values
     * are returned in an array containing one integer value for each day of the interval. The
     * first entry corresponds to the most recent day in the interval. Each subsequent entry
     * corresponds to one day older than the previous entry. The boundaries of the time interval
     * are included.
     */
    @Deprecated("")
    @Synchronized
    fun getValues(from: Timestamp, to: Timestamp): IntArray {
        if (from.isNewerThan(to)) throw IllegalArgumentException()
        val nDays = from.daysUntil(to) + 1
        val result = IntArray(nDays) { UNKNOWN }
        getKnown().filter { entry ->
            !entry.timestamp.isNewerThan(to) && !entry.timestamp.isOlderThan(from)
        }.forEach { entry ->
            val offset = entry.timestamp.daysUntil(to)
            result[offset] = entry.value
        }
        return result
    }

    @Deprecated("")
    @Synchronized
    fun getAllValues(): IntArray {
        val entries = getKnown()
        if (entries.isEmpty()) return IntArray(0)
        var (fromTimestamp, _) = entries.last()
        val toTimestamp = DateUtils.getTodayWithOffset()
        if (fromTimestamp.isNewerThan(toTimestamp)) fromTimestamp = toTimestamp
        return getValues(fromTimestamp, toTimestamp)
    }

    @Deprecated("")
    @Synchronized
    open fun getThisWeekValue(firstWeekday: Int, isNumerical: Boolean): Int {
        return getThisIntervalValue(
            truncateField = DateUtils.TruncateField.WEEK_NUMBER,
            firstWeekday = firstWeekday,
            isNumerical = isNumerical
        )
    }

    @Deprecated("")
    @Synchronized
    open fun getThisMonthValue(isNumerical: Boolean): Int {
        return getThisIntervalValue(
            truncateField = DateUtils.TruncateField.MONTH,
            firstWeekday = Calendar.SATURDAY,
            isNumerical = isNumerical
        )
    }

    @Deprecated("")
    @Synchronized
    open fun getThisQuarterValue(isNumerical: Boolean): Int {
        return getThisIntervalValue(
            truncateField = DateUtils.TruncateField.QUARTER,
            firstWeekday = Calendar.SATURDAY,
            isNumerical = isNumerical
        )
    }

    @Deprecated("")
    @Synchronized
    open fun getThisYearValue(isNumerical: Boolean): Int {
        return getThisIntervalValue(
            truncateField = DateUtils.TruncateField.YEAR,
            firstWeekday = Calendar.SATURDAY,
            isNumerical = isNumerical
        )
    }

    private fun getThisIntervalValue(
        truncateField: DateUtils.TruncateField,
        firstWeekday: Int,
        isNumerical: Boolean,
    ): Int {
        val groups: List<Entry> = groupBy(getKnown(), truncateField, firstWeekday, isNumerical)
        return if (groups.isEmpty()) 0 else groups[0].value
    }

    data class Interval(val begin: Timestamp, val center: Timestamp, val end: Timestamp) {
        val length: Int
            get() = begin.daysUntil(end) + 1
    }

    /**
     * Converts a list of intervals into a list of entries. Entries that fall outside of any
     * interval receive value UNKNOWN. Entries that fall within an interval but do not appear
     * in [original] receive value YES_AUTO. Entries provided in [original] are copied over.
     *
     * The intervals should be sorted by timestamp. The first element in the list should
     * correspond to the newest interval.
     */
    companion object {
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
                if (begin.daysUntil(center) < den) {
                    val end = begin.plus(den - 1)
                    intervals.add(Interval(begin, center, end))
                }
            }
            return intervals
        }
    }
}
