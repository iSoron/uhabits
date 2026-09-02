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
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.getToday
import org.isoron.uhabits.activities.common.views.MultiWeeklyChart
import org.isoron.uhabits.activities.common.views.MultiWeeklyRow
import org.isoron.uhabits.activities.habits.list.ListHabitsActivity
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.utils.toFixedAndroidColor
import org.isoron.uhabits.widgets.views.GraphWidgetView
import java.util.Locale

class MultiWeeklyWidget(
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
        val today = getToday()
        val weekStart = today.startOfWeek(prefs.firstWeekday)
        val rows = habits.map { habit ->
            val entries = (0 until 7).map { offset ->
                habit.computedEntries.get(weekStart.plus(offset)).value
            }
            MultiWeeklyRow(
                color = habit.color.toFixedAndroidColor(),
                label = habit.name.take(3),
                entries = entries
            )
        }
        val chart = widgetView.dataView as MultiWeeklyChart
        chart.setRows(rows)
        chart.setWeekStart(weekStart)
        chart.setDateFormatter(JavaLocalDateFormatter(Locale.getDefault()))
    }

    override fun buildView() =
        GraphWidgetView(context, MultiWeeklyChart(context)).apply {
            setTitle(buildColoredTitle())
        }

    private fun buildColoredTitle(): CharSequence {
        val names = habits.take(5).map { it.name.take(3) }
        val spannable = SpannableString(names.joinToString(" · "))
        var offset = 0
        names.forEachIndexed { index, name ->
            val start = offset
            val end = start + name.length
            spannable.setSpan(
                ForegroundColorSpan(habits[index].color.toFixedAndroidColor()),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            offset = end + 3
        }
        return spannable
    }
}
