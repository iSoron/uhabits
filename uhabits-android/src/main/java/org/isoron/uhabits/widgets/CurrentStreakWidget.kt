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

import android.app.PendingIntent
import android.content.Context
import android.view.View
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.ui.views.WidgetTheme
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.widgets.views.CurrentStreakWidgetView

open class CurrentStreakWidget(
    context: Context,
    widgetId: Int,
    protected val habit: Habit,
    stacked: Boolean = false
) : BaseWidget(context, widgetId, stacked) {

    override val defaultHeight: Int = 125
    override val defaultWidth: Int = 125

    override fun getOnClickPendingIntent(context: Context): PendingIntent? {
        return if (habit.isNumerical) {
            pendingIntentFactory.showNumberPicker(habit, DateUtils.getTodayWithOffset())
        } else {
            pendingIntentFactory.toggleCheckmark(habit, null)
        }
    }

    override fun refreshData(widgetView: View) {
        (widgetView as CurrentStreakWidgetView).apply {
            setBackgroundAlpha(preferedBackgroundAlpha)
            activeColor = WidgetTheme().color(habit.color).toInt()
            name = habit.name
            currentStreak = getCurrentStreak()
            isCompleted = habit.isCompletedToday()
            refresh()
        }
    }

    private fun getCurrentStreak(): Int {
        val today = DateUtils.getTodayWithOffset()
        val latestStreak = habit.streaks.getAll().maxByOrNull { it.end } ?: return 0

        // For non-daily habits, the streak doesn't necessarily end "yesterday".
        // It's still active if it ended within the period defined by the frequency.
        val intervalSize = habit.frequency.denominator
        val daysSinceStreakEnded = latestStreak.end.daysUntil(today)

        return if (daysSinceStreakEnded < intervalSize) {
            latestStreak.length
        } else {
            0
        }
    }

    override fun buildView(): View {
        return CurrentStreakWidgetView(context)
    }
}
