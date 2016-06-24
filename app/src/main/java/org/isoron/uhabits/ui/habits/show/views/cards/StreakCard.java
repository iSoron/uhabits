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

package org.isoron.uhabits.ui.habits.show.views.cards;

import android.content.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.habits.show.views.charts.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

public class StreakCard extends HabitCard
{
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.streakChart)
    StreakChart streakChart;

    public StreakCard(Context context)
    {
        super(context);
        init();
    }

    public StreakCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_streak, this);
        ButterKnife.bind(this);
        setOrientation(VERTICAL);
        if(isInEditMode()) initEditMode();
    }

    private void initEditMode()
    {
        int color = ColorUtils.getAndroidTestColor(1);
        title.setTextColor(color);
        streakChart.setColor(color);
        streakChart.populateWithRandomData();
    }

    @Override
    protected void refreshData()
    {
        Habit habit = getHabit();
        int color = ColorUtils.getColor(getContext(), habit.getColor());

        title.setTextColor(color);
        streakChart.setColor(color);

        new BaseTask()
        {
            public List<Streak> streaks;

            @Override
            protected void doInBackground()
            {
                streaks = habit.getStreaks().getBest(10);
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                streakChart.setStreaks(streaks);
                super.onPostExecute(aVoid);
            }
        }.execute();
    }
}
