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

package org.isoron.uhabits.activities.habits.show.views;

import android.content.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

public class StreakCard extends HabitCard
{
    public static final int NUM_STREAKS = 10;

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
        if (isInEditMode()) initEditMode();
    }

    private void initEditMode()
    {
        int color = PaletteUtils.getAndroidTestColor(1);
        title.setTextColor(color);
        streakChart.setColor(color);
        streakChart.populateWithRandomData();
    }

    @Override
    protected Task createRefreshTask()
    {
        return new RefreshTask();
    }

    private class RefreshTask extends CancelableTask
    {
        List<Streak> bestStreaks;

        @Override
        public void doInBackground()
        {
            if (isCanceled()) return;
            StreakList streaks = getHabit().getStreaks();
            bestStreaks = streaks.getBest(NUM_STREAKS);
        }

        @Override
        public void onPostExecute()
        {
            if (isCanceled()) return;
            streakChart.setStreaks(bestStreaks);
        }

        @Override
        public void onPreExecute()
        {
            int color =
                PaletteUtils.getColor(getContext(), getHabit().getColor());
            title.setTextColor(color);
            streakChart.setColor(color);
        }
    }
}
