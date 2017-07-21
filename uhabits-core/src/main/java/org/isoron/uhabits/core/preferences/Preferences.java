/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.preferences;

import android.support.annotation.*;

import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.*;

import java.util.*;

public class Preferences
{
    @NonNull
    private final Storage storage;

    @NonNull
    private List<Listener> listeners;

    @Nullable
    private Boolean shouldReverseCheckmarks = null;

    public Preferences(@NonNull Storage storage)
    {
        this.storage = storage;
        listeners = new LinkedList<>();
        storage.onAttached(this);
    }

    public void addListener(Listener listener)
    {
        listeners.add(listener);
    }

    public Integer getDefaultHabitColor(int fallbackColor)
    {
        return storage.getInt("pref_default_habit_palette_color",
            fallbackColor);
    }

    public HabitList.Order getDefaultOrder()
    {
        String name = storage.getString("pref_default_order", "BY_POSITION");

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

    public void setDefaultOrder(HabitList.Order order)
    {
        storage.putString("pref_default_order", order.name());
    }

    public int getDefaultScoreSpinnerPosition()
    {
        int defaultScoreInterval =
            storage.getInt("pref_score_view_interval", 1);
        if (defaultScoreInterval > 5 || defaultScoreInterval < 0)
            defaultScoreInterval = 1;
        return defaultScoreInterval;
    }

    public void setDefaultScoreSpinnerPosition(int position)
    {
        storage.putInt("pref_score_view_interval", position);
    }

    public int getLastHintNumber()
    {
        return storage.getInt("last_hint_number", -1);
    }

    public long getLastHintTimestamp()
    {
        return storage.getLong("last_hint_timestamp", -1);
    }

    public long getLastSync()
    {
        return storage.getLong("last_sync", 0);
    }

    public void setLastSync(long timestamp)
    {
        storage.putLong("last_sync", timestamp);
    }

    public boolean getShowArchived()
    {
        return storage.getBoolean("pref_show_archived", false);
    }

    public void setShowArchived(boolean showArchived)
    {
        storage.putBoolean("pref_show_archived", showArchived);
    }

    public boolean getShowCompleted()
    {
        return storage.getBoolean("pref_show_completed", true);
    }

    public void setShowCompleted(boolean showCompleted)
    {
        storage.putBoolean("pref_show_completed", showCompleted);
    }

    public long getSnoozeInterval()
    {
        return Long.parseLong(storage.getString("pref_snooze_interval", "15"));
    }

    public String getSyncAddress()
    {
        return storage.getString("pref_sync_address",
            "https://sync.loophabits.org:4000");
    }

    public void setSyncAddress(String address)
    {
        storage.putString("pref_sync_address", address);
        for (Listener l : listeners) l.onSyncFeatureChanged();
    }

    public String getSyncClientId()
    {
        String id = storage.getString("pref_sync_client_id", "");
        if (!id.isEmpty()) return id;

        id = UUID.randomUUID().toString();
        storage.putString("pref_sync_client_id", id);
        return id;
    }

    public String getSyncKey()
    {
        return storage.getString("pref_sync_key", "");
    }

    public int getTheme()
    {
        return storage.getInt("pref_theme", ThemeSwitcher.THEME_LIGHT);
    }

    public void setTheme(int theme)
    {
        storage.putInt("pref_theme", theme);
    }

    public void incrementLaunchCount()
    {
        int count = storage.getInt("launch_count", 0);
        storage.putInt("launch_count", count + 1);
    }

    public boolean isDeveloper()
    {
        return storage.getBoolean("pref_developer", false);
    }

    public void setDeveloper(boolean isDeveloper)
    {
        storage.putBoolean("pref_developer", isDeveloper);
    }

    public boolean isFirstRun()
    {
        return storage.getBoolean("pref_first_run", true);
    }

    public void setFirstRun(boolean isFirstRun)
    {
        storage.putBoolean("pref_first_run", isFirstRun);
    }

    public boolean isNumericalHabitsFeatureEnabled()
    {
        return storage.getBoolean("pref_feature_numerical_habits", false);
    }

    public boolean isPureBlackEnabled()
    {
        return storage.getBoolean("pref_pure_black", false);
    }

    public boolean isShortToggleEnabled()
    {
        return storage.getBoolean("pref_short_toggle", false);
    }

    public void setShortToggleEnabled(boolean enabled)
    {
        storage.putBoolean("pref_short_toggle", enabled);
    }

    public boolean isSyncEnabled()
    {
        return storage.getBoolean("pref_feature_sync", false);
    }

    public void removeListener(Listener listener)
    {
        listeners.remove(listener);
    }

    public void reset()
    {
        storage.clear();
    }

    public void setDefaultHabitColor(int color)
    {
        storage.putInt("pref_default_habit_palette_color", color);
    }

    public void setLastAppVersion(int version)
    {
        storage.putInt("last_version", version);
    }

    public void setNotificationsSticky(boolean sticky)
    {
        storage.getBoolean("pref_sticky_notifications", sticky);
        for (Listener l : listeners) l.onNotificationsChanged();
    }

    public void setCheckmarkSequenceReversed(boolean reverse)
    {
        shouldReverseCheckmarks = reverse;
        storage.putBoolean("pref_checkmark_reverse_order", reverse);
        for (Listener l : listeners) l.onCheckmarkSequenceChanged();
    }

    public void setSyncEnabled(boolean isEnabled)
    {
        storage.putBoolean("pref_feature_sync", isEnabled);
        for(Listener l : listeners) l.onSyncFeatureChanged();
    }

    public boolean shouldMakeNotificationsSticky()
    {
        return storage.getBoolean("pref_sticky_notifications", false);
    }

    public boolean isCheckmarkSequenceReversed()
    {
        if (shouldReverseCheckmarks == null) shouldReverseCheckmarks =
            storage.getBoolean("pref_checkmark_reverse_order", false);

        return shouldReverseCheckmarks;
    }

    public void updateLastHint(int number, long timestamp)
    {
        storage.putInt("last_hint_number", number);
        storage.putLong("last_hint_timestamp", timestamp);
    }

    public interface Listener
    {
        default void onCheckmarkSequenceChanged() {}

        default void onNotificationsChanged() {}

        default void onSyncFeatureChanged() {}
    }

    public interface Storage
    {
        void clear();

        boolean getBoolean(String key, boolean defValue);

        int getInt(String key, int defValue);

        long getLong(String key, int defValue);

        String getString(String key, String defValue);

        void onAttached(Preferences preferences);

        void putBoolean(String key, boolean value);

        void putInt(String key, int value);

        void putLong(String key, long value);

        void putString(String key, String value);

        void remove(String key);
    }
}
