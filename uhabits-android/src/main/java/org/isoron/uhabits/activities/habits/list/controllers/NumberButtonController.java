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

package org.isoron.uhabits.activities.habits.list.controllers;

import android.support.annotation.*;

import com.google.auto.factory.*;

import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;

@AutoFactory
public class NumberButtonController
{
    @Nullable
    private Listener listener;

    @NonNull
    private final Preferences prefs;

    @NonNull
    private Habit habit;

    private long timestamp;

    public NumberButtonController(@Provided @NonNull Preferences prefs,
                                  @NonNull Habit habit,
                                  long timestamp)
    {
        this.habit = habit;
        this.timestamp = timestamp;
        this.prefs = prefs;
    }

    public void onClick()
    {
        if (prefs.isShortToggleEnabled()) performEdit();
        else performInvalidToggle();
    }

    public boolean onLongClick()
    {
        performEdit();
        return true;
    }

    public void performInvalidToggle()
    {
        if (listener != null) listener.onInvalidEdit();
    }

    public void performEdit()
    {
        if (listener != null) listener.onEdit(habit, timestamp);
    }

    public void setListener(@Nullable Listener listener)
    {
        this.listener = listener;
    }

    public interface Listener
    {
        /**
         * Called when the user's attempt to edit the value is rejected.
         */
        void onInvalidEdit();

        /**
         * Called when a the user's attempt to edit the value has been accepted.
         * @param habit the habit being edited
         * @param timestamp the timestamp being edited
         */
        void onEdit(@NonNull Habit habit, long timestamp);
    }
}
