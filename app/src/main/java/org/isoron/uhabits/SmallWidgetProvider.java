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
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.views.SmallWidgetView;

public class SmallWidgetProvider extends AppWidgetProvider
{

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds)
    {
        for(int id : appWidgetIds)
            updateWidget(context, manager, id);
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId)
    {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.small_widget);
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        Long habitId = prefs.getLong(getWidgetPrefKey(widgetId), -1L);
        if(habitId < 0) return;

        Habit habit = Habit.get(habitId);

        SmallWidgetView widgetView = new SmallWidgetView(context);
        widgetView.setDrawingCacheEnabled(true);
        widgetView.measure(180, 200);
        widgetView.layout(0, 0, 180, 200);
        widgetView.buildDrawingCache(true);
        widgetView.setHabit(habit);

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
}
