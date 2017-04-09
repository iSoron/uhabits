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
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

public class ScoreCard extends HabitCard
{
    public static final int[] BUCKET_SIZES = { 1, 7, 31, 92, 365 };

    @BindView(R.id.spinner)
    Spinner spinner;

    @BindView(R.id.scoreView)
    ScoreChart chart;

    @BindView(R.id.title)
    TextView title;

    private int bucketSize;

    @Nullable
    private TaskRunner taskRunner;

    @Nullable
    private Preferences prefs;

    public ScoreCard(Context context)
    {
        super(context);
        init();
    }

    public ScoreCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @NonNull
    public static DateUtils.TruncateField getTruncateField(int bucketSize)
    {
        if (bucketSize == 7) return DateUtils.TruncateField.WEEK_NUMBER;
        if (bucketSize == 31) return DateUtils.TruncateField.MONTH;
        if (bucketSize == 92) return DateUtils.TruncateField.QUARTER;
        if (bucketSize == 365) return DateUtils.TruncateField.YEAR;

        Log.e("ScoreCard",
            String.format("Unknown bucket size: %d", bucketSize));

        return DateUtils.TruncateField.MONTH;
    }

    @OnItemSelected(R.id.spinner)
    public void onItemSelected(int position)
    {
        setBucketSizeFromPosition(position);
        HabitsApplication app =
            (HabitsApplication) getContext().getApplicationContext();
        app.getComponent().getWidgetUpdater().updateWidgets();
        refreshData();
    }

    @Override
    protected void refreshData()
    {
        if(taskRunner == null) return;
        taskRunner.execute(new RefreshTask());
    }

    private int getDefaultSpinnerPosition()
    {
        if(prefs == null) return 0;
        return prefs.getDefaultScoreSpinnerPosition();
    }

    private void init()
    {
        Context appContext = getContext().getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            taskRunner = app.getComponent().getTaskRunner();
            prefs = app.getComponent().getPreferences();
        }

        inflate(getContext(), R.layout.show_habit_score, this);
        ButterKnife.bind(this);

        int defaultPosition = getDefaultSpinnerPosition();
        setBucketSizeFromPosition(defaultPosition);
        spinner.setSelection(defaultPosition);

        if (isInEditMode())
        {
            spinner.setVisibility(GONE);
            title.setTextColor(ColorUtils.getAndroidTestColor(1));
            chart.setColor(ColorUtils.getAndroidTestColor(1));
            chart.populateWithRandomData();
        }
    }

    private void setBucketSizeFromPosition(int position)
    {
        if(prefs == null) return;
        prefs.setDefaultScoreSpinnerPosition(position);
        bucketSize = BUCKET_SIZES[position];
    }

    private class RefreshTask implements Task
    {
        @Override
        public void doInBackground()
        {
            List<Score> scores;
            ScoreList scoreList = getHabit().getScores();

            if (bucketSize == 1) scores = scoreList.toList();
            else scores = scoreList.groupBy(getTruncateField(bucketSize));

            chart.setScores(scores);
            chart.setBucketSize(bucketSize);
        }

        @Override
        public void onPreExecute()
        {
            int color =
                ColorUtils.getColor(getContext(), getHabit().getColor());
            title.setTextColor(color);
            chart.setColor(color);
        }
    }
}
