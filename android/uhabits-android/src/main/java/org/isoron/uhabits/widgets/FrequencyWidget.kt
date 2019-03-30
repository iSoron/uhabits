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
import org.isoron.uhabits.activities.common.views.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.widgets.views.*

class FrequencyWidget(
        context: Context,
        widgetId: Int,
        private val habit: Habit
) : BaseWidget(context, widgetId) {

    override fun getOnClickPendingIntent(context: Context) =
            pendingIntentFactory.showHabit(habit)

    override fun refreshData(v: View) {
        val widgetView = v as GraphWidgetView
        widgetView.setTitle(habit.name)
        (widgetView.dataView as FrequencyChart).apply {
            setColor(PaletteUtils.getColor(context, habit.color))
            setFrequency(habit.repetitions.weekdayFrequency)
        }
    }

    override fun buildView() =
            GraphWidgetView(context, FrequencyChart(context))

    override fun getDefaultHeight() = 200
    override fun getDefaultWidth() = 200
}
