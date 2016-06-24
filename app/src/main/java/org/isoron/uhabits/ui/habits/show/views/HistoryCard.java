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

package org.isoron.uhabits.ui.habits.show.views;

import android.content.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.common.views.*;
import org.isoron.uhabits.utils.*;

import butterknife.*;

public class HistoryCard extends HabitCard
{
    @BindView(R.id.historyChart)
    HistoryChart chart;

    @BindView(R.id.title)
    TextView title;

    public HistoryCard(Context context)
    {
        super(context);
        init();
    }

    public HistoryCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @OnClick(R.id.edit)
    public void onClickEditButton()
    {
        Log.d("HistoryCard", "onClickEditButton");

//        HistoryEditorDialog frag = new HistoryEditorDialog();
//        frag.setHabit(habit);
//        frag.show(getContext().getFragmentManager(), "historyEditor");
    }

    @Override
    protected void refreshData()
    {
        Habit habit = getHabit();

        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                int checkmarks[] = habit.getCheckmarks().getAllValues();
                chart.setCheckmarks(checkmarks);
            }

            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                int color = ColorUtils.getColor(getContext(), habit.getColor());
                title.setTextColor(color);
                chart.setColor(color);
            }
        }.execute();
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_history, this);
        ButterKnife.bind(this);

        if (isInEditMode()) initEditMode();
    }

    private void initEditMode()
    {
        int color = ColorUtils.getAndroidTestColor(1);
        title.setTextColor(color);
        chart.setColor(color);
        chart.populateWithRandomData();
    }
}
