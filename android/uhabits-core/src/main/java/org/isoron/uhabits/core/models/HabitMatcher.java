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

package org.isoron.uhabits.core.models;

public class HabitMatcher
{
    public static final HabitMatcher WITH_ALARM = new HabitMatcherBuilder()
        .setArchivedAllowed(true)
        .setReminderRequired(true)
        .build();

    private final boolean archivedAllowed;

    private final boolean reminderRequired;

    private final boolean completedAllowed;

    public HabitMatcher(boolean allowArchived,
                        boolean reminderRequired,
                        boolean completedAllowed)
    {
        this.archivedAllowed = allowArchived;
        this.reminderRequired = reminderRequired;
        this.completedAllowed = completedAllowed;
    }

    public boolean isArchivedAllowed()
    {
        return archivedAllowed;
    }

    public boolean isCompletedAllowed()
    {
        return completedAllowed;
    }

    public boolean isReminderRequired()
    {
        return reminderRequired;
    }

    public boolean matches(Habit habit)
    {
        if (!isArchivedAllowed() && habit.isArchived()) return false;
        if (isReminderRequired() && !habit.hasReminder()) return false;
        return isCompletedAllowed() || !habit.isCompletedToday();
    }
}
