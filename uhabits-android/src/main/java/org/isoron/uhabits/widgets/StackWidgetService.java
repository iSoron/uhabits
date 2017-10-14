package org.isoron.uhabits.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

/**
 * Created by victoryu on 9/30/17.
 */

public class StackWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private ArrayList<Habit> mHabitList;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {

    }

    public void onDestroy() {
        mHabitList.clear();
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
            CheckmarkWidget checkmarkWidget = new CheckmarkWidget(mContext, mAppWidgetId, habit);
            Bundle options = AppWidgetManager.getInstance(mContext).getAppWidgetOptions(mAppWidgetId);
            checkmarkWidget.setDimensions(getDimensionsFromOptions(mContext, options));
            RemoteViews landscape = checkmarkWidget.getLandscapeRemoteViews();
            RemoteViews portrait = checkmarkWidget.getPortraitRemoteViews();
            rv = new RemoteViews(landscape, portrait);
        }

        return rv;
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
