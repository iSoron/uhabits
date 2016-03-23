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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import org.isoron.uhabits.helpers.DialogHelper;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;

import java.io.FileOutputStream;
import java.io.IOException;

public abstract class BaseWidgetProvider extends AppWidgetProvider
{

    private int width, height;

    protected abstract int getDefaultHeight();

    protected abstract int getDefaultWidth();

    protected abstract PendingIntent getOnClickPendingIntent(Context context, Habit habit);

    protected abstract  int getLayoutId();

    protected abstract View buildCustomView(Context context, Habit habit);

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
        updateWidgetSize(context, options);

        Context appContext = context.getApplicationContext();
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), getLayoutId());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        Long habitId = prefs.getLong(getHabitIdKey(widgetId), -1L);
        if(habitId < 0) return;

        Habit habit = Habit.get(habitId);
        if(habit == null)
        {
            RemoteViews errorView = new RemoteViews(context.getPackageName(),
                    R.layout.widget_error);
            manager.updateAppWidget(widgetId, errorView);
            return;
        }

        View widgetView = buildCustomView(context, habit);
        measureCustomView(context, width, height, widgetView);

        widgetView.setDrawingCacheEnabled(true);
        widgetView.buildDrawingCache(true);
        Bitmap drawingCache = widgetView.getDrawingCache();

        remoteViews.setTextViewText(R.id.label, habit.name);
        remoteViews.setImageViewBitmap(R.id.imageView, drawingCache);

        //savePreview(context, widgetId, drawingCache);

        PendingIntent onClickIntent = getOnClickPendingIntent(context, habit);
        if(onClickIntent != null) remoteViews.setOnClickPendingIntent(R.id.imageView, onClickIntent);

        manager.updateAppWidget(widgetId, remoteViews);
    }

    private void savePreview(Context context, int widgetId, Bitmap widgetCache)
    {
        try
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(getLayoutId(), null);

            ImageView iv = (ImageView) view.findViewById(R.id.imageView);
            iv.setImageBitmap(widgetCache);

            view.measure(width, height);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap previewCache = view.getDrawingCache();

            String filename = String.format("%s/%d.png", context.getExternalCacheDir(), widgetId);
            Log.d("BaseWidgetProvider", String.format("Writing %s", filename));
            FileOutputStream out = new FileOutputStream(filename);

            if(previewCache != null)
                previewCache.compress(Bitmap.CompressFormat.PNG, 100, out);

            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void updateWidgetSize(Context context, Bundle options)
    {
        int maxWidth = getDefaultWidth();
        int minWidth = getDefaultWidth();
        int maxHeight = getDefaultHeight();
        int minHeight = getDefaultHeight();

        if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            maxWidth = (int) DialogHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH));
            maxHeight = (int) DialogHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
            minWidth = (int) DialogHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
            minHeight = (int) DialogHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        }

        width = maxWidth;
        height = maxHeight;
    }

    private void measureCustomView(Context context, int w, int h, View customView)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View entireView = inflater.inflate(getLayoutId(), null);

        int specWidth = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY);
        int specHeight = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY);

        entireView.measure(specWidth, specHeight);
        entireView.layout(0, 0, entireView.getMeasuredWidth(), entireView.getMeasuredHeight());

        View imageView = entireView.findViewById(R.id.imageView);
        w = imageView.getMeasuredWidth();
        h = imageView.getMeasuredHeight();

        specWidth = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY);
        specHeight = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY);
        customView.measure(specWidth, specHeight);
        customView.layout(0, 0, customView.getMeasuredWidth(), customView.getMeasuredHeight());
    }
}
