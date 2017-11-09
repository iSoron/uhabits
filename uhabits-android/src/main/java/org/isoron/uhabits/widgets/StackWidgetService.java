package org.isoron.uhabits.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.core.models.Habit;

import java.util.ArrayList;

import static android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT;
import static android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH;
import static android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT;
import static android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH;
import static org.isoron.androidbase.utils.InterfaceUtils.dpToPixels;
import static org.isoron.uhabits.widgets.StackWidgetService.WIDGET_TYPE;

public class StackWidgetService extends RemoteViewsService {

    public static final String WIDGET_TYPE = "WIDGET_TYPE";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private ArrayList<Habit> mHabitList;
    private StackWidgetType mWidgetType;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        int widgetTypeValue = intent.getIntExtra(WIDGET_TYPE, -1);
        if (widgetTypeValue != -1) {
            mWidgetType = StackWidgetType.getWidgetTypeFromValue(widgetTypeValue);
        }
    }

    public void onCreate() {

    }

    public void onDestroy() {

    }

    public int getCount() {
        return mHabitList.size();
    }

    @NonNull
    public WidgetDimensions getDimensionsFromOptions(@NonNull Context ctx,
                                                     @NonNull Bundle options) {
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

    public RemoteViews getViewAt(int position) {
        RemoteViews rv = null;
        if (position < getCount()) {
            Habit habit = mHabitList.get(position);
            BaseWidget widget = initializeWidget(habit);
            Bundle options = AppWidgetManager.getInstance(mContext).getAppWidgetOptions(mAppWidgetId);
            widget.setDimensions(getDimensionsFromOptions(mContext, options));
            final RemoteViews[] landscape = new RemoteViews[1];
            final RemoteViews[] portrait = new RemoteViews[1];

            Object lock = new Object();
            final boolean[] flag = {false};

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        landscape[0] = widget.getLandscapeRemoteViews();
                        portrait[0] = widget.getPortraitRemoteViews();
                        flag[0] = true;
                        lock.notifyAll();
                    }
                }
            });

            synchronized (lock) {
                while (!flag[0]) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {

                    }
                }
            }

            rv = new RemoteViews(landscape[0], portrait[0]);
        }

        return rv;
    }

    private BaseWidget initializeWidget(Habit habit) {
        switch (mWidgetType) {
            case CHECKMARK:
                return new CheckmarkWidget(mContext, mAppWidgetId, habit);
            case FREQUENCY:
                return new FrequencyWidget(mContext, mAppWidgetId, habit);
            case SCORE:
                HabitsApplication app = (HabitsApplication) mContext.getApplicationContext();
                return new ScoreWidget(mContext, mAppWidgetId, habit, app.getComponent().getPreferences());
            case HISTORY:
                return new HistoryWidget(mContext, mAppWidgetId, habit);
            case STREAKS:
                return new StreakWidget(mContext, mAppWidgetId, habit);
        }
        return null;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return false;
    }

    public void onDataSetChanged() {
        mHabitList = new ArrayList<>();
        HabitsApplication app = (HabitsApplication) mContext.getApplicationContext();
        for (Habit h : app.getComponent().getHabitList()) {
            mHabitList.add(h);
        }
    }
}
