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

/**
 * A Checkmark represents the completion status of the habit for a given day.
 *
 * While repetitions simply record that the habit was performed at a given date, a checkmark
 * provides more information, such as whether a repetition was expected at that day or not.
 *
 * Checkmarks are computed automatically from the list of repetitions.
 *
 * Note that the status comparator in relies on SKIP > YES_MANUAL > YES_AUTO > NO.
 */
data class Checkmark(
        val timestamp: Timestamp,
        val value: Int,
) {
    companion object {
        /**
         * Checkmark value indicating that the habit is not applicable for this timestamp.
         */
        const val SKIP = 3

        /**
         * Checkmark value indicating that the user has performed the habit at this timestamp.
         */
        const val YES_MANUAL = 2

        /**
         * Checkmark value indicating that the user did not perform the habit, but they were not
         * expected to, because of the frequency of the habit.
         */
        const val YES_AUTO = 1

        /**
         * Checkmark value indicating that the user did not perform the habit, even though they were
         * expected to perform it.
         */
        const val NO = 0

        /**
         * Checkmark value indicating that no data is available for the given timestamp.
         */
        const val UNKNOWN = -1
    }
}