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

import android.app.*
import android.content.*
import android.view.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.widgets.views.*

open class CheckmarkWidget(
        context: Context,
        widgetId: Int,
        protected val habit: Habit
) : BaseWidget(context, widgetId) {

    override fun getOnClickPendingIntent(context: Context): PendingIntent {
        return if (habit.isNumerical) {
            pendingIntentFactory.setNumericalValue(context, habit, 10, null)
        } else {
            val prefs = (context.applicationContext as HabitsApplication).component.preferences
            if (prefs.isAdvancedCheckmarksEnabled) {
                pendingIntentFactory.setYesNoValue(habit, null, -1)
            }  else {
                pendingIntentFactory.toggleCheckmark(habit, null)
            }
        }
    }

    override fun refreshData(v: View) {
        (v as CheckmarkWidgetView).apply {
            setBackgroundAlpha(preferedBackgroundAlpha)

            setActiveColor(PaletteUtils.getColor(context, habit.color))
            setName(habit.name)
            setCheckmarkValue(habit.checkmarks.todayValue)
            if (habit.isNumerical) {
                setNumerical(true)
                setCheckmarkState(getNumericalCheckmarkState())
            } else {
                setCheckmarkState(habit.checkmarks.todayValue)
            }
            setPercentage(habit.scores.todayValue.toFloat())
            refresh()
        }
    }

    override fun buildView(): View {
        return CheckmarkWidgetView(context)
    }

    override fun getDefaultHeight() = 125
    override fun getDefaultWidth() = 125

    private fun getNumericalCheckmarkState(): Int {
        return if (habit.isCompletedToday) {
            Checkmark.CHECKED_EXPLICITLY
        } else {
            Checkmark.UNCHECKED
        }
    }

}
