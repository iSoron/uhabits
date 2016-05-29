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

package org.isoron.uhabits.ui.habits.list.model;

import android.support.annotation.NonNull;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.Preferences;

import javax.inject.Inject;

/**
 * Provides a list of hints to be shown at the application startup, and takes
 * care of deciding when a new hint should be shown.
 */
public class HintList
{
    @Inject
    Preferences prefs;

    @NonNull
    private final String[] hints;

    /**
     * Constructs a new list containing the provided hints.
     *
     * @param hints initial list of hints
     */
    public HintList(@NonNull String hints[])
    {
        this.hints = hints;
        HabitsApplication.getComponent().inject(this);
    }

    /**
     * Returns a new hint to be shown to the user.
     * <p>
     * The hint returned is marked as read on the list, and will not be returned
     * again. In case all hints have already been read, and there is nothing
     * left, returns null.
     *
     * @return the next hint to be shown, or null if none
     */
    public String pop()
    {
        int next = prefs.getLastHintNumber() + 1;
        if (next >= hints.length) return null;

        prefs.updateLastHint(next, DateUtils.getStartOfToday());
        return hints[next];
    }

    /**
     * Returns whether it is time to show a new hint or not.
     *
     * @return true if hint should be shown, false otherwise
     */
    public boolean shouldShow()
    {
        long lastHintTimestamp = prefs.getLastHintTimestamp();
        return (DateUtils.getStartOfToday() > lastHintTimestamp);
    }
}
