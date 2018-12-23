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

package org.isoron.uhabits.core.preferences;

import org.isoron.uhabits.core.AppScope;
import org.isoron.uhabits.core.models.HabitNotFoundException;

import javax.inject.Inject;

@AppScope
public class WidgetPreferences {
    private Preferences.Storage storage;

    @Inject
    public WidgetPreferences(Preferences.Storage storage) {
        this.storage = storage;
    }

    public void addWidget(int widgetId, long habitIds[]) {
        storage.putLongArray(getHabitIdKey(widgetId), habitIds);
    }

    public long[] getHabitIdsFromWidgetId(int widgetId) {
        long habitIds[] = storage.getLongArray(getHabitIdKey(widgetId));
        if (habitIds.length == 0) throw new HabitNotFoundException();
        return habitIds;
    }

    public void removeWidget(int id) {
        String habitIdKey = getHabitIdKey(id);
        storage.remove(habitIdKey);
    }

    private String getHabitIdKey(int id) {
        return String.format("widget-%06d-habit", id);
    }
}
