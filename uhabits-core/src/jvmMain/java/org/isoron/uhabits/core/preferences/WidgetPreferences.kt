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
package org.isoron.uhabits.core.preferences

import org.isoron.uhabits.core.AppScope
import javax.inject.Inject

@AppScope
class WidgetPreferences @Inject constructor(private val storage: Preferences.Storage) {
    fun addWidget(widgetId: Int, habitIds: LongArray) {
        storage.putLongArray(getHabitIdKey(widgetId), habitIds)
    }

    fun getHabitIdsFromWidgetId(widgetId: Int): LongArray {
        val habitIdKey = getHabitIdKey(widgetId)
        return try {
            storage.getLongArray(habitIdKey, longArrayOf())
        } catch (e: ClassCastException) {
            // Up to Loop 1.7.11, this preference was not an array, but a single
            // long. Trying to read the old preference causes a cast exception.
            when (val habitId = storage.getLong(habitIdKey, -1)) {
                -1L -> longArrayOf()
                else -> longArrayOf(habitId)
            }
        }
    }

    fun removeWidget(id: Int) {
        val habitIdKey = getHabitIdKey(id)
        storage.remove(habitIdKey)
    }

    fun getSnoozeTime(id: Long): Long {
        return storage.getLong(getSnoozeKey(id), 0)
    }

    private fun getHabitIdKey(id: Int): String {
        return String.format("widget-%06d-habit", id)
    }

    private fun getSnoozeKey(id: Long): String {
        return String.format("snooze-%06d", id)
    }

    fun removeSnoozeTime(id: Long) {
        storage.putLong(getSnoozeKey(id), 0)
    }

    fun setSnoozeTime(id: Long, time: Long) {
        storage.putLong(getSnoozeKey(id), time)
    }
}
