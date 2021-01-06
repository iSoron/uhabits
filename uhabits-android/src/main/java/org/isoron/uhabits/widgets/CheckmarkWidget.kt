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

package org.isoron.uhabits.widgets

import android.app.PendingIntent
import android.content.Context
import android.view.View
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.utils.toThemedAndroidColor
import org.isoron.uhabits.widgets.views.CheckmarkWidgetView

open class CheckmarkWidget(
    context: Context,
    widgetId: Int,
    protected val habit: Habit
) : BaseWidget(context, widgetId) {

    override fun getOnClickPendingIntent(context: Context): PendingIntent {
        return if (habit.isNumerical) {
            pendingIntentFactory.setNumericalValue(context, habit, 10, null)
        } else {
            pendingIntentFactory.toggleCheckmark(habit, null)
        }
    }

    override fun refreshData(v: View) {
        (v as CheckmarkWidgetView).apply {
            val today = DateUtils.getTodayWithOffset()
            setBackgroundAlpha(preferedBackgroundAlpha)
            setActiveColor(habit.color.toThemedAndroidColor(context))
            setName(habit.name)
            setEntryValue(habit.computedEntries.get(today).value)
            if (habit.isNumerical) {
                setNumerical(true)
                setEntryState(getNumericalEntryState())
            } else {
                setEntryState(habit.computedEntries.get(today).value)
            }
            setPercentage(habit.scores.get(today).value.toFloat())
            refresh()
        }
    }

    override fun buildView(): View {
        return CheckmarkWidgetView(context)
    }

    override fun getDefaultHeight() = 125
    override fun getDefaultWidth() = 125

    private fun getNumericalEntryState(): Int {
        return if (habit.isCompletedToday()) {
            Entry.YES_MANUAL
        } else {
            Entry.NO
        }
    }
}
