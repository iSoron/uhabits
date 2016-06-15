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

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.habits.show.views.HabitDataView;

public class GraphWidgetView extends HabitWidgetView implements HabitDataView
{

    private final HabitDataView dataView;
    private TextView title;

    public GraphWidgetView(Context context, HabitDataView dataView)
    {
        super(context);
        this.dataView = dataView;
        init();
    }

    private void init()
    {
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        ((View) dataView).setLayoutParams(params);

        ViewGroup innerFrame = (ViewGroup) findViewById(R.id.innerFrame);
        innerFrame.addView(((View) dataView));

        title = (TextView) findViewById(R.id.title);
        title.setVisibility(VISIBLE);
    }

    @Override
    public void setHabit(@NonNull Habit habit)
    {
        super.setHabit(habit);
        dataView.setHabit(habit);
        title.setText(habit.getName());
    }

    @Override
    public void refreshData()
    {
        if(habit == null) return;
        dataView.refreshData();
    }

    @NonNull
    protected Integer getInnerLayoutId()
    {
        return R.layout.widget_graph;
    }
}
