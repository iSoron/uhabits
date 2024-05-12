/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.TruncateField
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import kotlin.collections.set
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

open class EntryList {

    private val entriesByDate: HashMap<LocalDate, Entry> = HashMap()

    /**
     * Returns the entry corresponding to the given date. If no entry with such date
     * has been previously added, returns Entry(date, UNKNOWN).
     */
    @Synchronized
    open fun get(date: LocalDate): Entry {
        return entriesByDate[date] ?: Entry(date, UNKNOWN)
    }

    /**
     * Returns one entry for each day in the given interval. The first element corresponds to the
     * newest entry, and the last element corresponds to the oldest. The interval endpoints are
     * included.
     */
    @Synchronized
    open fun getByInterval(from: LocalDate, to: LocalDate): List<Entry> {
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
     * Adds the given entry to the list. If another entry with the same date already exists,
     * replaces it.
     */
    @Synchronized
    open fun add(entry: Entry) {
        entriesByDate[entry.date] = entry
    }

    /**
     * Returns all entries whose values are known, sorted by date. The first element
     * corresponds to the newest entry, and the last element corresponds to the oldest.
     */
    @Synchronized
    open fun getKnown(): List<Entry> {
        return entriesByDate.values.sortedBy { it.date }.reversed()
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
        isNumerical: Boolean
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
        entriesByDate.clear()
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
    fun computeWeekdayFrequency(isNumerical: Boolean): HashMap<LocalDate, Array<Int>> {
        val entries = getKnown()
        val map = hashMapOf<LocalDate, Array<Int>>()
        for (entry in entries) {
            val weekday = (entry.date.dayOfWeek.daysSinceSunday + 1) % 7
            val monthStart = entry.date.startOfMonth()

            var list = map[monthStart]
            if (list == null) {
                list = arrayOf(0, 0, 0, 0, 0, 0, 0)
                map[monthStart] = list
            }

            if (isNumerical) {
                list[weekday] += entry.value
            } else if (entry.value == YES_MANUAL) {
                list[weekday] += 1
            }
        }
        return map
    }

    @Synchronized fun normalizeEntries(
        isNumerical: Boolean,
        frequency: Frequency,
        targetValue: Double
    ): EntryList {
        val entries = getKnown()
        val normalized = EntryList()
        val dailyTarget = frequency.toDouble() * (if (isNumerical) targetValue * 1000 else 2.0)
        for (entry in entries) {
            if (!isNumerical && entry.value != YES_MANUAL) continue
            val newValue = (entry.value.toDouble() / dailyTarget * 1000).roundToInt()
            normalized.add(Entry(entry.timestamp, newValue))
        }
        return normalized
    }

    data class Interval(val begin: LocalDate, val center: LocalDate, val end: LocalDate) {
        val length: Int
            get() = begin.daysUntil(end) + 1
    }

    companion object {
        /**
         * Converts a list of intervals into a list of entries. Entries that fall outside of any
         * interval receive value UNKNOWN. Entries that fall within an interval but do not appear
         * in [original] receive value YES_AUTO. Entries provided in [original] are copied over.
         *
         * The intervals should be sorted by date. The first element in the list should
         * correspond to the newest interval.
         */
        fun buildEntriesFromInterval(
            original: List<Entry>,
            intervals: List<Interval>
        ): List<Entry> {
            val result = arrayListOf<Entry>()
            if (original.isEmpty()) return result

            var from = original[0].date
            var to = original[0].date

            for (e in original) {
                if (e.date < from) from = e.date
                if (e.date > to) to = e.date
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
                val offset = entry.date.daysUntil(to)
                val value = if (
                    result[offset].value == UNKNOWN ||
                    entry.value == SKIP ||
                    entry.value == YES_MANUAL
                ) {
                    entry.value
                } else {
                    YES_AUTO
                }
                result[offset] = Entry(entry.date, value, entry.notes)
            }

            return result
        }

        /**
         * Starting from the second newest interval, this function tries to slide the
         * intervals backwards into the past, so that gaps are eliminated and
         * streaks are maximized.
         *
         * The intervals should be sorted by date. The first element in the list should
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
            entries: List<Entry>
        ): ArrayList<Interval> {
            val filtered = entries.filter { it.value == YES_MANUAL }
            val num = freq.numerator
            val den = freq.denominator
            val intervals = arrayListOf<Interval>()
            for (i in num - 1 until filtered.size) {
                val begin = filtered[i].date
                val center = filtered[i - num + 1].date
                var size = den
                if (den == 30 || den == 31) {
                    size = if (begin.day == begin.monthLength) {
                        begin.plus(1).monthLength
                    } else {
                        begin.monthLength
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

private fun truncateDate(
    date: LocalDate,
    field: TruncateField,
    firstWeekday: DayOfWeek
): LocalDate {
    return when (field) {
        TruncateField.DAY -> date
        TruncateField.WEEK_NUMBER -> date.startOfWeek(firstWeekday)
        TruncateField.MONTH -> date.startOfMonth()
        TruncateField.QUARTER -> date.startOfQuarter()
        TruncateField.YEAR -> date.startOfYear()
    }
}

/**
 * Given a list of entries, truncates the date of each entry (according to the field given),
 * groups the entries according to this truncated date, then creates a new entry (d,v) for
 * each group, where d is the truncated date and v is the sum of the values of all entries in
 * the group.
 *
 * For numerical habits, non-positive entry values are converted to zero. For boolean habits, each
 * YES_MANUAL value is converted to 1000 and all other values are converted to zero.
 *
 * SKIP values are converted to zero (if they weren't, each SKIP day would count as 0.003).
 *
 * The returned list is sorted by date, with the newest entry coming first and the oldest entry
 * coming last. If the original list has gaps in it (for example, weeks or months without any
 * entries), then the list produced by this method will also have gaps.
 *
 * The argument [firstWeekday] is only relevant when truncating by week.
 */
fun List<Entry>.groupedSum(
    truncateField: TruncateField,
    firstWeekday: Int = 7,
    isNumerical: Boolean
): List<Entry> {
    val firstWeekdayEnum = DayOfWeek.values()[firstWeekday - 1]
    return this.map { (date, value) ->
        if (isNumerical) {
            if (value == SKIP) {
                Entry(date, 0)
            } else {
                Entry(date, max(0, value))
            }
        } else {
            Entry(date, if (value == YES_MANUAL) 1000 else 0)
        }
    }.groupBy { entry ->
        truncateDate(entry.date, truncateField, firstWeekdayEnum)
    }.entries.map { (date, entries) ->
        Entry(date, entries.sumOf { it.value })
    }.sortedBy { (date, _) ->
        -date.daysSince2000
    }
}

fun List<Entry>.groupedSum(): List<Entry> {
    return this
        .groupBy { entry -> entry.date }
        .entries.map { (date, entries) ->
            Entry(date, entries.sumOf { it.value })
        }.sortedBy { (date, _) ->
            -date.daysSince2000
        }
}

/**
 * Counts the number of days with value SKIP in the given period.
 */
fun List<Entry>.countSkippedDays(
    truncateField: TruncateField,
    firstWeekday: Int = 7
): List<Entry> {
    val firstWeekdayEnum = DayOfWeek.values()[firstWeekday - 1]
    return this.map { (date, value) ->
        if (value == SKIP) {
            Entry(date, 1)
        } else {
            Entry(date, 0)
        }
    }.groupBy { entry ->
        truncateDate(entry.date, truncateField, firstWeekdayEnum)
    }.entries.map { (date, entries) ->
        Entry(date, entries.sumOf { it.value })
    }.sortedBy { (date, _) ->
        -date.daysSince2000
    }
}
