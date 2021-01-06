/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

import androidx.annotation.*;

import org.isoron.platform.time.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.*;
import org.isoron.uhabits.core.utils.*;

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

    public HabitList.Order getDefaultPrimaryOrder()
    {
        String name = storage.getString("pref_default_order", "BY_POSITION");

        try
        {
            return HabitList.Order.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            setDefaultPrimaryOrder(HabitList.Order.BY_POSITION);
            return HabitList.Order.BY_POSITION;
        }
    }

    public HabitList.Order getDefaultSecondaryOrder() {
        String name = storage.getString("pref_default_secondary_order", "BY_NAME_ASC");

        try
        {
            return HabitList.Order.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            setDefaultSecondaryOrder(HabitList.Order.BY_NAME_ASC);
            return HabitList.Order.BY_POSITION;
        }
    }

    public void setDefaultPrimaryOrder(HabitList.Order order)
    {
        storage.putString("pref_default_order", order.name());
    }

    public void setDefaultSecondaryOrder(HabitList.Order order)
    {
        storage.putString("pref_default_secondary_order", order.name());
    }

    public int getScoreCardSpinnerPosition()
    {
        return Math.min(4, Math.max(0, storage.getInt("pref_score_view_interval", 1)));
    }

    public void setScoreCardSpinnerPosition(int position)
    {
        storage.putInt("pref_score_view_interval", position);
    }

    public int getBarCardBoolSpinnerPosition()
    {
        return Math.min(3, Math.max(0, storage.getInt("pref_bar_card_bool_spinner", 0)));
    }

    public void setBarCardBoolSpinnerPosition(int position)
    {
        storage.putInt("pref_bar_card_bool_spinner", position);
    }

    public int getBarCardNumericalSpinnerPosition()
    {
        return Math.min(4, Math.max(0, storage.getInt("pref_bar_card_numerical_spinner", 0)));
    }

    public void setBarCardNumericalSpinnerPosition(int position)
    {
        storage.putInt("pref_bar_card_numerical_spinner", position);
    }

    public int getLastHintNumber()
    {
        return storage.getInt("last_hint_number", -1);
    }

    public Timestamp getLastHintTimestamp()
    {
        long unixTime = storage.getLong("last_hint_timestamp", -1);
        if (unixTime < 0) return null;
        else return new Timestamp(unixTime);
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

    public void setSnoozeInterval(int interval)
    {
        storage.putString("pref_snooze_interval", String.valueOf(interval));
    }

    public int getTheme()
    {
        return storage.getInt("pref_theme", ThemeSwitcher.THEME_AUTOMATIC);
    }

    public void setTheme(int theme)
    {
        storage.putInt("pref_theme", theme);
    }

    public void incrementLaunchCount()
    {
        storage.putInt("launch_count", getLaunchCount() + 1);
    }

    public int getLaunchCount()
    {
        return storage.getInt("launch_count", 0);
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

    public boolean isPureBlackEnabled()
    {
        return storage.getBoolean("pref_pure_black", false);
    }

    public void setPureBlackEnabled(boolean enabled)
    {
        storage.putBoolean("pref_pure_black", enabled);
    }

    public boolean isShortToggleEnabled()
    {
        return storage.getBoolean("pref_short_toggle", false);
    }

    public void setShortToggleEnabled(boolean enabled)
    {
        storage.putBoolean("pref_short_toggle", enabled);
    }

    public void removeListener(Listener listener)
    {
        listeners.remove(listener);
    }

    public void clear()
    {
        storage.clear();
    }

    public void setDefaultHabitColor(int color)
    {
        storage.putInt("pref_default_habit_palette_color", color);
    }

    public void setNotificationsSticky(boolean sticky)
    {
        storage.putBoolean("pref_sticky_notifications", sticky);
        for (Listener l : listeners) l.onNotificationsChanged();
    }

    public void setNotificationsLed(boolean enabled)
    {
        storage.putBoolean("pref_led_notifications", enabled);
        for (Listener l : listeners) l.onNotificationsChanged();
    }

    public boolean shouldMakeNotificationsSticky()
    {
        return storage.getBoolean("pref_sticky_notifications", false);
    }

    public boolean shouldMakeNotificationsLed()
    {
        return storage.getBoolean("pref_led_notifications", false);
    }

    public boolean isCheckmarkSequenceReversed()
    {
        if (shouldReverseCheckmarks == null) shouldReverseCheckmarks =
            storage.getBoolean("pref_checkmark_reverse_order", false);

        return shouldReverseCheckmarks;
    }

    public void setCheckmarkSequenceReversed(boolean reverse)
    {
        shouldReverseCheckmarks = reverse;
        storage.putBoolean("pref_checkmark_reverse_order", reverse);
        for (Listener l : listeners) l.onCheckmarkSequenceChanged();
    }

    public void updateLastHint(int number, Timestamp timestamp)
    {
        storage.putInt("last_hint_number", number);
        storage.putLong("last_hint_timestamp", timestamp.getUnixTime());
    }

    public int getLastAppVersion()
    {
        return storage.getInt("last_version", 0);
    }

    public void setLastAppVersion(int version)
    {
        storage.putInt("last_version", version);
    }

    public int getWidgetOpacity()
    {
        return Integer.parseInt(storage.getString("pref_widget_opacity", "255"));
    }

    public void setWidgetOpacity(int value)
    {
        storage.putString("pref_widget_opacity", Integer.toString(value));
    }

    public boolean isSkipEnabled()
    {
        return storage.getBoolean("pref_skip_enabled", false);
    }

    public void setSkipEnabled(boolean value)
    {
        storage.putBoolean("pref_skip_enabled", value);
    }

    public String getSyncBaseURL()
    {
        return storage.getString("pref_sync_base_url", "");
    }

    public String getSyncKey()
    {
        return storage.getString("pref_sync_key", "");
    }

    public String getEncryptionKey()
    {
        return storage.getString("pref_encryption_key", "");
    }

    public boolean isSyncEnabled()
    {
        return storage.getBoolean("pref_sync_enabled", false);
    }

    public void enableSync(String syncKey, String encKey)
    {
        storage.putBoolean("pref_sync_enabled", true);
        storage.putString("pref_sync_key", syncKey);
        storage.putString("pref_encryption_key", encKey);
        for (Listener l : listeners) l.onSyncEnabled();
    }

    public void disableSync()
    {
        storage.putBoolean("pref_sync_enabled", false);
        storage.putString("pref_sync_key", "");
        storage.putString("pref_encryption_key", "");
    }

    public boolean areQuestionMarksEnabled()
    {
        return storage.getBoolean("pref_unknown_enabled", false);
    }

    /**
     * @return An integer representing the first day of the week. Sunday
     * corresponds to 1, Monday to 2, and so on, until Saturday, which is
     * represented by 7. By default, this is based on the current system locale,
     * unless the user changed this in the settings.
     */
    @Deprecated()
    public int getFirstWeekdayInt()
    {
        String weekday = storage.getString("pref_first_weekday", "");
        if (weekday.isEmpty()) return DateUtils.getFirstWeekdayNumberAccordingToLocale();
        return Integer.parseInt(weekday);
    }

    public DayOfWeek getFirstWeekday()
    {
        int weekday = Integer.parseInt(storage.getString("pref_first_weekday", "-1"));
        if (weekday < 0) weekday = DateUtils.getFirstWeekdayNumberAccordingToLocale();
        switch (weekday) {
            case 1: return DayOfWeek.SUNDAY;
            case 2: return DayOfWeek.MONDAY;
            case 3: return DayOfWeek.TUESDAY;
            case 4: return DayOfWeek.WEDNESDAY;
            case 5: return DayOfWeek.THURSDAY;
            case 6: return DayOfWeek.FRIDAY;
            case 7: return DayOfWeek.SATURDAY;
            default: throw new IllegalArgumentException();
        }
    }

    public interface Listener
    {
        default void onCheckmarkSequenceChanged()
        {
        }

        default void onNotificationsChanged()
        {
        }

        default void onSyncEnabled()
        {
        }
    }

    public interface Storage
    {
        void clear();

        boolean getBoolean(String key, boolean defValue);

        int getInt(String key, int defValue);

        long getLong(String key, long defValue);

        String getString(String key, String defValue);

        void onAttached(Preferences preferences);

        void putBoolean(String key, boolean value);

        void putInt(String key, int value);

        void putLong(String key, long value);

        void putString(String key, String value);

        void remove(String key);

        default void putLongArray(String key, long[] values)
        {
            putString(key, StringUtils.joinLongs(values));
        }

        default long[] getLongArray(String key, long[] defValue)
        {
            String string = getString(key, "");
            if (string.isEmpty()) return defValue;
            else return StringUtils.splitLongs(string);
        }
    }
}
