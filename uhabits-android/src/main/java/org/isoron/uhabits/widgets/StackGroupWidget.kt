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

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import org.isoron.uhabits.R
import org.isoron.uhabits.widgets.views.CheckmarkWidgetView

class StackGroupWidget(
        context: Context,
        widgetId: Int
) : BaseWidget(context, widgetId) {

    override fun getOnClickPendingIntent(context: Context) = null

    override fun refreshData(v: View) {
        // unused
    }

    override fun getRemoteViews(width: Int, height: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_stackview)
        val serviceIntent = Intent(context, StackWidgetService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        // TODO this commented line is taken from official examples, but adding it seems to make the widget not update immediately when completing the habit
        // serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)) // embed extras so they don't get ignored
        remoteViews.setRemoteAdapter(R.id.stackWidgetView, serviceIntent)
        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(id, R.id.stackWidgetView)
        // TODO what should the empty view look like?
        remoteViews.setEmptyView(R.id.stackWidgetView, R.id.stackWidgetEmptyView)
        return remoteViews
    }

    override fun buildView() = null // unused
    override fun getDefaultHeight() = 0 // unused
    override fun getDefaultWidth() = 0 // unused
}
