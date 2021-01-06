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

data class Frequency(
    var numerator: Int,
    var denominator: Int,
) {
    init {
        if (numerator == denominator) {
            denominator = 1
            numerator = 1
        }
    }

    fun toDouble(): Double {
        return numerator.toDouble() / denominator
    }

    companion object {
        @JvmField
        val DAILY = Frequency(1, 1)

        @JvmField
        val THREE_TIMES_PER_WEEK = Frequency(3, 7)

        @JvmField
        val TWO_TIMES_PER_WEEK = Frequency(2, 7)

        @JvmField
        val WEEKLY = Frequency(1, 7)
    }
}
