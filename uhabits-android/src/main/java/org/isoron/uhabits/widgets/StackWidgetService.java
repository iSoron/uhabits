package org.isoron.uhabits.widgets;

import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.*;

import java.util.*;

import static android.appwidget.AppWidgetManager.*;
import static org.isoron.androidbase.utils.InterfaceUtils.dpToPixels;
import static org.isoron.uhabits.widgets.StackWidgetService.*;

public class StackWidgetService extends RemoteViewsService
{
    public static final String WIDGET_TYPE = "WIDGET_TYPE";
    public static final String HABIT_IDS = "HABIT_IDS";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
    private Context context;
    private int widgetId;
    private long[] habitIds;
    private ArrayList<Habit> habits = new ArrayList<>();
    private StackWidgetType widgetType;

    public StackRemoteViewsFactory(Context context, Intent intent)
    {
        this.context = context;
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                      AppWidgetManager.INVALID_APPWIDGET_ID);
        int widgetTypeValue = intent.getIntExtra(WIDGET_TYPE, -1);
        String habitIdsStr = intent.getStringExtra(HABIT_IDS);

        if (widgetTypeValue < 0) throw new RuntimeException("invalid widget type");
        if (habitIdsStr == null) throw new RuntimeException("habitIdsStr is null");

        widgetType = StackWidgetType.getWidgetTypeFromValue(widgetTypeValue);
        habitIds = StringUtils.splitLongs(habitIdsStr);
    }

    public void onCreate()
    {

    }

    public void onDestroy()
    {

    }

    public int getCount()
    {
        return habits.size();
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

    public RemoteViews getViewAt(int position)
    {
        RemoteViews rv = null;
        if (position < getCount())
        {
            Habit habit = habits.get(position);
            BaseWidget widget = initializeWidget(habit);
            Bundle options =
                AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId);
            widget.setDimensions(getDimensionsFromOptions(context, options));
            final RemoteViews[] landscape = new RemoteViews[1];
            final RemoteViews[] portrait = new RemoteViews[1];

            Object lock = new Object();
            final boolean[] flag = {false};

            new Handler(Looper.getMainLooper()).post(() ->
                                                     {
                                                         synchronized (lock)
                                                         {
                                                             landscape[0] =
                                                                 widget.getLandscapeRemoteViews();
                                                             portrait[0] =
                                                                 widget.getPortraitRemoteViews();
                                                             flag[0] = true;
                                                             lock.notifyAll();
                                                         }
                                                     });

            synchronized (lock)
            {
                while (!flag[0])
                {
                    try
                    {
                        lock.wait();
                    }
                    catch (InterruptedException e)
                    {
                        // ignored
                    }
                }
            }

            rv = new RemoteViews(landscape[0], portrait[0]);
        }

        return rv;
    }

    private BaseWidget initializeWidget(Habit habit)
    {
        switch (widgetType)
        {
            case CHECKMARK:
                return new CheckmarkWidget(context, widgetId, habit);
            case FREQUENCY:
                return new FrequencyWidget(context, widgetId, habit);
            case SCORE:
                return new ScoreWidget(context, widgetId, habit);
            case HISTORY:
                return new HistoryWidget(context, widgetId, habit);
            case STREAKS:
                return new StreakWidget(context, widgetId, habit);
        }
        return null;
    }

    public RemoteViews getLoadingView()
    {
        return null;
    }

    public int getViewTypeCount()
    {
        return 1;
    }

    public long getItemId(int position)
    {
        return position;
    }

    public boolean hasStableIds()
    {
        return false;
    }

    public void onDataSetChanged()
    {
        habits.clear();
        HabitsApplication app = (HabitsApplication) context.getApplicationContext();
        HabitList habitList = app.getComponent().getHabitList();

        for (long id : habitIds)
        {
            Habit h = habitList.getById(id);
            if (h == null) throw new HabitNotFoundException();
            habits.add(h);
        }
    }
}
