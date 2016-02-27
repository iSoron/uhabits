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
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.views.SmallWidgetView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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

        Habit habit = Habit.get((long) widgetId);

        SmallWidgetView widgetView = new SmallWidgetView(context);
        widgetView.setDrawingCacheEnabled(true);
        widgetView.measure(200, 200);
        widgetView.layout(0, 0, 200, 200);
        widgetView.buildDrawingCache(true);
        widgetView.setHabit(habit);

        Bitmap drawingCache = widgetView.getDrawingCache();

        try
        {
            drawingCache.compress(Bitmap.CompressFormat.PNG, 100,
                    new FileOutputStream(context.getFilesDir() + "/widget.png"));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
;
        remoteViews.setTextViewText(R.id.tvName, habit.name);
        remoteViews.setImageViewBitmap(R.id.imageView, drawingCache);
        manager.updateAppWidget(widgetId, remoteViews);
    }
}
