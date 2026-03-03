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

import javax.annotation.concurrent.ThreadSafe
import kotlin.math.min

@ThreadSafe
class StreakList {
    private val list = ArrayList<Streak>()

    @Synchronized
    fun getBest(limit: Int): List<Streak> {
        list.sortWith { s1: Streak, s2: Streak -> s2.compareLonger(s1) }
        return list.subList(0, min(list.size, limit)).apply {
            sortWith { s1: Streak, s2: Streak -> s2.compareNewer(s1) }
        }.toList()
    }

    /**
     * Calculates the current streak as the number of consecutive completed periods.
     *
     * For daily habits (1/1): counts consecutive days with a check, going backwards from yesterday.
     * For weekly habits (x/7): counts consecutive weeks where checks >= numerator.
     * For monthly habits (x/30 or x/31): counts consecutive months where checks >= numerator.
     * For custom (x/y): counts consecutive y-day windows where checks >= numerator.
     */
    fun getCurrentStreakCount(
        originalEntries: EntryList,
        frequency: Frequency,
        today: Timestamp
    ): Int {
        val num = frequency.numerator
        val den = frequency.denominator

        // Daily habit: use the existing day-based streak list
        if (num == 1 && den == 1) {
            for (streak in list) {
                if (streak.end == today || streak.end == today.minus(1)) {
                    return streak.length
                }
            }
            return 0
        }

        // Collect all manually checked timestamps
        val checks = originalEntries.getKnown()
            .filter { it.value == Entry.YES_MANUAL }
            .map { it.timestamp }
            .toSet()

        if (checks.isEmpty()) return 0

        // Count completed periods going backwards from the current/previous period
        var streakCount = 0

        if (den == 7) {
            // Weekly: count consecutive completed weeks
            var weekStart = getWeekStart(today)
            // Check current week first; if not met, start from previous week
            val checksThisWeek = countChecksInRange(checks, weekStart, weekStart.plus(6))
            if (checksThisWeek >= num) {
                streakCount++
                weekStart = weekStart.minus(7)
            } else {
                weekStart = weekStart.minus(7)
                // Check if previous week qualifies
                val checksPrevWeek = countChecksInRange(checks, weekStart, weekStart.plus(6))
                if (checksPrevWeek < num) return 0
                streakCount++
                weekStart = weekStart.minus(7)
            }
            // Continue counting backwards
            while (true) {
                val count = countChecksInRange(checks, weekStart, weekStart.plus(6))
                if (count >= num) {
                    streakCount++
                    weekStart = weekStart.minus(7)
                } else {
                    break
                }
            }
        } else if (den == 30 || den == 31) {
            // Monthly: count consecutive completed months
            var cal = today.toCalendar()
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
            var monthStart = Timestamp(cal.timeInMillis)
            var monthEnd = getMonthEnd(monthStart)

            val checksThisMonth = countChecksInRange(checks, monthStart, monthEnd)
            if (checksThisMonth >= num) {
                streakCount++
            } else {
                // Move to previous month
                cal.add(java.util.Calendar.MONTH, -1)
                monthStart = Timestamp(cal.timeInMillis)
                monthEnd = getMonthEnd(monthStart)
                val checksPrevMonth = countChecksInRange(checks, monthStart, monthEnd)
                if (checksPrevMonth < num) return 0
                streakCount++
            }
            // Continue counting backwards
            while (true) {
                cal.add(java.util.Calendar.MONTH, -1)
                monthStart = Timestamp(cal.timeInMillis)
                monthEnd = getMonthEnd(monthStart)
                val count = countChecksInRange(checks, monthStart, monthEnd)
                if (count >= num) {
                    streakCount++
                } else {
                    break
                }
            }
        } else {
            // Custom: x times in y days — use sliding windows of size `den`
            var windowEnd = today
            var windowStart = today.minus(den - 1)

            val checksThisWindow = countChecksInRange(checks, windowStart, windowEnd)
            if (checksThisWindow >= num) {
                streakCount++
                windowEnd = windowStart.minus(1)
                windowStart = windowEnd.minus(den - 1)
            } else {
                windowEnd = windowEnd.minus(den)
                windowStart = windowEnd.minus(den - 1)
                val checksPrevWindow = countChecksInRange(checks, windowStart, windowEnd)
                if (checksPrevWindow < num) return 0
                streakCount++
                windowEnd = windowStart.minus(1)
                windowStart = windowEnd.minus(den - 1)
            }
            while (true) {
                val count = countChecksInRange(checks, windowStart, windowEnd)
                if (count >= num) {
                    streakCount++
                    windowEnd = windowStart.minus(1)
                    windowStart = windowEnd.minus(den - 1)
                } else {
                    break
                }
            }
        }

        return streakCount
    }

    private fun getWeekStart(day: Timestamp): Timestamp {
        val cal = day.toCalendar()
        // Set to Monday as start of week
        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY) {
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        return Timestamp(cal.timeInMillis)
    }

    private fun getMonthEnd(monthStart: Timestamp): Timestamp {
        val cal = monthStart.toCalendar()
        val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        return monthStart.plus(daysInMonth - 1)
    }

    private fun countChecksInRange(checks: Set<Timestamp>, from: Timestamp, to: Timestamp): Int {
        var count = 0
        var current = from
        while (!current.isNewerThan(to)) {
            if (checks.contains(current)) count++
            current = current.plus(1)
        }
        return count
    }

    @Synchronized
    fun recompute(
        computedEntries: EntryList,
        from: Timestamp,
        to: Timestamp,
        isNumerical: Boolean,
        targetValue: Double,
        targetType: NumericalHabitType
    ) {
        list.clear()
        val timestamps = computedEntries
            .getByInterval(from, to)
            .filter {
                val value = it.value
                if (isNumerical) {
                    when (targetType) {
                        NumericalHabitType.AT_LEAST -> value / 1000.0 >= targetValue
                        NumericalHabitType.AT_MOST -> value != Entry.UNKNOWN && value / 1000.0 <= targetValue
                    }
                } else {
                    value > 0
                }
            }
            .map { it.timestamp }
            .toTypedArray()

        if (timestamps.isEmpty()) return

        var begin = timestamps[0]
        var end = timestamps[0]
        for (i in 1 until timestamps.size) {
            val current = timestamps[i]
            if (current == begin.minus(1)) {
                begin = current
            } else {
                list.add(Streak(begin, end))
                begin = current
                end = current
            }
        }
        list.add(Streak(begin, end))
    }
}
