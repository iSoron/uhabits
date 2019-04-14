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
import kotlin.math.*

class ScoreList(private val checkmarkList: CheckmarkList) {
    /**
     * Returns a list of all scores, from the beginning of the habit history
     * until the specified date.
     *
     * The interval is inclusive, and the list is sorted from newest to oldest.
     * That is, the first element of the returned list corresponds to the date
     * provided.
     */
    fun getUntil(date: LocalDate): List<Score> {
        val frequency = checkmarkList.frequency
        val checks = checkmarkList.getUntil(date)
        val scores = mutableListOf<Score>()
        val type = checkmarkList.habitType

        var currentScore = 0.0
        checks.reversed().forEach { check ->
            val value = if (type == HabitType.BOOLEAN_HABIT) {
                min(1, check.value)
            } else {
                check.value
            }
            currentScore = compute(frequency, currentScore, value)
            scores.add(Score(check.date, currentScore))
        }
        return scores.reversed()
    }

    fun getAt(date: LocalDate): Score {
        return getUntil(date)[0]
    }

    companion object {
        /**
         * Given the frequency of the habit, the previous score, and the value of
         * the current checkmark, computes the current score for the habit.
         */
        fun compute(frequency: Frequency,
                    previousScore: Double,
                    checkmarkValue: Int): Double {
            val multiplier = 0.5.pow(frequency.toDouble() / 13.0)
            val score =  previousScore * multiplier + checkmarkValue * (1 - multiplier)
            return floor(score * 1e6) / 1e6
        }
    }
}