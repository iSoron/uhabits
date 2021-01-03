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

class HabitMatcherBuilder {
    private var archivedAllowed = false
    private var reminderRequired = false
    private var completedAllowed = true

    fun build(): HabitMatcher {
        return HabitMatcher(
            isArchivedAllowed = archivedAllowed,
            isReminderRequired = reminderRequired,
            isCompletedAllowed = completedAllowed,
        )
    }

    fun setArchivedAllowed(archivedAllowed: Boolean): HabitMatcherBuilder {
        this.archivedAllowed = archivedAllowed
        return this
    }

    fun setCompletedAllowed(completedAllowed: Boolean): HabitMatcherBuilder {
        this.completedAllowed = completedAllowed
        return this
    }

    fun setReminderRequired(reminderRequired: Boolean): HabitMatcherBuilder {
        this.reminderRequired = reminderRequired
        return this
    }
}
