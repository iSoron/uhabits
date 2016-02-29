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

package org.isoron.uhabits.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.isoron.helpers.DialogHelper;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;

public abstract class BaseWidgetProvider extends AppWidgetProvider
{

    protected abstract int getDefaultHeight();

    protected abstract int getDefaultWidth();

    protected abstract PendingIntent getOnClickPendingIntent(Context context, Habit habit);

    protected abstract  int getLayoutId();

    protected abstract View buildCustomView(Context context, int max_height, int max_width,
                                            Habit habit);

    public static String getHabitIdKey(long widgetId)
    {
        return String.format("widget-%06d-habit", widgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        for(Integer id : appWidgetIds)
            prefs.edit().remove(getHabitIdKey(id));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions)
    {
        updateWidget(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds)
    {
        for(int id : appWidgetIds)
        {
            Bundle options = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                options = manager.getAppWidgetOptions(id);

            updateWidget(context, manager, id, options);
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId, Bundle options)
    {
        int maxWidth = getDefaultWidth();
        int maxHeight = getDefaultHeight();

        if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            maxWidth = (int) DialogHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH));
            maxHeight = (int) DialogHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
        }

        Context appContext = context.getApplicationContext();
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), getLayoutId());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        Long habitId = prefs.getLong(getHabitIdKey(widgetId), -1L);
        if(habitId < 0) return;

        Habit habit = Habit.get(habitId);
        View widgetView = buildCustomView(context, maxHeight, maxWidth, habit);
        widgetView.setDrawingCacheEnabled(true);
        widgetView.buildDrawingCache(true);
        Bitmap drawingCache = widgetView.getDrawingCache();

        remoteViews.setTextViewText(R.id.label, habit.name);
        remoteViews.setImageViewBitmap(R.id.imageView, drawingCache);

        PendingIntent onClickIntent = getOnClickPendingIntent(context, habit);
        if(onClickIntent != null) remoteViews.setOnClickPendingIntent(R.id.imageView, onClickIntent);

        manager.updateAppWidget(widgetId, remoteViews);
    }
}
