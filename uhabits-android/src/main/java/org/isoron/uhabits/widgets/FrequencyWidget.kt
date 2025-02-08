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
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.activities.common.views.FrequencyChart
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.ui.screens.habits.show.views.FrequencyCardPresenter
import org.isoron.uhabits.core.ui.views.WidgetTheme
import org.isoron.uhabits.widgets.views.GraphWidgetView

class FrequencyWidget private constructor(
    context: Context,
    widgetId: Int,
    private val habit: Habit?,
    private val habitGroup: HabitGroup?,
    private val firstWeekday: Int,
    stacked: Boolean
) : BaseWidget(context, widgetId, stacked) {

    constructor(context: Context, widgetId: Int, habit: Habit, firstWeekday: Int, stacked: Boolean = false) : this(context, widgetId, habit, null, firstWeekday, stacked)
    constructor(context: Context, widgetId: Int, habitGroup: HabitGroup, firstWeekday: Int, stacked: Boolean = false) : this(context, widgetId, null, habitGroup, firstWeekday, stacked)

    override val defaultHeight: Int = 200
    override val defaultWidth: Int = 200

    override fun getOnClickPendingIntent(context: Context): PendingIntent {
        return if (habit != null) {
            pendingIntentFactory.showHabit(habit)
        } else {
            pendingIntentFactory.showHabitGroup(habitGroup!!)
        }
    }

    override fun refreshData(v: View) {
        val widgetView = v as GraphWidgetView
        widgetView.setTitle(habit?.name ?: habitGroup!!.name)
        widgetView.setBackgroundAlpha(preferedBackgroundAlpha)
        if (preferedBackgroundAlpha >= 255) widgetView.setShadowAlpha(0x4f)
        val color = habit?.color ?: habitGroup!!.color
        val isNumerical = habit?.isNumerical ?: true
        val frequency = if (habit != null) {
            habit.originalEntries.computeWeekdayFrequency(habit.isNumerical)
        } else {
            FrequencyCardPresenter.getFrequenciesFromHabitGroup(habitGroup!!)
        }
        (widgetView.dataView as FrequencyChart).apply {
            setFirstWeekday(firstWeekday)
            setColor(WidgetTheme().color(color).toInt())
            setIsNumerical(isNumerical)
            setFrequency(frequency)
        }
    }

    override fun buildView() =
        GraphWidgetView(context, FrequencyChart(context))
}
