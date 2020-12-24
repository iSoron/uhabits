/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

data class Entry(
        val timestamp: Timestamp,
        val value: Int,
) {
    companion object {
        /**
         * Value indicating that the habit is not applicable for this timestamp.
         */
        const val SKIP = 3

        /**
         * Value indicating that the user has performed the habit at this timestamp.
         */
        const val YES_MANUAL = 2

        /**
         * Value indicating that the user did not perform the habit, but they were not
         * expected to, because of the frequency of the habit.
         */
        const val YES_AUTO = 1

        /**
         * Value indicating that the user did not perform the habit, even though they were
         * expected to perform it.
         */
        const val NO = 0

        /**
         * Value indicating that no data is available for the given timestamp.
         */
        const val UNKNOWN = -1

        fun nextToggleValueWithSkip(value: Int): Int {
            return when (value) {
                NO, UNKNOWN, YES_AUTO -> YES_MANUAL
                YES_MANUAL -> SKIP
                SKIP -> NO
                else -> NO
            }
        }

        fun nextToggleValueWithoutSkip(value: Int): Int {
            return when (value) {
                NO, UNKNOWN, YES_AUTO -> YES_MANUAL
                else -> NO
            }
        }

    }
}