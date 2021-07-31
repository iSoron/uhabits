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
import org.isoron.uhabits.activities.common.views.ScoreChart
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.ui.screens.habits.show.views.ScoreCardPresenter
import org.isoron.uhabits.core.ui.views.WidgetTheme
import org.isoron.uhabits.widgets.views.GraphWidgetView

class ScoreWidget(
    context: Context,
    id: Int,
    private val habit: Habit,
    stacked: Boolean = false,
) : BaseWidget(context, id, stacked) {
    override val defaultHeight: Int = 300
    override val defaultWidth: Int = 300

    override fun getOnClickPendingIntent(context: Context): PendingIntent =
        pendingIntentFactory.showHabit(habit)

    override fun refreshData(view: View) {
        val viewModel = ScoreCardPresenter.buildState(
            habit = habit,
            firstWeekday = prefs.firstWeekdayInt,
            spinnerPosition = prefs.scoreCardSpinnerPosition,
            theme = WidgetTheme(),
        )
        val widgetView = view as GraphWidgetView
        widgetView.setBackgroundAlpha(preferedBackgroundAlpha)
        if (preferedBackgroundAlpha >= 255) widgetView.setShadowAlpha(0x4f)
        (widgetView.dataView as ScoreChart).apply {
            setIsTransparencyEnabled(true)
            setBucketSize(viewModel.bucketSize)
            setColor(WidgetTheme().color(habit.color).toInt())
            setScores(viewModel.scores)
        }
    }

    override fun buildView() =
        GraphWidgetView(context, ScoreChart(context)).apply {
            setTitle(habit.name)
        }
}
