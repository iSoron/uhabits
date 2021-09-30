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
import org.isoron.platform.gui.AndroidDataView
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.ui.screens.habits.show.views.HistoryCardPresenter
import org.isoron.uhabits.core.ui.views.HistoryChart
import org.isoron.uhabits.core.ui.views.WidgetTheme
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.widgets.views.GraphWidgetView
import java.util.Locale

class HistoryWidget(
    context: Context,
    id: Int,
    private val habit: Habit,
    stacked: Boolean = false,
) : BaseWidget(context, id, stacked) {

    override val defaultHeight: Int = 250
    override val defaultWidth: Int = 250

    override fun getOnClickPendingIntent(context: Context): PendingIntent {
        return pendingIntentFactory.showHabit(habit)
    }

    override fun refreshData(view: View) {
        val widgetView = view as GraphWidgetView
        widgetView.setBackgroundAlpha(preferedBackgroundAlpha)
        if (preferedBackgroundAlpha >= 255) widgetView.setShadowAlpha(0x4f)
        val model = HistoryCardPresenter.buildState(
            habit = habit,
            firstWeekday = prefs.firstWeekday,
            theme = WidgetTheme(),
        )
        (widgetView.dataView as AndroidDataView).apply {
            val historyChart = (this.view as HistoryChart)
            historyChart.series = model.series
            historyChart.defaultSquare = model.defaultSquare
            historyChart.notesIndicators = model.notesIndicators
        }
    }

    override fun buildView() =
        GraphWidgetView(
            context,
            AndroidDataView(context).apply {
                view = HistoryChart(
                    today = DateUtils.getTodayWithOffset().toLocalDate(),
                    paletteColor = habit.color,
                    theme = WidgetTheme(),
                    dateFormatter = JavaLocalDateFormatter(Locale.getDefault()),
                    firstWeekday = prefs.firstWeekday,
                    series = listOf(),
                    defaultSquare = HistoryChart.Square.OFF,
                    notesIndicators = listOf(),
                )
            }
        ).apply {
            setTitle(habit.name)
        }
}
