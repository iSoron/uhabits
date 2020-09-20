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
import android.content.res.*;
import android.util.*;
import android.widget.*;

import androidx.annotation.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

public class TargetCard extends HabitCard
{
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.targetChart)
    TargetChart targetChart;

    int firstWeekday = Calendar.SATURDAY;

    public TargetCard(Context context)
    {
        super(context);
        init();
    }

    public TargetCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_target, this);
        ButterKnife.bind(this);
        setOrientation(VERTICAL);

        Context app = getContext().getApplicationContext();
        if (app instanceof HabitsApplication) {
            HabitsApplication habitsApp = (HabitsApplication) app;
            Preferences prefs = habitsApp.getComponent().getPreferences();
            firstWeekday = prefs.getFirstWeekday();
        }

        if (isInEditMode()) initEditMode();
    }

    private void initEditMode()
    {
        int color = PaletteUtils.getAndroidTestColor(1);
        title.setTextColor(color);
        targetChart.setColor(color);
        targetChart.populateWithRandomData();
    }

    @Override
    protected Task createRefreshTask()
    {
        return new RefreshTask(getContext(), getHabit(), firstWeekday, targetChart, title);
    }

    public static class RefreshTask extends CancelableTask
    {
        double todayValue;
        double thisWeekValue;
        double thisMonthValue;
        double thisQuarterValue;
        double thisYearValue;

        private Context context;
        private Habit habit;
        private int firstWeekday;
        private TargetChart chart;
        private TextView title;

        public RefreshTask(@NonNull Context context,
                           @NonNull Habit habit,
                           int firstWeekday,
                           @NonNull TargetChart chart,
                           @Nullable TextView title)
        {
            this.context = context;
            this.habit = habit;
            this.firstWeekday = firstWeekday;
            this.chart = chart;
            this.title = title;
        }

        @Override
        public void doInBackground()
        {
            if (isCanceled()) return;
            CheckmarkList checkmarks = habit.getCheckmarks();
            todayValue = checkmarks.getTodayValue() / 1e3;
            thisWeekValue = checkmarks.getThisWeekValue(firstWeekday) / 1e3;
            thisMonthValue = checkmarks.getThisMonthValue() / 1e3;
            thisQuarterValue = checkmarks.getThisQuarterValue() / 1e3;
            thisYearValue = checkmarks.getThisYearValue() / 1e3;
        }

        @Override
        public void onPostExecute()
        {
            if (isCanceled()) return;
            Calendar cal = DateUtils.getStartOfTodayCalendarWithOffset();
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int daysInQuarter = 91;
            int daysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR);

            int den = habit.getFrequency().getDenominator();
            double dailyTarget = habit.getTargetValue() / den;
            Resources res = context.getResources();

            ArrayList<Double> values = new ArrayList<>();
            if (den <= 1) values.add(todayValue);
            if (den <= 7) values.add(thisWeekValue);
            values.add(thisMonthValue);
            values.add(thisQuarterValue);
            values.add(thisYearValue);
            chart.setValues(values);

            ArrayList<Double> targets = new ArrayList<>();
            if (den <= 1) targets.add(dailyTarget);
            if (den <= 7) targets.add(dailyTarget * 7);
            targets.add(dailyTarget * daysInMonth);
            targets.add(dailyTarget * daysInQuarter);
            targets.add(dailyTarget * daysInYear);
            chart.setTargets(targets);

            ArrayList<String> labels = new ArrayList<>();
            if (den <= 1) labels.add(res.getString(R.string.today));
            if (den <= 7) labels.add(res.getString(R.string.week));
            labels.add(res.getString(R.string.month));
            labels.add(res.getString(R.string.quarter));
            labels.add(res.getString(R.string.year));
            chart.setLabels(labels);
        }

        @Override
        public void onPreExecute()
        {
            int color = PaletteUtils.getColor(context, habit.getColor());
            if(title != null) title.setTextColor(color);
            chart.setColor(color);
        }
    }
}
