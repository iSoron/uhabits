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

package org.isoron.uhabits.preferences;

import android.content.*;
import android.preference.*;
import android.support.annotation.*;

import org.isoron.androidbase.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.preferences.*;

import javax.inject.*;

@AppScope
public class SharedPreferencesStorage
    implements SharedPreferences.OnSharedPreferenceChangeListener,
               Preferences.Storage
{
    @NonNull
    private SharedPreferences sharedPrefs;

    @Nullable
    private Preferences preferences;

    @Inject
    public SharedPreferencesStorage(@AppContext Context context)
    {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
    }

    public void clear()
    {
        sharedPrefs.edit().clear().apply();
    }

    @Override
    public boolean getBoolean(String key, boolean defValue)
    {
        return sharedPrefs.getBoolean(key, defValue);
    }

    @Override
    public int getInt(String key, int defValue)
    {
        return sharedPrefs.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, int defValue)
    {
        return sharedPrefs.getLong(key, defValue);
    }

    @Override
    public String getString(String key, String defValue)
    {
        return sharedPrefs.getString(key, defValue);
    }

    @Override
    public void onAttached(Preferences preferences)
    {
        this.preferences = preferences;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key)
    {
        if(preferences == null) return;
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        switch (key)
        {
            case "pref_checkmark_reverse_order":
                preferences.setCheckmarkSequenceReversed(getBoolean(key, false));
                break;

            case "pref_sticky_notifications":
                preferences.setNotificationsSticky(getBoolean(key, false));
                break;

            case "pref_feature_sync":
                preferences.setSyncEnabled(getBoolean(key, false));
                break;
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void putBoolean(String key, boolean value)
    {
        sharedPrefs.edit().putBoolean(key, value).apply();
    }

    @Override
    public void putInt(String key, int value)
    {
        sharedPrefs.edit().putInt(key, value).apply();
    }

    @Override
    public void putLong(String key, long value)
    {
        sharedPrefs.edit().putLong(key, value).apply();
    }

    @Override
    public void putString(String key, String value)
    {
        sharedPrefs.edit().putString(key, value).apply();
    }

    @Override
    public void remove(String key)
    {
        sharedPrefs.edit().remove(key).apply();
    }
}
