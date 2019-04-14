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

data class Checkmark(var date: LocalDate,
                     var value: Int) {

    companion object {
        /**
         * Value assigned when the user has explicitly marked the habit as
         * completed.
         */
        const val CHECKED_MANUAL = 2

        /**
         * Value assigned when the user has not explicitly marked the habit as
         * completed, however, due to the frequency of the habit, an automatic
         * checkmark was added.
         */
        const val CHECKED_AUTOMATIC = 1

        /**
         * Value assigned when the user has not completed the habit, and the app
         * has not automatically a checkmark.
         */
        const val UNCHECKED = 0
    }
}
