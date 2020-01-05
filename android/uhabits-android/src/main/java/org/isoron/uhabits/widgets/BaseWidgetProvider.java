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

import android.appwidget.*;
import android.content.*;
import android.os.*;
import androidx.annotation.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;

import java.util.*;

import static android.appwidget.AppWidgetManager.*;
import static org.isoron.androidbase.utils.InterfaceUtils.dpToPixels;

public abstract class BaseWidgetProvider extends AppWidgetProvider
{
    private HabitList habits;

    private Preferences preferences;

    private WidgetPreferences widgetPrefs;

    public static void updateAppWidget(@NonNull AppWidgetManager manager,
                                       @NonNull BaseWidget widget)
    {
        RemoteViews landscape = widget.getLandscapeRemoteViews();
        RemoteViews portrait = widget.getPortraitRemoteViews();
        RemoteViews views = new RemoteViews(landscape, portrait);
        manager.updateAppWidget(widget.getId(), views);
    }

    @NonNull
    public WidgetDimensions getDimensionsFromOptions(@NonNull Context ctx,
                                                     @NonNull Bundle options)
    {
        int maxWidth =
            (int) dpToPixels(ctx, options.getInt(OPTION_APPWIDGET_MAX_WIDTH));
        int maxHeight =
            (int) dpToPixels(ctx, options.getInt(OPTION_APPWIDGET_MAX_HEIGHT));
        int minWidth =
            (int) dpToPixels(ctx, options.getInt(OPTION_APPWIDGET_MIN_WIDTH));
        int minHeight =
            (int) dpToPixels(ctx, options.getInt(OPTION_APPWIDGET_MIN_HEIGHT));

        return new WidgetDimensions(minWidth, maxHeight, maxWidth, minHeight);
    }

    @Override
    public void onAppWidgetOptionsChanged(@Nullable Context context,
                                          @Nullable AppWidgetManager manager,
                                          int widgetId,
                                          @Nullable Bundle options)
    {
        try
        {
            if (context == null) throw new RuntimeException("context is null");
            if (manager == null) throw new RuntimeException("manager is null");
            if (options == null) throw new RuntimeException("options is null");
            updateDependencies(context);
            context.setTheme(R.style.WidgetTheme);

            BaseWidget widget = getWidgetFromId(context, widgetId);
            WidgetDimensions dims = getDimensionsFromOptions(context, options);
            widget.setDimensions(dims);
            updateAppWidget(manager, widget);
        }
        catch (RuntimeException e)
        {
            drawErrorWidget(context, manager, widgetId, e);
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleted(@Nullable Context context, @Nullable int[] ids)
    {
        if (context == null) throw new RuntimeException("context is null");
        if (ids == null) throw new RuntimeException("ids is null");

        updateDependencies(context);

        for (int id : ids)
        {
            try
            {
                BaseWidget widget = getWidgetFromId(context, id);
                widget.delete();
            }
            catch (HabitNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdate(@Nullable Context context,
                         @Nullable AppWidgetManager manager,
                         @Nullable int[] widgetIds)
    {
        if (context == null) throw new RuntimeException("context is null");
        if (manager == null) throw new RuntimeException("manager is null");
        if (widgetIds == null) throw new RuntimeException("widgetIds is null");
        updateDependencies(context);
        context.setTheme(R.style.WidgetTheme);

        new Thread(() ->
        {
            Looper.prepare();
            for (int id : widgetIds)
                update(context, manager, id);
        }).start();
    }

    protected List<Habit> getHabitsFromWidgetId(int widgetId)
    {
        long selectedIds[] = widgetPrefs.getHabitIdsFromWidgetId(widgetId);
        ArrayList<Habit> selectedHabits = new ArrayList<>(selectedIds.length);
        for (long id : selectedIds)
        {
            Habit h = habits.getById(id);
            if (h == null) throw new HabitNotFoundException();
            selectedHabits.add(h);
        }

        return selectedHabits;
    }

    @NonNull
    protected abstract BaseWidget getWidgetFromId(@NonNull Context context,
                                                  int id);

    private void drawErrorWidget(Context context,
                                 AppWidgetManager manager,
                                 int widgetId,
                                 RuntimeException e)
    {
        RemoteViews errorView =
            new RemoteViews(context.getPackageName(), R.layout.widget_error);

        if (e instanceof HabitNotFoundException)
        {
            errorView.setCharSequence(R.id.label, "setText",
                context.getString(R.string.habit_not_found));
        }

        manager.updateAppWidget(widgetId, errorView);
    }

    private void update(@NonNull Context context,
                        @NonNull AppWidgetManager manager,
                        int widgetId)
    {
        try
        {
            BaseWidget widget = getWidgetFromId(context, widgetId);
            Bundle options = manager.getAppWidgetOptions(widgetId);
            widget.setDimensions(getDimensionsFromOptions(context, options));
            updateAppWidget(manager, widget);
        }
        catch (RuntimeException e)
        {
            drawErrorWidget(context, manager, widgetId, e);
            e.printStackTrace();
        }
    }

    private void updateDependencies(Context context)
    {
        HabitsApplication app =
            (HabitsApplication) context.getApplicationContext();
        habits = app.getComponent().getHabitList();
        preferences = app.getComponent().getPreferences();
        widgetPrefs = app.getComponent().getWidgetPreferences();
    }

    public Preferences getPreferences()
    {
        return preferences;
    }
}
