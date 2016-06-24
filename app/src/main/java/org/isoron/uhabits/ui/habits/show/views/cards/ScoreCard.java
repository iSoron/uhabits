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
import android.support.annotation.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.habits.show.views.charts.*;
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

    @OnItemSelected(R.id.spinner)
    public void onItemSelected(int position)
    {
        setBucketSizeFromPosition(position);
        refreshData();
    }

    @Override
    protected void refreshData()
    {
        Habit habit = getHabit();
        int color = ColorUtils.getColor(getContext(), habit.getColor());

        title.setTextColor(color);
        chart.setPrimaryColor(color);

        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                List<Score> scores;

                if (bucketSize == 1) scores = habit.getScores().getAll();
                else scores = habit.getScores().groupBy(getTruncateField());

                chart.setScores(scores);
                chart.setBucketSize(bucketSize);
            }
        }.execute();
    }

    private int getDefaultSpinnerPosition()
    {
        if (isInEditMode()) return 0;
        return InterfaceUtils.getDefaultScoreSpinnerPosition(getContext());
    }

    @NonNull
    private DateUtils.TruncateField getTruncateField()
    {
        if (bucketSize == 7) return DateUtils.TruncateField.WEEK_NUMBER;
        if (bucketSize == 31) return DateUtils.TruncateField.MONTH;
        if (bucketSize == 92) return DateUtils.TruncateField.QUARTER;
        if (bucketSize == 365) return DateUtils.TruncateField.YEAR;

        Log.e("ScoreCard",
            String.format("Unknown bucket size: %d", bucketSize));

        return DateUtils.TruncateField.MONTH;
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_score, this);
        ButterKnife.bind(this);

        int defaultPosition = getDefaultSpinnerPosition();
        setBucketSizeFromPosition(defaultPosition);
        spinner.setSelection(defaultPosition);

        if (isInEditMode())
        {
            spinner.setVisibility(GONE);
            title.setTextColor(ColorUtils.getAndroidTestColor(1));
            chart.setPrimaryColor(ColorUtils.getAndroidTestColor(1));
            chart.populateWithRandomData();
        }
    }

    private void setBucketSizeFromPosition(int position)
    {
        if (isInEditMode()) return;

        InterfaceUtils.setDefaultScoreSpinnerPosition(getContext(), position);
        bucketSize = BUCKET_SIZES[position];
    }
}
