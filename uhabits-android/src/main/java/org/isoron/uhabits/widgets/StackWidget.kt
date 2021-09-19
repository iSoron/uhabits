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
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import org.isoron.platform.utils.StringUtils
import org.isoron.uhabits.core.models.Habit

class StackWidget(
    context: Context,
    widgetId: Int,
    private val widgetType: StackWidgetType,
    private val habits: List<Habit>,
    stacked: Boolean = true,
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
        val remoteViews =
            RemoteViews(context.packageName, StackWidgetType.getStackWidgetLayoutId(widgetType))
        val serviceIntent = Intent(context, StackWidgetService::class.java)
        val habitIds = StringUtils.joinLongs(habits.map { it.id!! }.toLongArray())

        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        serviceIntent.putExtra(StackWidgetService.WIDGET_TYPE, widgetType.value)
        serviceIntent.putExtra(StackWidgetService.HABIT_IDS, habitIds)
        serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
        remoteViews.setRemoteAdapter(
            StackWidgetType.getStackWidgetAdapterViewId(widgetType),
            serviceIntent
        )
        manager.notifyAppWidgetViewDataChanged(
            id,
            StackWidgetType.getStackWidgetAdapterViewId(widgetType)
        )
        remoteViews.setEmptyView(
            StackWidgetType.getStackWidgetAdapterViewId(widgetType),
            StackWidgetType.getStackWidgetEmptyViewId(widgetType)
        )
        return remoteViews
    }
}
