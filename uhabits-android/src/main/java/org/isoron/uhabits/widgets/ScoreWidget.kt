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
import org.isoron.uhabits.activities.habits.show.views.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.widgets.views.*

class ScoreWidget(
        context: Context,
        id: Int,
        private val habit: Habit,
        private val prefs: Preferences
) : BaseWidget(context, id) {

    override fun getOnClickPendingIntent(context: Context) =
            pendingIntentFactory.showHabit(habit)

    override fun refreshData(view: View) {
        val size = ScoreCard.BUCKET_SIZES[prefs.defaultScoreSpinnerPosition]
        val scores = when(size) {
            1 -> habit.scores.toList()
            else -> habit.scores.groupBy(ScoreCard.getTruncateField(size))
        }

        val widgetView = view as GraphWidgetView
        (widgetView.dataView as ScoreChart).apply {
            setIsTransparencyEnabled(true)
            setBucketSize(size)
            setColor(PaletteUtils.getColor(context, habit.color))
            setScores(scores)
        }
    }

    override fun buildView() =
            GraphWidgetView(context, ScoreChart(context)).apply {
                setTitle(habit.name)
            }

    override fun getDefaultHeight() = 300
    override fun getDefaultWidth() = 300
}
