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
package org.isoron.uhabits.widgets

import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils

internal object WidgetHabitStats {

    fun computeCurrentStreakDays(habit: Habit, today: Timestamp): Int {
        val todayValue = habit.computedEntries.get(today).value
        val isTodayEntered = todayValue != Entry.UNKNOWN

        val streakEnd: Timestamp = when {
            isSuccessOn(habit, today) -> today
            !isTodayEntered -> today.minus(1)
            else -> return 0
        }

        var length = 0
        var current = streakEnd
        while (length < 3650 && isSuccessOn(habit, current)) {
            length++
            current = current.minus(1)
        }

        return length
    }

    /**
     * Seven values (oldest -> newest), ending at [today].
     */
    fun computeWeeklySuccess(habit: Habit, today: Timestamp): BooleanArray {
        val result = BooleanArray(7)
        for (i in 0 until 7) {
            val day = today.minus(6 - i)
            result[i] = isSuccessOn(habit, day)
        }
        return result
    }

    /**
     * Returns the next reminder time (UTC millis) that should actually show, respecting the
     * reminder weekday selection and the user's start-of-day offset.
     */
    fun computeNextReminderTimeUtcMillis(habit: Habit, nowUtcMillis: Long): Long? {
        val reminder = habit.reminder ?: return null
        val allowedDays = if (reminder.days.isEmpty) {
            BooleanArray(7) { true }
        } else {
            reminder.days.toArray()
        }

        val nowLocal = DateUtils.getLocalTime(nowUtcMillis)
        val todayStartLocal = DateUtils.getStartOfDay(nowLocal)

        // Search up to two weeks ahead to be safe.
        for (d in 0..13) {
            val dayStartLocal = todayStartLocal + d * DateUtils.DAY_LENGTH
            val candidateLocal =
                dayStartLocal + reminder.hour * DateUtils.HOUR_LENGTH + reminder.minute * DateUtils.MINUTE_LENGTH

            val candidateUtc = DateUtils.applyTimezone(candidateLocal)
            if (candidateUtc <= nowUtcMillis) continue

            val habitDayStartLocal = DateUtils.getStartOfDayWithOffset(DateUtils.removeTimezone(candidateUtc))
            val weekday = Timestamp(habitDayStartLocal).weekday
            if (allowedDays[weekday]) return candidateUtc
        }

        return null
    }

    private fun isSuccessOn(habit: Habit, day: Timestamp): Boolean {
        val value = habit.computedEntries.get(day).value
        if (!habit.isNumerical) return value > 0

        return when (habit.targetType) {
            NumericalHabitType.AT_LEAST -> value / 1000.0 >= habit.targetValue
            NumericalHabitType.AT_MOST -> value != Entry.UNKNOWN && value / 1000.0 <= habit.targetValue
        }
    }
}
