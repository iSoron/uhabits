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

package org.isoron.uhabits.intents

import android.content.*
import android.content.ContentUris.*
import android.net.*
import org.isoron.uhabits.core.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.utils.*
import javax.inject.*

@AppScope
class IntentParser
@Inject constructor(private val habits: HabitList) {

    fun parseCheckmarkIntent(intent: Intent): CheckmarkIntentData {
        val uri = intent.data ?: throw IllegalArgumentException("uri is null")
        return CheckmarkIntentData(parseHabit(uri), parseTimestamp(intent))
    }

    private fun parseHabit(uri: Uri): Habit {
        val habit = habits.getById(parseId(uri)) ?:
                    throw IllegalArgumentException("habit not found")
        return habit
    }

    private fun parseTimestamp(intent: Intent): Long {
        val today = DateUtils.getStartOfToday()
        var timestamp = intent.getLongExtra("timestamp", today)
        timestamp = DateUtils.getStartOfDay(timestamp)

        if (timestamp < 0 || timestamp > today)
            throw IllegalArgumentException("timestamp is not valid")

        return timestamp
    }

    class CheckmarkIntentData(var habit: Habit, var timestamp: Long)
}
