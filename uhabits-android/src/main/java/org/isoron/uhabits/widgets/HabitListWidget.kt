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
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Habit
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import org.isoron.uhabits.activities.common.views.HabitListChart
import org.isoron.uhabits.core.ui.screens.habits.show.views.HabitListCardPresenter
import org.isoron.uhabits.core.ui.views.WidgetTheme
import org.isoron.uhabits.widgets.views.GraphWidgetView


class HabitListWidget(
    context: Context,
    val widgetId: Int,
    private val habits: List<Habit>,
    stacked: Boolean = false
): BaseWidget(context, widgetId, stacked)  {

    override val defaultHeight: Int = 200
    override val defaultWidth: Int = 200

    override fun getOnClickPendingIntent(context: Context): PendingIntent =
        pendingIntentFactory.showListHabitsActivity()

    override fun refreshData(view: View) {
        val maxDays = 10
        val data = HabitListCardPresenter.buildState(
            habits = habits,
            theme = WidgetTheme(),
            maxDays = maxDays
        )
        val widgetView = view as GraphWidgetView
        widgetView.setBackgroundAlpha(preferedBackgroundAlpha)
        if (preferedBackgroundAlpha >= 255) widgetView.setShadowAlpha(0x4f)
        (widgetView.dataView as HabitListChart).apply{
            setHabits(data.habits)
            setHeaderDates(data.weekDayStrings, data.dateStrings)
            setMaxCheckMarks(maxDays)
        }

    }

    override fun buildView() =
        GraphWidgetView(context, HabitListChart(context)).apply {
            setTitle("Jordan Test")
            val title = findViewById<View>(R.id.title) as TextView
            title.textSize = 0.toFloat()
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)


    }
}