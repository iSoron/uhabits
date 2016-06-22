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
import android.support.annotation.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

public class ScoreCard extends RelativeLayout
    implements ModelObservable.Listener
{
    public static final int[] BUCKET_SIZES = { 1, 7, 31, 92, 365 };

    @BindView(R.id.spinner)
    Spinner spinner;

    @BindView(R.id.scoreView)
    ScoreChart chart;

    @BindView(R.id.title)
    TextView title;

    @Nullable
    private Habit habit;

    private int color;

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
    }

    @Override
    public void onModelChange()
    {
        refreshData();
    }

    public void setHabit(@NonNull Habit habit)
    {
        this.habit = habit;
        color = ColorUtils.getColor(getContext(), habit.getColor());
        refreshData();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (habit != null)
        {
            habit.getObservable().addListener(this);
            habit.getScores().getObservable().addListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (habit != null)
        {
            habit.getObservable().removeListener(this);
            habit.getScores().getObservable().removeListener(this);
        }

        super.onDetachedFromWindow();
    }

    @NonNull
    private DateUtils.TruncateField getTruncateField()
    {
        DateUtils.TruncateField field;

        switch (bucketSize)
        {
            case 7:
                field = DateUtils.TruncateField.WEEK_NUMBER;
                break;

            case 365:
                field = DateUtils.TruncateField.YEAR;
                break;

            case 92:
                field = DateUtils.TruncateField.QUARTER;
                break;

            default:
                Log.e("ScoreCard",
                    String.format("Unknown bucket size: %d", bucketSize));
                // continue to case 31

            case 31:
                field = DateUtils.TruncateField.MONTH;
                break;
        }

        return field;
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_strength, this);
        ButterKnife.bind(this);

        int defaultPosition = getDefaultSpinnerPosition();
        setBucketSizeFromPosition(defaultPosition);
        spinner.setSelection(defaultPosition);

        if(isInEditMode())
        {
            spinner.setVisibility(GONE);
            title.setTextColor(ColorUtils.getAndroidTestColor(1));
            chart.setPrimaryColor(ColorUtils.getAndroidTestColor(1));
            chart.populateWithRandomData();
        }
    }

    private int getDefaultSpinnerPosition()
    {
        if(isInEditMode()) return 0;
        return InterfaceUtils.getDefaultScoreSpinnerPosition(getContext());
    }

    private void refreshData()
    {
        if (habit == null) return;

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

    private void setBucketSizeFromPosition(int position)
    {
        if(isInEditMode()) return;

        InterfaceUtils.setDefaultScoreSpinnerPosition(getContext(), position);
        bucketSize = BUCKET_SIZES[position];
        refreshData();
    }
}
