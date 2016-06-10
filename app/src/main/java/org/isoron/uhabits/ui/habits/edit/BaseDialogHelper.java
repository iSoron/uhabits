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

    @BindView(R.id.tvName)
    TextView tvName;

    @BindView(R.id.tvDescription)
    TextView tvDescription;

    @BindView(R.id.tvFreqNum)
    TextView tvFreqNum;

    @BindView(R.id.tvFreqDen)
    TextView tvFreqDen;

    @BindView(R.id.tvReminderTime)
    TextView tvReminderTime;

    @BindView(R.id.tvReminderDays)
    TextView tvReminderDays;

    @BindView(R.id.sFrequency)
    Spinner sFrequency;

    @BindView(R.id.llCustomFrequency)
    ViewGroup llCustomFrequency;

    @BindView(R.id.llReminderDays)
    ViewGroup llReminderDays;

    public BaseDialogHelper(DialogFragment frag, View view)
    {
        this.frag = frag;
        ButterKnife.bind(this, view);
    }

    void parseFormIntoHabit(Habit habit)
    {
        habit.setName(tvName.getText().toString().trim());
        habit.setDescription(tvDescription.getText().toString().trim());
        String freqNum = tvFreqNum.getText().toString();
        String freqDen = tvFreqDen.getText().toString();
        if (!freqNum.isEmpty()) habit.setFreqNum(Integer.parseInt(freqNum));
        if (!freqDen.isEmpty()) habit.setFreqDen(Integer.parseInt(freqDen));
    }

    void populateColor(int paletteColor)
    {
        tvName.setTextColor(
            ColorUtils.getColor(frag.getContext(), paletteColor));
    }

    protected void populateForm(final Habit habit)
    {
        if (habit.getName() != null) tvName.setText(habit.getName());
        if (habit.getDescription() != null) tvDescription.setText(
            habit.getDescription());

        populateColor(habit.getColor());
        populateFrequencyFields(habit);
        populateReminderFields(habit);
    }

    @SuppressLint("SetTextI18n")
    void populateFrequencyFields(Habit habit)
    {
        int quickSelectPosition = -1;

        if (habit.getFreqNum().equals(habit.getFreqDen())) quickSelectPosition = 0;

        else if (habit.getFreqNum() == 1 && habit.getFreqDen() == 7)
            quickSelectPosition = 1;

        else if (habit.getFreqNum() == 2 && habit.getFreqDen() == 7)
            quickSelectPosition = 2;

        else if (habit.getFreqNum() == 5 && habit.getFreqDen() == 7)
            quickSelectPosition = 3;

        if (quickSelectPosition >= 0)
            showSimplifiedFrequency(quickSelectPosition);

        else showCustomFrequency();

        tvFreqNum.setText(habit.getFreqNum().toString());
        tvFreqDen.setText(habit.getFreqDen().toString());
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

        String time =
            DateUtils.formatTime(frag.getContext(), habit.getReminderHour(),
                habit.getReminderMin());
        tvReminderTime.setText(time);
        llReminderDays.setVisibility(View.VISIBLE);

        boolean weekdays[] = DateUtils.unpackWeekdayList(
            habit.getReminderDays());
        tvReminderDays.setText(
            DateUtils.formatWeekdayList(frag.getContext(), weekdays));
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

        if (habit.getName().length() == 0)
        {
            tvName.setError(
                frag.getString(R.string.validation_name_should_not_be_blank));
            valid = false;
        }

        if (habit.getFreqNum() <= 0)
        {
            tvFreqNum.setError(
                frag.getString(R.string.validation_number_should_be_positive));
            valid = false;
        }

        if (habit.getFreqNum() > habit.getFreqDen())
        {
            tvFreqNum.setError(
                frag.getString(R.string.validation_at_most_one_rep_per_day));
            valid = false;
        }

        return valid;
    }
}
