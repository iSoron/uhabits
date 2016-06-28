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

import android.content.*;
import android.support.annotation.*;

import org.apache.commons.lang3.*;
import org.isoron.uhabits.ui.widgets.*;

public class ScoreWidgetProvider extends BaseWidgetProvider
{
    @NonNull
    @Override
    protected BaseWidget getWidgetFromId(@NonNull Context context, int id)
    {
        throw new NotImplementedException("");
    }

//    @Override
//    protected View buildCustomView(Context context, Habit habit)
//    {
//        ScoreChart dataView = new ScoreChart(context);
//        GraphWidgetView view = new GraphWidgetView(context, dataView);
//        view.setTitle(habit.getName());
//        return view;
//    }
//
//    @Override
//    protected int getDefaultHeight()
//    {
//        return 300;
//    }
//
//    @Override
//    protected int getDefaultWidth()
//    {
//        return 300;
//    }
//
//    @Override
//    protected int getLayoutId()
//    {
//        return R.layout.widget_wrapper;
//    }
//
//    @Override
//    protected PendingIntent getOnClickPendingIntent(Context context,
//                                                    Habit habit)
//    {
//        return HabitBroadcastReceiver.buildViewHabitIntent(context, habit);
//    }
//
//    @Override
//    protected void refreshCustomViewData(Context context,
//                                         View view,
//                                         Habit habit)
//    {
//        int defaultScoreInterval =
//            InterfaceUtils.getDefaultScoreSpinnerPosition(context);
//        int size = ScoreCard.BUCKET_SIZES[defaultScoreInterval];
//
//        GraphWidgetView widgetView = (GraphWidgetView) view;
//        ScoreChart chart = (ScoreChart) widgetView.getDataView();
//
//        int color = ColorUtils.getColor(context, habit.getColor());
//        List<Score> scores = habit.getScores().getAll();
//
//        chart.setIsTransparencyEnabled(true);
//        chart.setBucketSize(size);
//        chart.setColor(color);
//        chart.setScores(scores);
//    }
}
