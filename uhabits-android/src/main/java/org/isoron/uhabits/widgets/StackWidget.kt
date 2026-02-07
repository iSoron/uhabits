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
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import org.isoron.platform.utils.StringUtils
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import java.util.Date
import kotlin.math.max

class StackWidget(
    context: Context,
    widgetId: Int,
    private val widgetType: StackWidgetType,
    private val habits: List<Habit>,
    stacked: Boolean = true
) : BaseWidget(context, widgetId, stacked) {
    override val defaultHeight: Int = 0
    override val defaultWidth: Int = 0

    override fun getOnClickPendingIntent(context: Context): PendingIntent? = null

    override fun refreshData(v: View) {
        // unused
    }

    override fun buildView(): View? {
        // unused
        return null
    }

    override fun getRemoteViews(width: Int, height: Int): RemoteViews {
        val manager = AppWidgetManager.getInstance(context)
        val habitIds = StringUtils.joinLongs(habits.map { it.id!! }.toLongArray())

        if (widgetType == StackWidgetType.CHECKMARK && shouldUseCheckmarkGrid(width, height)) {
            val remoteViews = RemoteViews(context.packageName, R.layout.checkmark_gridview_widget)
            val serviceIntent = Intent(context, GridWidgetService::class.java)

            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            serviceIntent.putExtra(GridWidgetService.WIDGET_TYPE, widgetType.value)
            serviceIntent.putExtra(GridWidgetService.HABIT_IDS, habitIds)
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

            remoteViews.setRemoteAdapter(R.id.checkmarkGridWidgetView, serviceIntent)
            manager.notifyAppWidgetViewDataChanged(id, R.id.checkmarkGridWidgetView)
            remoteViews.setEmptyView(R.id.checkmarkGridWidgetView, R.id.checkmarkGridWidgetEmptyView)

            val doneCount = habits.count { it.isCompletedToday() }
            remoteViews.setTextViewText(
                R.id.checkmarkGridWidgetSummary,
                context.getString(R.string.widget_dashboard_done_summary, doneCount, habits.size)
            )

            val now = System.currentTimeMillis()
            val next = habits.mapNotNull { WidgetHabitStats.computeNextReminderTimeUtcMillis(it, now) }.minOrNull()
            if (next != null) {
                val timeStr = DateFormat.getTimeFormat(context).format(Date(next))
                remoteViews.setViewVisibility(R.id.checkmarkGridWidgetNext, View.VISIBLE)
                remoteViews.setTextViewText(
                    R.id.checkmarkGridWidgetNext,
                    context.getString(R.string.widget_next_reminder_at, timeStr)
                )
            } else {
                remoteViews.setViewVisibility(R.id.checkmarkGridWidgetNext, View.GONE)
            }

            remoteViews.setPendingIntentTemplate(
                R.id.checkmarkGridWidgetView,
                StackWidgetType.getPendingIntentTemplate(pendingIntentFactory, widgetType, habits)
            )

            return remoteViews
        }

        val remoteViews =
            RemoteViews(context.packageName, StackWidgetType.getStackWidgetLayoutId(widgetType))
        val serviceIntent = Intent(context, StackWidgetService::class.java)

        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        serviceIntent.putExtra(StackWidgetService.WIDGET_TYPE, widgetType.value)
        serviceIntent.putExtra(StackWidgetService.HABIT_IDS, habitIds)
        serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

        val adapterViewId = StackWidgetType.getStackWidgetAdapterViewId(widgetType)
        remoteViews.setRemoteAdapter(adapterViewId, serviceIntent)
        manager.notifyAppWidgetViewDataChanged(id, adapterViewId)
        remoteViews.setEmptyView(adapterViewId, StackWidgetType.getStackWidgetEmptyViewId(widgetType))
        remoteViews.setPendingIntentTemplate(
            adapterViewId,
            StackWidgetType.getPendingIntentTemplate(pendingIntentFactory, widgetType, habits)
        )

        return remoteViews
    }

    private fun shouldUseCheckmarkGrid(width: Int, height: Int): Boolean {
        if (habits.size < 3) return false
        val minCell = dpToPixels(context, 140f).toInt()
        val columns = max(1, width / max(1, minCell))
        val rows = max(1, height / max(1, minCell))
        return columns >= 2 && rows >= 2
    }
}
