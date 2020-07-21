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

import androidx.annotation.NonNull;

import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.utils.*;

import butterknife.*;

public class OverviewCard extends HabitCard
{
    @NonNull
    private Cache cache;

    @BindView(R.id.scoreRing)
    RingView scoreRing;

    @BindView(R.id.scoreLabel)
    TextView scoreLabel;

    @BindView(R.id.monthDiffLabel)
    TextView monthDiffLabel;

    @BindView(R.id.yearDiffLabel)
    TextView yearDiffLabel;

    @BindView(R.id.totalCountLabel)
    TextView totalCountLabel;

    @BindView(R.id.title)
    TextView title;

    private int color;

    public OverviewCard(Context context)
    {
        super(context);
        init();
    }

    public OverviewCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private String formatPercentageDiff(float percentageDiff)
    {
        return String.format("%s%.0f%%", (percentageDiff >= 0 ? "+" : "\u2212"),
            Math.abs(percentageDiff) * 100);
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_overview, this);
        ButterKnife.bind(this);
        cache = new Cache();
        if (isInEditMode()) initEditMode();
    }

    private void initEditMode()
    {
        color = PaletteUtils.getAndroidTestColor(1);
        cache.todayScore = 0.6f;
        cache.lastMonthScore = 0.42f;
        cache.lastYearScore = 0.75f;
        refreshColors();
        refreshScore();
    }

    private void refreshColors()
    {
        scoreRing.setColor(color);
        scoreLabel.setTextColor(color);
        title.setTextColor(color);
    }

    private void refreshScore()
    {
        float todayPercentage = cache.todayScore;
        float monthDiff = todayPercentage - cache.lastMonthScore;
        float yearDiff = todayPercentage - cache.lastYearScore;

        scoreRing.setPercentage(todayPercentage);
        scoreLabel.setText(String.format("%.0f%%", todayPercentage * 100));

        monthDiffLabel.setText(formatPercentageDiff(monthDiff));
        yearDiffLabel.setText(formatPercentageDiff(yearDiff));
        totalCountLabel.setText(String.valueOf(cache.totalCount));

        StyledResources res = new StyledResources(getContext());
        int inactiveColor = res.getColor(R.attr.mediumContrastTextColor);

        monthDiffLabel.setTextColor(monthDiff >= 0 ? color : inactiveColor);
        yearDiffLabel.setTextColor(yearDiff >= 0 ? color : inactiveColor);
        totalCountLabel.setTextColor(yearDiff >= 0 ? color : inactiveColor);

        postInvalidate();
    }

    private class Cache
    {
        float todayScore;

        float lastMonthScore;

        float lastYearScore;

        long totalCount;
    }

    @Override
    protected Task createRefreshTask()
    {
        return new RefreshTask();
    }

    private class RefreshTask  extends CancelableTask
    {
        @Override
        public void doInBackground()
        {
            if (isCanceled()) return;
            Habit habit = getHabit();

            ScoreList scores = habit.getScores();

            Timestamp today = DateUtils.getToday();
            Timestamp lastMonth = today.minus(30);
            Timestamp lastYear = today.minus(365);

            cache.todayScore = (float) scores.getTodayValue();
            cache.lastMonthScore = (float) scores.getValue(lastMonth);
            cache.lastYearScore = (float) scores.getValue(lastYear);
            cache.totalCount = habit.getRepetitions().getTotalSuccessfulCount();
        }

        @Override
        public void onPostExecute()
        {
            if (isCanceled()) return;
            refreshScore();
        }

        @Override
        public void onPreExecute()
        {
            color = PaletteUtils.getColor(getContext(), getHabit().getColor());
            refreshColors();
        }
    }
}
