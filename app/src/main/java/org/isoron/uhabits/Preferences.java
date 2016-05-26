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

package org.isoron.uhabits;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences
{
    private static Preferences singleton;

    private Context context;
    private SharedPreferences prefs;

    private Preferences()
    {
        this.context = HabitsApplication.getContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Preferences getInstance()
    {
        if(singleton == null) singleton = new Preferences();
        return singleton;
    }

    public void initialize()
    {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
    }


    public void incrementLaunchCount()
    {
        int count = prefs.getInt("launch_count", 0);
        prefs.edit().putInt("launch_count", count + 1).apply();
    }

    public void updateLastAppVersion()
    {
        prefs.edit().putInt("last_version", BuildConfig.VERSION_CODE).apply();
    }

    public boolean isFirstRun()
    {
        return prefs.getBoolean("pref_first_run", true);
    }

    public void setFirstRun(boolean isFirstRun)
    {
        prefs.edit().putBoolean("pref_first_run", isFirstRun).apply();
    }

    public void setLastHintTimestamp(long timestamp)
    {
        prefs.edit().putLong("last_hint_timestamp", timestamp).apply();
    }

    public boolean isShortToggleEnabled()
    {
        return prefs.getBoolean("pref_short_toggle", false);
    }
}
