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
import android.content.Context;
import android.view.View;

import org.isoron.uhabits.HabitBroadcastReceiver;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.views.GraphWidgetView;
import org.isoron.uhabits.views.HabitDataView;
import org.isoron.uhabits.views.HabitScoreView;

public class ScoreWidgetProvider extends BaseWidgetProvider
{
    @Override
    protected View buildCustomView(Context context, Habit habit)
    {
        HabitScoreView dataView = new HabitScoreView(context);
        dataView.setIsTransparencyEnabled(true);
        GraphWidgetView view = new GraphWidgetView(context, dataView);
        view.setHabit(habit);
        return view;
    }

    @Override
    protected void refreshCustomViewData(View view)
    {
        ((HabitDataView) view).refreshData();
    }

    @Override
    protected PendingIntent getOnClickPendingIntent(Context context, Habit habit)
    {
        return HabitBroadcastReceiver.buildViewHabitIntent(context, habit);
    }

    @Override
    protected int getDefaultHeight()
    {
        return 200;
    }

    @Override
    protected int getDefaultWidth()
    {
        return 200;
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.widget_wrapper;
    }
}
