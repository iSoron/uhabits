/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.activities.habits.show

import android.annotation.*
import android.content.*
import org.isoron.androidbase.activities.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.*
import org.isoron.uhabits.activities.habits.list.views.*
import org.isoron.uhabits.core.commands.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.utils.*
import org.isoron.uhabits.utils.*
import java.util.*
import javax.inject.*

@ActivityScope
class ShowHabitPresenter
@Inject constructor(
        val habit: Habit,
        val preferences: Preferences,
        commandRunner: CommandRunner,
        @ActivityContext val context: Context,
) : Presenter<ShowHabitViewModel>(commandRunner) {

    private val resources = context.resources

    override fun refresh(): ShowHabitViewModel {
        val today = DateUtils.getTodayWithOffset()
        val lastMonth = today.minus(30)
        val lastYear = today.minus(365)

        val reminderText = if (habit.hasReminder()) {
            formatTime(context, habit.reminder.hour, habit.reminder.minute)!!
        } else {
            resources.getString(R.string.reminder_off)
        }

        val scores = habit.scores
        val scoreToday = scores.todayValue.toFloat()
        val scoreLastMonth = scores.getValue(lastMonth).toFloat()
        val scoreLastYear = scores.getValue(lastYear).toFloat()

        return ShowHabitViewModel(
                title = habit.name,
                description = habit.description,
                question = habit.question,
                color = habit.color,
                isNumerical = habit.isNumerical,
                scoreToday = scoreToday,
                scoreMonthDiff = scoreToday - scoreLastMonth,
                scoreYearDiff = scoreToday - scoreLastYear,
                totalCount = habit.repetitions.totalCount,
                targetText = "${habit.targetValue.toShortString()} ${habit.unit}",
                frequencyText = habit.frequency.format(),
                reminderText = reminderText,
        )
    }

    @SuppressLint("StringFormatMatches")
    private fun Frequency.format(): String {
        val num = this.numerator
        val den = this.denominator
        if (num == den) {
            return resources.getString(R.string.every_day)
        }
        if (den == 7) {
            return resources.getString(R.string.x_times_per_week, num)
        }
        if (den == 30 || den == 31) {
            return resources.getString(R.string.x_times_per_month, num)
        }
        if (num == 1) {
            if (den == 7) {
                return resources.getString(R.string.every_week)
            }
            if (den % 7 == 0) {
                return resources.getString(R.string.every_x_weeks, den / 7)
            }
            if (den == 30 || den == 31) {
                return resources.getString(R.string.every_month)
            }
            return resources.getString(R.string.every_x_days, den)
        }
        return String.format(
                Locale.US,
                "%d %s %d %s",
                num,
                resources.getString(R.string.times_every),
                den,
                resources.getString(R.string.days),
        )
    }
}