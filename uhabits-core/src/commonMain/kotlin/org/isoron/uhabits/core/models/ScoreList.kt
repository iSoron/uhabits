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

import org.isoron.platform.Synchronized
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.models.Score.Companion.compute
import kotlin.math.max
import kotlin.math.min

class ScoreList {

    private val map = mutableMapOf<LocalDate, Score>()

    /**
     * Returns the score for a given day. If the date given happens before the first
     * repetition of the habit or after the last computed score, returns a score with value zero.
     */
    @Synchronized
    operator fun get(date: LocalDate): Score {
        return map[date] ?: Score(date, 0.0)
    }

    /**
     * Returns the list of scores that fall within the given interval.
     *
     * There is exactly one score per day in the interval. The endpoints of the interval are
     * included. The list is ordered by date (decreasing). That is, the first score
     * corresponds to the newest date, and the last score corresponds to the oldest date.
     */
    @Synchronized
    fun getByInterval(
        from: LocalDate,
        to: LocalDate
    ): List<Score> {
        val result: MutableList<Score> = mutableListOf()
        if (from.isNewerThan(to)) return result
        var current = to
        while (!current.isOlderThan(from)) {
            result.add(get(current))
            current = current.minus(1)
        }
        return result
    }

    /**
     * Recomputes all scores between the provided [from] and [to] dates.
     */
    @Synchronized
    fun recompute(
        frequency: Frequency,
        isNumerical: Boolean,
        numericalHabitType: NumericalHabitType,
        targetValue: Double,
        computedEntries: EntryList,
        from: LocalDate,
        to: LocalDate
    ) {
        map.clear()
        var rollingSum = 0.0
        var numerator = frequency.numerator
        var denominator = frequency.denominator
        val freq = frequency.toDouble()
        val values = computedEntries.getByInterval(from, to).map { it.value }.toIntArray()
        val isAtMost = numericalHabitType == NumericalHabitType.AT_MOST

        // For non-daily boolean habits, we double the numerator and the denominator to smooth
        // out irregular repetition schedules (for example, weekly habits performed on different
        // days of the week)
        if (!isNumerical && freq < 1.0) {
            numerator *= 2
            denominator *= 2
        }

        val window = ArrayDeque<Int>()
        var previousValue = if (isNumerical && isAtMost) 1.0 else 0.0
        for (i in values.indices) {
            val offset = values.size - i - 1
            val value = values[offset]
            if (value != Entry.SKIP) {
                window.addLast(value)
                if (window.size > denominator) {
                    val oldest = window.removeFirst()
                    if (isNumerical) {
                        rollingSum -= max(0, oldest)
                    } else if (oldest == Entry.YES_MANUAL) {
                        rollingSum -= 1.0
                    }
                }

                if (isNumerical) {
                    rollingSum += max(0, value)
                    val normalizedRollingSum = rollingSum / 1000
                    val percentageCompleted = if (!isAtMost) {
                        if (targetValue > 0) {
                            min(1.0, normalizedRollingSum / targetValue)
                        } else {
                            1.0
                        }
                    } else {
                        if (targetValue > 0) {
                            (1 - ((normalizedRollingSum - targetValue) / targetValue)).coerceIn(
                                0.0,
                                1.0
                            )
                        } else {
                            if (normalizedRollingSum > 0) 0.0 else 1.0
                        }
                    }

                    previousValue = compute(freq, previousValue, percentageCompleted)
                } else {
                    if (value == Entry.YES_MANUAL) {
                        rollingSum += 1.0
                    }
                    val percentageCompleted = min(1.0, rollingSum / numerator)
                    previousValue = compute(freq, previousValue, percentageCompleted)
                }
            }
            val date = from.plus(i)
            map[date] = Score(date, previousValue)
        }
    }
}
