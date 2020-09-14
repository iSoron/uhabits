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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import org.isoron.androidbase.utils.StyledResources;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.common.views.RingView;
import org.isoron.uhabits.core.commands.CommandRunner;
import org.isoron.uhabits.core.commands.CreateRepetitionCommand;
import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.core.models.HabitList;
import org.isoron.uhabits.core.models.ScoreList;
import org.isoron.uhabits.core.models.Timestamp;
import org.isoron.uhabits.core.tasks.CancelableTask;
import org.isoron.uhabits.core.tasks.Task;
import org.isoron.uhabits.core.utils.DateUtils;
import org.isoron.uhabits.utils.PaletteUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    @BindView(R.id.checkHabitTodayButton)
    Button checkHabitTodayButton;

    private int color;
    private CommandRunner commandRunner;
    private HabitList habitList;

    private volatile boolean habitIsCheckedForToday;

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

    @OnClick(R.id.checkHabitTodayButton)
    public void onClickFeedback()
    {
        int newValue = this.habitIsCheckedForToday ? 0 : 1;
        final Habit habit = getHabit();
        commandRunner.execute(
                new CreateRepetitionCommand(habitList, habit, DateUtils.getToday(), newValue),
                habit.getId()
        );
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
        commandRunner = component.getCommandRunner();
        commandRunner.addListener((command, refreshKey) -> {
            if(command instanceof  CreateRepetitionCommand) {
                refreshData();
            }
        });
        habitList = component.getHabitList();
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


    private void refreshHabitChecked()
    {
       this.habitIsCheckedForToday = cache.habitIsCheckedForToday;

       @StringRes int checkHabitButtonTextRes =
               this.habitIsCheckedForToday ? R.string.mark_undone_for_today :
                       R.string.mark_done_for_today;
       checkHabitTodayButton.setText(checkHabitButtonTextRes);
    }

    private class Cache
    {
        float todayScore;

        float lastMonthScore;

        float lastYearScore;

        long totalCount;

        boolean habitIsCheckedForToday;
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
            cache.totalCount = habit.getRepetitions().getTotalCount();
            cache.habitIsCheckedForToday = habit.getCheckmarks().getTodayValue() == 1;
        }

        @Override
        public void onPostExecute()
        {
            if (isCanceled()) return;
            refreshScore();
            refreshHabitChecked();
        }

        @Override
        public void onPreExecute()
        {
            color = PaletteUtils.getColor(getContext(), getHabit().getColor());
            refreshColors();
        }
    }
}
