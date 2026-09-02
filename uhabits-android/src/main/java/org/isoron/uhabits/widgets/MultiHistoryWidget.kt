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
import android.content.Intent
import android.view.View
import org.isoron.platform.gui.AndroidDataView
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.getToday
import org.isoron.uhabits.activities.habits.list.ListHabitsActivity
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.ui.screens.habits.show.views.HistoryCardPresenter
import org.isoron.uhabits.core.ui.views.MultiHistoryChart
import org.isoron.uhabits.core.ui.views.MultiHistoryData
import org.isoron.uhabits.core.ui.views.WidgetTheme
import org.isoron.uhabits.widgets.views.GraphWidgetView
import java.util.Locale

class MultiHistoryWidget(
    context: Context,
    id: Int,
    private val habits: List<Habit>,
    stacked: Boolean = false
) : BaseWidget(context, id, stacked) {

    override val defaultHeight: Int = 250
    override val defaultWidth: Int = 250

    override fun getOnClickPendingIntent(context: Context): PendingIntent? {
        val intent = Intent(context, ListHabitsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun refreshData(view: View) {
        val widgetView = view as GraphWidgetView
        widgetView.setBackgroundAlpha(preferedBackgroundAlpha)
        if (preferedBackgroundAlpha >= 255) widgetView.setShadowAlpha(0x4f)
        val data = habits.map { habit ->
            val state = HistoryCardPresenter.buildState(
                habit = habit,
                firstWeekday = prefs.firstWeekday,
                theme = WidgetTheme()
            )
            MultiHistoryData(
                paletteColor = state.color,
                series = state.series,
                defaultSquare = state.defaultSquare
            )
        }
        (widgetView.dataView as AndroidDataView).apply {
            val chart = (this.view as MultiHistoryChart)
            chart.habits = data
        }
    }

    override fun buildView() =
        GraphWidgetView(
            context,
            AndroidDataView(context).apply {
                view = MultiHistoryChart(
                    today = getToday(),
                    habits = listOf(),
                    theme = WidgetTheme(),
                    dateFormatter = JavaLocalDateFormatter(Locale.getDefault()),
                    firstWeekday = prefs.firstWeekday,
                    padding = 2.5
                )
            }
        ).apply {
            setTitle(habits.take(5).joinToString(" · ") { it.name.take(3) })
        }
}
