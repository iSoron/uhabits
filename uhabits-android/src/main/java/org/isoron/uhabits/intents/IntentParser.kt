/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

import android.content.ContentUris.parseId
import android.content.Intent
import android.net.Uri
import me.tatarka.inject.annotations.Inject
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList

@Inject
@AppScope
class IntentParser(
    private val habits: HabitList,
    private val habitGroups: HabitGroupList
) {

    fun parseCheckmarkIntent(intent: Intent): CheckmarkIntentData {
        val uri = intent.data ?: throw IllegalArgumentException("uri is null")
        return CheckmarkIntentData(parseHabit(uri), parseDate(intent))
    }

    fun copyIntentData(source: Intent, destination: Intent) {
        destination.data = source.data
        val todayMillis = getToday().unixTime
        destination.putExtra("timestamp", source.getLongExtra("timestamp", todayMillis))
    }

    private fun parseHabit(uri: Uri): Habit {
        return habits.getById(parseId(uri))
            ?: habitGroups.getHabitByID(parseId(uri))
            ?: throw IllegalArgumentException("habit not found")
    }

    private fun parseDate(intent: Intent): LocalDate {
        val todayMillis = getToday().unixTime
        var timestamp = intent.getLongExtra("timestamp", todayMillis)
        timestamp = LocalDate.fromUnixTime(timestamp).unixTime

        if (timestamp < 0 || timestamp > todayMillis) {
            throw IllegalArgumentException("timestamp is not valid")
        }

        return LocalDate.fromUnixTime(timestamp)
    }

    class CheckmarkIntentData(var habit: Habit, var date: LocalDate)
}
