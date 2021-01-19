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

import org.isoron.uhabits.core.AppScope;

import javax.inject.Inject;

@AppScope
public class WidgetPreferences {
    private Preferences.Storage storage;

    @Inject
    public WidgetPreferences(Preferences.Storage storage) {
        this.storage = storage;
    }

    public void addWidget(int widgetId, long[] habitIds) {
        storage.putLongArray(getHabitIdKey(widgetId), habitIds);
    }

    public long[] getHabitIdsFromWidgetId(int widgetId) {
        long[] habitIds;
        String habitIdKey = getHabitIdKey(widgetId);
        try {
            habitIds = storage.getLongArray(habitIdKey, new long[]{-1});
        } catch (ClassCastException e) {
            // Up to Loop 1.7.11, this preference was not an array, but a single
            // long. Trying to read the old preference causes a cast exception.
            habitIds = new long[1];
            habitIds[0] = storage.getLong(habitIdKey, -1);
            storage.putLongArray(habitIdKey, habitIds);
        }
        return habitIds;
    }

    public void removeWidget(int id) {
        String habitIdKey = getHabitIdKey(id);
        storage.remove(habitIdKey);
    }

    public long getSnoozeTime(long id)
    {
        return storage.getLong(getSnoozeKey(id), 0);
    }

    private String getHabitIdKey(int id) {
        return String.format("widget-%06d-habit", id);
    }

    private String getSnoozeKey(long id)
    {
        return String.format("snooze-%06d", id);
    }

    public void removeSnoozeTime(long id)
    {
        storage.putLong(getSnoozeKey(id), 0);
    }

    public void setSnoozeTime(Long id, long time)
    {
        storage.putLong(getSnoozeKey(id), time);
    }
}
