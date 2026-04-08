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

import org.isoron.platform.time.LocalDate
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

    @Synchronized
    fun recompute(
        computedEntries: EntryList,
        from: LocalDate,
        to: LocalDate,
        isNumerical: Boolean,
        targetValue: Double,
        targetType: NumericalHabitType
    ) {
        list.clear()
        val dates = computedEntries
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
            .map { it.date }
            .toTypedArray()

        if (dates.isEmpty()) return

        var begin = dates[0]
        var end = dates[0]
        for (i in 1 until dates.size) {
            val current = dates[i]
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

    @Synchronized
    fun isInStreaks(date: LocalDate): Boolean {
        return list.any { it.isInStreak(date) }
    }

    @Synchronized
    fun combineFrom(
        habitList: HabitList,
        from: LocalDate,
        to: LocalDate
    ) {
        list.clear()
        if (habitList.isEmpty) return
        var current = from
        var streakRunning = false
        var streakStart = from
        val notArchivedHabits = habitList.filter { !it.isArchived }
        while (current <= to) {
            if (notArchivedHabits.all { it.streaks.isInStreaks(current) }) {
                if (!streakRunning) {
                    streakStart = current
                    streakRunning = true
                }
            } else {
                if (streakRunning) {
                    val streakEnd = current.minus(1)
                    list.add(Streak(streakStart, streakEnd))
                    streakRunning = false
                }
            }
            current = current.plus(1)
        }
        list.sortByDescending { it.start }
    }
}
