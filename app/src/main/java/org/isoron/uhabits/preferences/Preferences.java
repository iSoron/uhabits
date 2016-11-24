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

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.models.*;

import java.util.*;

import javax.inject.*;

@AppScope
public class Preferences
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private final Context context;

    private SharedPreferences prefs;

    private Boolean shouldReverseCheckmarks = null;

    private LinkedList<Listener> listeners;

    @Inject
    public Preferences(@AppContext Context context)
    {
        this.context = context;
        listeners = new LinkedList<>();

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void addListener(Listener listener)
    {
        listeners.add(listener);
    }

    public Integer getDefaultHabitColor(int fallbackColor)
    {
        return prefs.getInt("pref_default_habit_palette_color", fallbackColor);
    }

    public HabitList.Order getDefaultOrder()
    {
        String name = prefs.getString("pref_default_order", "BY_POSITION");

        try
        {
            return HabitList.Order.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            setDefaultOrder(HabitList.Order.BY_POSITION);
            return HabitList.Order.BY_POSITION;
        }
    }

    public int getDefaultScoreSpinnerPosition()
    {
        int defaultScoreInterval = prefs.getInt("pref_score_view_interval", 1);
        if (defaultScoreInterval > 5 || defaultScoreInterval < 0)
            defaultScoreInterval = 1;
        return defaultScoreInterval;
    }

    public void setDefaultOrder(HabitList.Order order)
    {
        prefs.edit().putString("pref_default_order", order.name()).apply();
    }

    public void setDefaultScoreSpinnerPosition(int position)
    {
        prefs.edit().putInt("pref_score_view_interval", position).apply();
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

    public boolean getShowArchived()
    {
        return prefs.getBoolean("pref_show_archived", false);
    }

    public void setShowArchived(boolean showArchived)
    {
        prefs.edit().putBoolean("pref_show_archived", showArchived).apply();
    }

    public boolean getShowCompleted()
    {
        return prefs.getBoolean("pref_show_completed", true);
    }

    public void setShowCompleted(boolean showCompleted)
    {
        prefs.edit().putBoolean("pref_show_completed", showCompleted).apply();
    }

    public long getSnoozeInterval()
    {
        return Long.parseLong(prefs.getString("pref_snooze_interval", "15"));
    }

    public int getTheme()
    {
        return prefs.getInt("pref_theme", ThemeSwitcher.THEME_LIGHT);
    }

    public void setTheme(int theme)
    {
        prefs.edit().putInt("pref_theme", theme).apply();
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

    public boolean isPureBlackEnabled()
    {
        return prefs.getBoolean("pref_pure_black", false);
    }

    public boolean isShortToggleEnabled()
    {
        return prefs.getBoolean("pref_short_toggle", false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key)
    {
        if (key.equals("pref_checkmark_reverse_order"))
        {
            shouldReverseCheckmarks = null;
            for(Listener l : listeners) l.onCheckmarkOrderChanged();
        }

        if(key.equals("pref_sticky_notifications"))
        {
            for(Listener l : listeners) l.onNotificationsChanged();
        }
    }

    public void removeListener(Listener listener)
    {
        listeners.remove(listener);
    }

    public void setDefaultHabitColor(int color)
    {
        prefs.edit().putInt("pref_default_habit_palette_color", color).apply();
    }

    public void setShouldReverseCheckmarks(boolean reverse)
    {
        shouldReverseCheckmarks = null;
        prefs
            .edit()
            .putBoolean("pref_checkmark_reverse_order", reverse)
            .apply();

        for(Listener l : listeners) l.onCheckmarkOrderChanged();
    }

    public boolean shouldReverseCheckmarks()
    {
        if (shouldReverseCheckmarks == null) shouldReverseCheckmarks =
            prefs.getBoolean("pref_checkmark_reverse_order", false);

        return shouldReverseCheckmarks;
    }

    public boolean shouldMakeNotificationsSticky()
    {
        return prefs.getBoolean("pref_sticky_notifications", false);
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

    public interface Listener
    {
        default void onCheckmarkOrderChanged() {}

        default void onNotificationsChanged() {}
    }
}
