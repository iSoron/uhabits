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

package org.isoron.uhabits.widgets

import android.content.*
import android.view.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.widgets.views.*


class CurrentStreakWidget(
        context: Context,
        widgetId: Int,
        private val habit: Habit
) : BaseWidget(context, widgetId) {

    override fun getOnClickPendingIntent(context: Context) =
            pendingIntentFactory.toggleCheckmark(habit, null)
    
    override fun refreshData(v: View) {
        val activeStreak = habit.streaks.activeStreak
        val numReps = if (activeStreak != null) {
            habit.repetitions.getByInterval(activeStreak.start, activeStreak.end).size.toString()
        } else {
            "0"
        }
        val timeoutPercent = habit.timeoutPercentage()

        (v as CurrentStreakWidgetView).apply {
            setBackgroundAlpha(preferedBackgroundAlpha)
            setCurrentStreak(numReps)
            setPercentage(habit.scores.todayValue.toFloat())
            setTimeOutPercentage(1 - timeoutPercent)
            setName(habit.name)
            setCheckmarkValue(habit.checkmarks.todayValue)
            refresh()
        }
    }

    override fun buildView() = CurrentStreakWidgetView(context)
    override fun getDefaultHeight() = 125
    override fun getDefaultWidth() = 125
}
