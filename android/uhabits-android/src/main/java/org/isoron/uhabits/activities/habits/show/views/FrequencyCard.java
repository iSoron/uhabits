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

import androidx.annotation.Nullable;

import org.isoron.uhabits.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

public class FrequencyCard extends HabitCard
{
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.frequencyChart)
    FrequencyChart chart;

    @Nullable
    private Preferences prefs;

    public FrequencyCard(Context context)
    {
        super(context);
        init();
    }

    public FrequencyCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @Override
    protected Task createRefreshTask()
    {
        return new RefreshTask();
    }

    private void init()
    {
        Context appContext = getContext().getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
        }
        inflate(getContext(), R.layout.show_habit_frequency, this);
        ButterKnife.bind(this);
        if (isInEditMode()) initEditMode();
    }

    private void initEditMode()
    {
        int color = PaletteUtils.getAndroidTestColor(1);
        title.setTextColor(color);
        chart.setColor(color);
        chart.populateWithRandomData();
    }

    private class RefreshTask  extends CancelableTask
    {
        @Override
        public void doInBackground()
        {
            if (isCanceled()) return;
            RepetitionList reps = getHabit().getRepetitions();
            HashMap<Timestamp, Integer[]> frequency = reps.getWeekdayFrequency();
            if(prefs != null) chart.setFirstWeekday(prefs.getFirstWeekday());
            chart.setFrequency(frequency);
        }

        @Override
        public void onPreExecute()
        {
            int color = PaletteUtilsKt.toThemedAndroidColor(getHabit().getColor(), getContext());
            title.setTextColor(color);
            chart.setColor(color);
        }
    }
}
