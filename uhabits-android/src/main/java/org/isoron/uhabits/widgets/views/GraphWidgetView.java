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

package org.isoron.uhabits.widgets.views;

import android.content.*;
import android.graphics.Color;
import android.support.annotation.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.utils.PaletteUtils;

import java.util.List;

public class GraphWidgetView extends HabitWidgetView
{

    private final View dataView;

    private LinearLayout legend;

    private List<Habit> habits;

    public GraphWidgetView(Context context, View dataView)
    {
        super(context);
        this.dataView = dataView;
        init();
    }

    public View getDataView()
    {
        return dataView;
    }

    public void setHabits(List<Habit> habits) {
        for (Habit habit : habits) {
            TextView t = new TextView(getContext());
            if (habits.size() == 1) {
                t.setTextColor(Color.WHITE);
            } else {
                t.setTextColor(PaletteUtils.getColor(getContext(), habit.getColor()));
            }
            t.setPadding(7, 0, 7, 0);
            t.setText(habit.getName());
            legend.addView(t);
        }
    }

    @Override
    @NonNull
    protected Integer getInnerLayoutId()
    {
        return R.layout.widget_graph;
    }

    private void init()
    {
        ViewGroup.LayoutParams params =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        dataView.setLayoutParams(params);

        ViewGroup innerFrame = (ViewGroup) findViewById(R.id.innerFrame);
        innerFrame.addView(dataView);

        legend = (LinearLayout) findViewById(R.id.legend);
    }
}
