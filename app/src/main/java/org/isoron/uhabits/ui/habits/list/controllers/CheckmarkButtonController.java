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

package org.isoron.uhabits.ui.habits.list.controllers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.habits.list.views.CheckmarkButtonView;
import org.isoron.uhabits.utils.Preferences;

import javax.inject.Inject;

public class CheckmarkButtonController
{
    @Nullable
    private CheckmarkButtonView view;

    @Nullable
    private Listener listener;

    @Inject
    Preferences prefs;

    @NonNull
    private Habit habit;

    private long timestamp;

    public CheckmarkButtonController(@NonNull Habit habit, long timestamp)
    {
        this.habit = habit;
        this.timestamp = timestamp;
        HabitsApplication.getComponent().inject(this);
    }

    public void onClick()
    {
        if (prefs.isShortToggleEnabled()) performToggle();
        else performInvalidToggle();
    }

    public boolean onLongClick()
    {
        performToggle();
        return true;
    }

    public void performInvalidToggle()
    {
        if (listener != null) listener.onInvalidToggle();
    }

    public void performToggle()
    {
        if (view != null) view.toggle();
        if (listener != null) listener.onToggle(habit, timestamp);
    }

    public void setListener(@Nullable Listener listener)
    {
        this.listener = listener;
    }

    public void setView(@Nullable CheckmarkButtonView view)
    {
        this.view = view;
    }

    public interface Listener
    {
        /**
         * Called when the user's attempt to perform a toggle is rejected.
         */
        void onInvalidToggle();


        void onToggle(@NonNull Habit habit, long timestamp);
    }
}
