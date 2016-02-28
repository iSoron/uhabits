/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import org.isoron.helpers.DialogHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.views.HabitHistoryView;

public class HabitWidgetProvider extends AppWidgetProvider
{

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds)
    {
        for(int id : appWidgetIds)
        {
            Bundle options = manager.getAppWidgetOptions(id);
            updateWidget(context, manager, id, options);
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId, Bundle options)
    {
        int max_height = (int) DialogHelper.dpToPixels(context, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
        int min_height = (int) DialogHelper.dpToPixels(context, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        int max_width =  (int) DialogHelper.dpToPixels(context, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH));
        int min_width =  (int) DialogHelper.dpToPixels(context, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));

        Log.d("HabitWidgetProvider", String.format("max_h=%d min_h=%d max_w=%d min_w=%d",
                max_height, min_height, max_width, min_width));

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_graph);
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        Long habitId = prefs.getLong(getWidgetPrefKey(widgetId), -1L);
        if(habitId < 0) return;

        Habit habit = Habit.get(habitId);

//        SmallWidgetView widgetView = new SmallWidgetView(context);
        HabitHistoryView widgetView = new HabitHistoryView(context, null);
//        HabitScoreView widgetView = new HabitScoreView(context, null);
//        HabitStreakView widgetView = new HabitStreakView(context, null);
        widgetView.setHabit(habit);
        widgetView.setDrawingCacheEnabled(true);
        widgetView.measure(max_width, max_height);
        widgetView.layout(0, 0, max_width, max_height);

        int width = widgetView.getMeasuredWidth();
        int height = widgetView.getMeasuredHeight();
        Log.d("SmallWidgetProvider", String.format("width=%d height=%d\n", width, height));

        height -= DialogHelper.dpToPixels(context, 12f);
        widgetView.measure(width, height);
        widgetView.layout(0, 0, width, height);
        widgetView.buildDrawingCache(true);
        Bitmap drawingCache = widgetView.getDrawingCache();

        remoteViews.setTextViewText(R.id.tvName, habit.name);
        remoteViews.setImageViewBitmap(R.id.imageView, drawingCache);
        remoteViews.setOnClickPendingIntent(R.id.imageView,
                HabitBroadcastReceiver.buildCheckIntent(context, habit, null));
        manager.updateAppWidget(widgetId, remoteViews);
    }

    public static String getWidgetPrefKey(long widgetId)
    {
        return String.format("widget-%03d", widgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        for(Integer id : appWidgetIds)
            prefs.edit().remove(getWidgetPrefKey(id));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions)
    {
        updateWidget(context, appWidgetManager, appWidgetId, newOptions);
    }
}
