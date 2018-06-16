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
import android.support.annotation.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

import static org.isoron.uhabits.activities.habits.show.views.ScoreCard.getTruncateField;

public class BarCard extends HabitCard
{
    public static final int[] NUMERICAL_BUCKET_SIZES = {1, 7, 31, 92, 365};
    public static final int[] BOOLEAN_BUCKET_SIZES = {7, 31, 92, 365};

    @BindView(R.id.numericalSpinner)
    Spinner numericalSpinner;

    @BindView(R.id.boolSpinner)
    Spinner boolSpinner;

    @BindView(R.id.barChart)
    BarChart chart;

    @BindView(R.id.title)
    TextView title;

    @Nullable
    private TaskRunner taskRunner;

    private int bucketSize;

    public BarCard(Context context)
    {
        super(context);
        init();
    }

    public BarCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @OnItemSelected(R.id.numericalSpinner)
    public void onNumericalItemSelected(int position)
    {
        bucketSize = NUMERICAL_BUCKET_SIZES[position];
        refreshData();
    }

    @OnItemSelected(R.id.boolSpinner)
    public void onBoolItemSelected(int position)
    {
        bucketSize = BOOLEAN_BUCKET_SIZES[position];
        refreshData();
    }

    @Override
    protected void refreshData()
    {
        if (taskRunner == null) return;
        taskRunner.execute(new RefreshTask(getHabit()));
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_bar, this);
        ButterKnife.bind(this);

        boolSpinner.setSelection(1);
        numericalSpinner.setSelection(2);
        bucketSize = 7;

        Context appContext = getContext().getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            taskRunner = app.getComponent().getTaskRunner();
        }
    }

    private class RefreshTask implements Task
    {
        private final Habit habit;

        public RefreshTask(Habit habit)
        {
            this.habit = habit;
        }

        @Override
        public void doInBackground()
        {
            List<Checkmark> checkmarks;
            if (bucketSize == 1) checkmarks = habit.getCheckmarks().getAll();
            else checkmarks = habit.getCheckmarks().groupBy(getTruncateField(bucketSize));
            chart.setCheckmarks(checkmarks);
            chart.setBucketSize(bucketSize);
        }

        @Override
        public void onPreExecute()
        {
            int color = PaletteUtils.getColor(getContext(), habit.getColor());
            title.setTextColor(color);
            chart.setColor(color);
            if (habit.isNumerical())
            {
                boolSpinner.setVisibility(GONE);
                chart.setTarget(habit.getTargetValue());
            }
            else
            {
                numericalSpinner.setVisibility(GONE);
                chart.setTarget(0);
            }
        }
    }
}
