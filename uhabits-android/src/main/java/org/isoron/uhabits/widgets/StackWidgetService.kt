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

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.RemoteViewsService.RemoteViewsFactory
import org.isoron.platform.utils.StringUtils.Companion.splitLongs
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitNotFoundException
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import java.util.ArrayList

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext, intent)
    }

    companion object {
        const val WIDGET_TYPE = "WIDGET_TYPE"
        const val HABIT_IDS = "HABIT_IDS"
    }
}

internal class StackRemoteViewsFactory(private val context: Context, intent: Intent) :
    RemoteViewsFactory {
    private val widgetId: Int = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )
    private val habitIds: LongArray
    private val widgetType: StackWidgetType
    private var remoteViews = ArrayList<RemoteViews>()
    override fun onCreate() {}
    override fun onDestroy() {}
    override fun getCount(): Int {
        return habitIds.size
    }

    fun getDimensionsFromOptions(
        ctx: Context,
        options: Bundle
    ): WidgetDimensions {
        val maxWidth = dpToPixels(
            ctx,
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH).toFloat()
        ).toInt()
        val maxHeight = dpToPixels(
            ctx,
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).toFloat()
        ).toInt()
        val minWidth = dpToPixels(
            ctx,
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).toFloat()
        ).toInt()
        val minHeight = dpToPixels(
            ctx,
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT).toFloat()
        ).toInt()
        return WidgetDimensions(minWidth, maxHeight, maxWidth, minHeight)
    }

    override fun getViewAt(position: Int): RemoteViews? {
        Log.i("StackRemoteViewsFactory", "getViewAt $position")
        return if (0 <= position && position < remoteViews.size) remoteViews[position] else null
    }

    private fun constructWidget(
        habit: Habit,
        prefs: Preferences
    ): BaseWidget {
        return when (widgetType) {
            StackWidgetType.CHECKMARK -> CheckmarkWidget(context, widgetId, habit, true)
            StackWidgetType.FREQUENCY -> FrequencyWidget(
                context,
                widgetId,
                habit,
                prefs.firstWeekdayInt,
                true
            )
            StackWidgetType.SCORE -> ScoreWidget(context, widgetId, habit, true)
            StackWidgetType.HISTORY -> HistoryWidget(context, widgetId, habit, true)
            StackWidgetType.STREAKS -> StreakWidget(context, widgetId, habit, true)
            StackWidgetType.TARGET -> TargetWidget(context, widgetId, habit, true)
        }
    }

    override fun getLoadingView(): RemoteViews {
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId)
        val widget = EmptyWidget(context, widgetId)
        widget.setDimensions(getDimensionsFromOptions(context, options))
        val landscapeViews = widget.landscapeRemoteViews
        val portraitViews = widget.portraitRemoteViews
        return RemoteViews(landscapeViews, portraitViews)
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return habitIds[position]
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDataSetChanged() {
        Log.i("StackRemoteViewsFactory", "onDataSetChanged started")
        val app = context.applicationContext as HabitsApplication
        val prefs = app.component.preferences
        val habitList = app.component.habitList
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId)
        val newRemoteViews = ArrayList<RemoteViews>()
        if (Looper.myLooper() == null) Looper.prepare()
        for (id in habitIds) {
            val h = habitList.getById(id) ?: throw HabitNotFoundException()
            val widget = constructWidget(h, prefs)
            widget.setDimensions(getDimensionsFromOptions(context, options))
            val landscapeViews = widget.landscapeRemoteViews
            val portraitViews = widget.portraitRemoteViews
            newRemoteViews.add(RemoteViews(landscapeViews, portraitViews))
            Log.i("StackRemoteViewsFactory", "onDataSetChanged constructed widget $id")
        }
        remoteViews = newRemoteViews
        Log.i("StackRemoteViewsFactory", "onDataSetChanged ended")
    }

    init {
        val widgetTypeValue = intent.getIntExtra(StackWidgetService.WIDGET_TYPE, -1)
        val habitIdsStr = intent.getStringExtra(StackWidgetService.HABIT_IDS)
        if (widgetTypeValue < 0) throw RuntimeException("invalid widget type")
        if (habitIdsStr == null) throw RuntimeException("habitIdsStr is null")
        widgetType = StackWidgetType.getWidgetTypeFromValue(widgetTypeValue)
            ?: throw RuntimeException("unknown widget type value: $widgetTypeValue")
        habitIds = splitLongs(habitIdsStr)
    }
}
