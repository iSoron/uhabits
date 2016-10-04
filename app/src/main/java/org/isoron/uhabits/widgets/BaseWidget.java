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

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.support.annotation.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.preferences.*;

import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.*;
import static android.view.View.MeasureSpec.*;

public abstract class BaseWidget
{
    private final WidgetPreferences prefs;

    private final int id;

    @NonNull
    private WidgetDimensions dimensions;

    @NonNull
    private final Context context;

    protected final PendingIntentFactory pendingIntentFactory;

    public BaseWidget(@NonNull Context context, int id)
    {
        this.id = id;
        this.context = context;

        HabitsApplication app =
            (HabitsApplication) context.getApplicationContext();

        prefs = app.getComponent().getWidgetPreferences();
        pendingIntentFactory = app.getComponent().getPendingIntentFactory();
        dimensions = new WidgetDimensions(getDefaultWidth(), getDefaultHeight(),
            getDefaultWidth(), getDefaultHeight());
    }

    public void delete()
    {
        prefs.removeWidget(id);
    }

    @NonNull
    public Context getContext()
    {
        return context;
    }

    public int getId()
    {
        return id;
    }

    @NonNull
    public RemoteViews getLandscapeRemoteViews()
    {
        return getRemoteViews(dimensions.getLandscapeWidth(),
            dimensions.getLandscapeHeight());
    }

    public abstract PendingIntent getOnClickPendingIntent(Context context);

    @NonNull
    public RemoteViews getPortraitRemoteViews()
    {
        return getRemoteViews(dimensions.getPortraitWidth(),
            dimensions.getPortraitHeight());
    }

    public abstract void refreshData(View widgetView);

    public void setDimensions(@NonNull WidgetDimensions dimensions)
    {
        this.dimensions = dimensions;
    }

    protected abstract View buildView();

    protected abstract int getDefaultHeight();

    protected abstract int getDefaultWidth();

    private void adjustRemoteViewsPadding(RemoteViews remoteViews,
                                          View view,
                                          int width,
                                          int height)
    {
        int imageWidth = view.getMeasuredWidth();
        int imageHeight = view.getMeasuredHeight();
        int p[] = calculatePadding(width, height, imageWidth, imageHeight);
        remoteViews.setViewPadding(R.id.buttonOverlay, p[0], p[1], p[2], p[3]);
    }

    private void buildRemoteViews(View view,
                                  RemoteViews remoteViews,
                                  int width,
                                  int height)
    {
        Bitmap bitmap = getBitmapFromView(view);
        remoteViews.setImageViewBitmap(R.id.imageView, bitmap);

        if (SDK_INT >= JELLY_BEAN)
            adjustRemoteViewsPadding(remoteViews, view, width, height);

        PendingIntent onClickIntent = getOnClickPendingIntent(context);
        if (onClickIntent != null)
            remoteViews.setOnClickPendingIntent(R.id.button, onClickIntent);
    }

    private int[] calculatePadding(int entireWidth,
                                   int entireHeight,
                                   int imageWidth,
                                   int imageHeight)
    {
        int w = (int) (((float) entireWidth - imageWidth) / 2);
        int h = (int) (((float) entireHeight - imageHeight) / 2);

        return new int[]{ w, h, w, h };
    }

    @NonNull
    private Bitmap getBitmapFromView(View view)
    {
        view.invalidate();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap drawingCache = view.getDrawingCache();

        if(drawingCache == null)
            throw new IllegalStateException("bitmap is null");

        return drawingCache;
    }

    @NonNull
    private RemoteViews getRemoteViews(int width, int height)
    {
        View view = buildView();
        measureView(view, width, height);

        refreshData(view);

        if (view.isLayoutRequested()) measureView(view, width, height);

        RemoteViews remoteViews =
            new RemoteViews(context.getPackageName(), R.layout.widget_wrapper);

        buildRemoteViews(view, remoteViews, width, height);

        return remoteViews;
    }

    private void measureView(View view, int width, int height)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View entireView = inflater.inflate(R.layout.widget_wrapper, null);

        int specWidth = makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int specHeight = makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        entireView.measure(specWidth, specHeight);
        entireView.layout(0, 0, entireView.getMeasuredWidth(),
            entireView.getMeasuredHeight());

        View imageView = entireView.findViewById(R.id.imageView);
        width = imageView.getMeasuredWidth();
        height = imageView.getMeasuredHeight();

        specWidth = makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        specHeight = makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        view.measure(specWidth, specHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }
}
