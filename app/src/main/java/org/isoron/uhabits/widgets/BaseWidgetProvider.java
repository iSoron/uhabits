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
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.UIHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.BaseTask;

import java.io.FileOutputStream;
import java.io.IOException;

public abstract class BaseWidgetProvider extends AppWidgetProvider
{
    private class WidgetDimensions
    {
        public int portraitWidth, portraitHeight;
        public int landscapeWidth, landscapeHeight;
    }

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
            prefs.edit().remove(getHabitIdKey(id)).apply();
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

    private void updateWidget(Context context, AppWidgetManager manager,
                              int widgetId, Bundle options)
    {
        WidgetDimensions dim = getWidgetDimensions(context, options);

        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        Long habitId = prefs.getLong(getHabitIdKey(widgetId), -1L);
        if(habitId < 0) return;

        Habit habit = Habit.get(habitId);
        if(habit == null)
        {
            drawErrorWidget(context, manager, widgetId);
            return;
        }

        new RenderWidgetTask(widgetId, context, habit, dim, manager).execute();
    }

    private void drawErrorWidget(Context context, AppWidgetManager manager, int widgetId)
    {
        RemoteViews errorView = new RemoteViews(context.getPackageName(), R.layout.widget_error);
        manager.updateAppWidget(widgetId, errorView);
    }

    protected abstract void refreshCustomViewData(View widgetView);

    private void savePreview(Context context, int widgetId, Bitmap widgetCache, int width,
                             int height, String label)
    {
        try
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(getLayoutId(), null);

            TextView tvLabel = (TextView) view.findViewById(R.id.label);
            if(tvLabel != null) tvLabel.setText(label);

            ImageView iv = (ImageView) view.findViewById(R.id.imageView);
            if(iv != null) iv.setImageBitmap(widgetCache);

            view.measure(width, height);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap previewCache = view.getDrawingCache();

            String filename = String.format("%s/%d_%d.png", context.getExternalCacheDir(), widgetId, width);
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

    private WidgetDimensions getWidgetDimensions(Context context, Bundle options)
    {
        int maxWidth = getDefaultWidth();
        int minWidth = getDefaultWidth();
        int maxHeight = getDefaultHeight();
        int minHeight = getDefaultHeight();

        if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            maxWidth = (int) UIHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH));
            maxHeight = (int) UIHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
            minWidth = (int) UIHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
            minHeight = (int) UIHelper.dpToPixels(context,
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        }

        WidgetDimensions ws = new WidgetDimensions();
        ws.portraitWidth = minWidth;
        ws.portraitHeight = maxHeight;
        ws.landscapeWidth = maxWidth;
        ws.landscapeHeight = minHeight;
        return ws;
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

    private class RenderWidgetTask extends BaseTask
    {
        private final int widgetId;
        private final Context context;
        private final Habit habit;
        private final AppWidgetManager manager;
        private RemoteViews portraitRemoteViews, landscapeRemoteViews;
        private View portraitWidgetView, landscapeWidgetView;
        private WidgetDimensions dim;

        public RenderWidgetTask(int widgetId, Context context, Habit habit, WidgetDimensions ws,
                                AppWidgetManager manager)
        {
            this.widgetId = widgetId;
            this.context = context;
            this.habit = habit;
            this.manager = manager;
            this.dim = ws;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            context.setTheme(R.style.TransparentWidgetTheme);

            portraitRemoteViews = new RemoteViews(context.getPackageName(), getLayoutId());
            portraitWidgetView = buildCustomView(context, habit);
            measureCustomView(context, dim.portraitWidth, dim.portraitHeight, portraitWidgetView);

            landscapeRemoteViews = new RemoteViews(context.getPackageName(), getLayoutId());
            landscapeWidgetView = buildCustomView(context, habit);
            measureCustomView(context, dim.landscapeWidth, dim.landscapeHeight,
                    landscapeWidgetView);
        }

        private void updateAppWidget()
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                manager.updateAppWidget(widgetId, new RemoteViews(landscapeRemoteViews,
                        portraitRemoteViews));
            else
                manager.updateAppWidget(widgetId, portraitRemoteViews);
        }

        @Override
        protected void doInBackground()
        {
            refreshCustomViewData(portraitWidgetView);
            refreshCustomViewData(landscapeWidgetView);
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            try
            {
                buildRemoteViews(portraitWidgetView, portraitRemoteViews,
                        dim.portraitWidth, dim.portraitHeight);
                buildRemoteViews(landscapeWidgetView, landscapeRemoteViews,
                        dim.landscapeWidth, dim.landscapeHeight);
                updateAppWidget();
            }
            catch (Exception e)
            {
                drawErrorWidget(context, manager, widgetId);
                e.printStackTrace();
            }

            super.onPostExecute(aVoid);
        }

        private void buildRemoteViews(View widgetView, RemoteViews remoteViews, int width,
                                      int height)
        {
            widgetView.invalidate();
            widgetView.setDrawingCacheEnabled(true);
            widgetView.buildDrawingCache(true);
            Bitmap drawingCache = widgetView.getDrawingCache();
            remoteViews.setTextViewText(R.id.label, habit.name);
            remoteViews.setImageViewBitmap(R.id.imageView, drawingCache);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                int imageWidth = widgetView.getMeasuredWidth();
                int imageHeight = widgetView.getMeasuredHeight();
                int p[] = getPadding(width, height, imageWidth, imageHeight);
                remoteViews.setViewPadding(R.id.buttonOverlay, p[0], p[1], p[2], p[3]);
            }

            //savePreview(context, widgetId, drawingCache, width, height, habit.name);

            PendingIntent onClickIntent = getOnClickPendingIntent(context, habit);
            if (onClickIntent != null) remoteViews.setOnClickPendingIntent(R.id.button,
                    onClickIntent);
        }
    }

    private int[] getPadding(int entireWidth, int entireHeight, int imageWidth,
                             int imageHeight)
    {
        int w = (int) (((float) entireWidth - imageWidth) / 2);
        int h = (int) (((float) entireHeight - imageHeight) / 2);

        return new int[]{ w, h, w, h };
    }
}
