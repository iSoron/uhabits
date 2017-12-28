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

import android.annotation.*;
import android.content.*;
import android.content.res.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.utils.*;

import butterknife.*;

public class SubtitleCard extends HabitCard
{
    @BindView(R.id.questionLabel)
    TextView questionLabel;

    @BindView(R.id.frequencyLabel)
    TextView frequencyLabel;

    @BindView(R.id.reminderLabel)
    TextView reminderLabel;

    public SubtitleCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @Override
    protected void refreshData()
    {
        Habit habit = getHabit();
        int color = PaletteUtils.getColor(getContext(), habit.getColor());

        reminderLabel.setText(getResources().getString(R.string.reminder_off));
        questionLabel.setVisibility(VISIBLE);

        questionLabel.setTextColor(color);
        questionLabel.setText(habit.getDescription());
        frequencyLabel.setText(toText(habit.getFrequency()));

        if (habit.hasReminder()) updateReminderText(habit.getReminder());

        if (habit.getDescription().isEmpty()) questionLabel.setVisibility(GONE);

        invalidate();
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_subtitle, this);
        ButterKnife.bind(this);

        if (isInEditMode()) initEditMode();
    }

    @SuppressLint("SetTextI18n")
    private void initEditMode()
    {
        questionLabel.setTextColor(PaletteUtils.getAndroidTestColor(1));
        questionLabel.setText("Have you meditated today?");
        reminderLabel.setText("08:00");
    }

    private String toText(Frequency freq)
    {
        Resources resources = getResources();
        int num = freq.getNumerator();
        int den = freq.getDenominator();

        if (den == 1) {
            return resources.getQuantityString(R.plurals.times_a_day, num, num);
        } else if (den == 7) {
            return resources.getQuantityString(R.plurals.times_a_week, num, num);
        } else if (den == 28) {
            return resources.getQuantityString(R.plurals.times_a_month, num, num);
        } else {
            return resources.getQuantityString(R.plurals.times_unknown_interval, num, num, den);
        }
    }

    private void updateReminderText(Reminder reminder)
    {
        reminderLabel.setText(
            AndroidDateUtils.formatTime(getContext(), reminder.getHour(),
                reminder.getMinute()));
    }
}
