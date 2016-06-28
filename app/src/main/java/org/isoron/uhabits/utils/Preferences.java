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

package org.isoron.uhabits.utils;

import android.content.*;
import android.preference.*;

import org.isoron.uhabits.*;

public class Preferences
{
    private Context context;

    private SharedPreferences prefs;

    public Preferences()
    {
        this.context = HabitsApplication.getContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Integer getDefaultHabitColor(int fallbackColor)
    {
        return prefs.getInt("pref_default_habit_palette_color", fallbackColor);
    }

    /**
     * Returns the number of the last hint shown to the user.
     *
     * @return number of last hint shown
     */
    public int getLastHintNumber()
    {
        return prefs.getInt("last_hint_number", -1);
    }

    /**
     * Returns the time when the last hint was shown to the user.
     *
     * @return timestamp of the day the last hint was shown
     */
    public long getLastHintTimestamp()
    {
        return prefs.getLong("last_hint_timestamp", -1);
    }

    public void incrementLaunchCount()
    {
        int count = prefs.getInt("launch_count", 0);
        prefs.edit().putInt("launch_count", count + 1).apply();
    }

    public void initialize()
    {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
    }

    public boolean isFirstRun()
    {
        return prefs.getBoolean("pref_first_run", true);
    }

    public void setFirstRun(boolean isFirstRun)
    {
        prefs.edit().putBoolean("pref_first_run", isFirstRun).apply();
    }

    public boolean isShortToggleEnabled()
    {
        return prefs.getBoolean("pref_short_toggle", false);
    }

    public void setShortToggleEnabled(boolean enabled)
    {
        prefs.edit().putBoolean("pref_short_toggle", enabled).apply();
    }

    public void setDefaultHabitColor(int color)
    {
        prefs.edit().putInt("pref_default_habit_palette_color", color).apply();
    }

    public void setShouldReverseCheckmarks(boolean shouldReverse)
    {
        prefs
            .edit()
            .putBoolean("pref_checkmark_reverse_order", shouldReverse)
            .apply();
    }

    public boolean shouldReverseCheckmarks()
    {
        return prefs.getBoolean("pref_checkmark_reverse_order", false);
    }

    public void updateLastAppVersion()
    {
        prefs.edit().putInt("last_version", BuildConfig.VERSION_CODE).apply();
    }

    /**
     * Sets the last hint shown to the user, and the time that it was shown.
     *
     * @param number    number of the last hint shown
     * @param timestamp timestamp for the day the last hint was shown
     */
    public void updateLastHint(int number, long timestamp)
    {
        prefs
            .edit()
            .putInt("last_hint_number", number)
            .putLong("last_hint_timestamp", timestamp)
            .apply();
    }
}
