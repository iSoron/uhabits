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

package org.isoron.uhabits.ui.habits.edit;

import android.annotation.SuppressLint;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.utils.ColorUtils;
import org.isoron.uhabits.utils.DateUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseDialogHelper
{
    private DialogFragment frag;
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.tvDescription) TextView tvDescription;
    @BindView(R.id.tvFreqNum) TextView tvFreqNum;
    @BindView(R.id.tvFreqDen) TextView tvFreqDen;
    @BindView(R.id.tvReminderTime) TextView tvReminderTime;
    @BindView(R.id.tvReminderDays) TextView tvReminderDays;
    @BindView(R.id.sFrequency) Spinner sFrequency;
    @BindView(R.id.llCustomFrequency) ViewGroup llCustomFrequency;
    @BindView(R.id.llReminderDays) ViewGroup llReminderDays;

    public BaseDialogHelper(DialogFragment frag, View view)
    {
        this.frag = frag;
        ButterKnife.bind(this, view);
    }

    protected void populateForm(final Habit habit)
    {
        if(habit.name != null) tvName.setText(habit.name);
        if(habit.description != null) tvDescription.setText(habit.description);

        populateColor(habit.color);
        populateFrequencyFields(habit);
        populateReminderFields(habit);
    }

    void populateColor(int paletteColor)
    {
        tvName.setTextColor(ColorUtils.getColor(frag.getContext(), paletteColor));
    }

    @SuppressWarnings("ConstantConditions")
    void populateReminderFields(Habit habit)
    {
        if (!habit.hasReminder())
        {
            tvReminderTime.setText(R.string.reminder_off);
            llReminderDays.setVisibility(View.GONE);
            return;
        }

        String time = DateUtils.formatTime(frag.getContext(), habit.reminderHour, habit.reminderMin);
        tvReminderTime.setText(time);
        llReminderDays.setVisibility(View.VISIBLE);

        boolean weekdays[] = DateUtils.unpackWeekdayList(habit.reminderDays);
        tvReminderDays.setText(DateUtils.formatWeekdayList(frag.getContext(), weekdays));
    }

    @SuppressLint("SetTextI18n")
    void populateFrequencyFields(Habit habit)
    {
        int quickSelectPosition = -1;

        if(habit.freqNum.equals(habit.freqDen))
            quickSelectPosition = 0;

        else if(habit.freqNum == 1 && habit.freqDen == 7)
            quickSelectPosition = 1;

        else if(habit.freqNum == 2 && habit.freqDen == 7)
            quickSelectPosition = 2;

        else if(habit.freqNum == 5 && habit.freqDen == 7)
            quickSelectPosition = 3;

        if(quickSelectPosition >= 0) showSimplifiedFrequency(quickSelectPosition);
        else showCustomFrequency();

        tvFreqNum.setText(habit.freqNum.toString());
        tvFreqDen.setText(habit.freqDen.toString());
    }

    private void showCustomFrequency()
    {
        sFrequency.setVisibility(View.GONE);
        llCustomFrequency.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void showSimplifiedFrequency(int quickSelectPosition)
    {
        sFrequency.setVisibility(View.VISIBLE);
        sFrequency.setSelection(quickSelectPosition);
        llCustomFrequency.setVisibility(View.GONE);
    }

    boolean validate(Habit habit)
    {
        Boolean valid = true;

        if (habit.name.length() == 0)
        {
            tvName.setError(frag.getString(R.string.validation_name_should_not_be_blank));
            valid = false;
        }

        if (habit.freqNum <= 0)
        {
            tvFreqNum.setError(frag.getString(R.string.validation_number_should_be_positive));
            valid = false;
        }

        if (habit.freqNum > habit.freqDen)
        {
            tvFreqNum.setError(frag.getString(R.string.validation_at_most_one_rep_per_day));
            valid = false;
        }

        return valid;
    }

    void parseFormIntoHabit(Habit habit)
    {
        habit.name = tvName.getText().toString().trim();
        habit.description = tvDescription.getText().toString().trim();
        String freqNum = tvFreqNum.getText().toString();
        String freqDen = tvFreqDen.getText().toString();
        if(!freqNum.isEmpty()) habit.freqNum =  Integer.parseInt(freqNum);
        if(!freqDen.isEmpty()) habit.freqDen = Integer.parseInt(freqDen);
    }
}
