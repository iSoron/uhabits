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
import android.support.annotation.*;
import android.view.*;

import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;
import org.isoron.uhabits.widgets.views.*;

public class HistoryWidget extends BaseWidget
{
    @NonNull
    private Habit habit;

    public HistoryWidget(@NonNull Context context, int id, @NonNull Habit habit)
    {
        super(context, id);
        this.habit = habit;
    }

    @Override
    public PendingIntent getOnClickPendingIntent(Context context)
    {
        return pendingIntentFactory.showHabit(habit);
    }

    @Override
    public void refreshData(View view)
    {
        GraphWidgetView widgetView = (GraphWidgetView) view;
        HistoryChart chart = (HistoryChart) widgetView.getDataView();

        int color = ColorUtils.getColor(getContext(), habit.getColor());
        int[] values = habit.getCheckmarks().getAllValues();

        chart.setColor(color);
        chart.setCheckmarks(values);
    }

    @Override
    protected View buildView()
    {
        HistoryChart dataView = new HistoryChart(getContext());
        GraphWidgetView widgetView =
            new GraphWidgetView(getContext(), dataView);
        widgetView.setTitle(habit.getName());
        return widgetView;
    }

    @Override
    protected int getDefaultHeight()
    {
        return 250;
    }

    @Override
    protected int getDefaultWidth()
    {
        return 250;
    }
}
