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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.Command;
import org.isoron.uhabits.commands.CreateHabitCommand;
import org.isoron.uhabits.commands.EditHabitCommand;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.utils.ColorUtils;
import org.isoron.uhabits.utils.InterfaceUtils;

import java.util.Arrays;

public class EditHabitDialogFragment extends AppCompatDialogFragment
        implements OnClickListener, WeekdayPickerDialog.OnWeekdaysPickedListener,
        TimePickerDialog.OnTimeSetListener, Spinner.OnItemSelectedListener
{
    private Integer mode;
    static final int EDIT_MODE = 0;
    static final int CREATE_MODE = 1;

    private InterfaceUtils.OnSavedListener onSavedListener;

    private Habit originalHabit;
    private Habit modifiedHabit;

    private TextView tvName;
    private TextView tvDescription;
    private TextView tvFreqNum;
    private TextView tvFreqDen;
    private TextView tvReminderTime;
    private TextView tvReminderDays;

    private Spinner sFrequency;
    private ViewGroup llCustomFrequency;
    private ViewGroup llReminderDays;

    private SharedPreferences prefs;
    private boolean is24HourMode;

    public static EditHabitDialogFragment editSingleHabitFragment(long id)
    {
        EditHabitDialogFragment frag = new EditHabitDialogFragment();
        Bundle args = new Bundle();
        args.putLong("habitId", id);
        args.putInt("editMode", EDIT_MODE);
        frag.setArguments(args);
        return frag;
    }

    public static EditHabitDialogFragment createHabitFragment()
    {
        EditHabitDialogFragment frag = new EditHabitDialogFragment();
        Bundle args = new Bundle();
        args.putInt("editMode", CREATE_MODE);
        frag.setArguments(args);
        return frag;
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.edit_habit, container, false);
        tvName = (TextView) view.findViewById(R.id.input_name);
        tvDescription = (TextView) view.findViewById(R.id.input_description);
        tvFreqNum = (TextView) view.findViewById(R.id.input_freq_num);
        tvFreqDen = (TextView) view.findViewById(R.id.input_freq_den);
        tvReminderTime = (TextView) view.findViewById(R.id.inputReminderTime);
        tvReminderDays = (TextView) view.findViewById(R.id.inputReminderDays);

        sFrequency = (Spinner) view.findViewById(R.id.sFrequency);
        llCustomFrequency = (ViewGroup) view.findViewById(R.id.llCustomFrequency);
        llReminderDays = (ViewGroup) view.findViewById(R.id.llReminderDays);

        Button buttonSave = (Button) view.findViewById(R.id.buttonSave);
        Button buttonDiscard = (Button) view.findViewById(R.id.buttonDiscard);
        ImageButton buttonPickColor = (ImageButton) view.findViewById(R.id.buttonPickColor);

        buttonSave.setOnClickListener(this);
        buttonDiscard.setOnClickListener(this);
        tvReminderTime.setOnClickListener(this);
        tvReminderDays.setOnClickListener(this);
        buttonPickColor.setOnClickListener(this);
        sFrequency.setOnItemSelectedListener(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Bundle args = getArguments();
        mode = (Integer) args.get("editMode");

        is24HourMode = DateFormat.is24HourFormat(getActivity());

        if (mode == CREATE_MODE)
        {
            getDialog().setTitle(R.string.create_habit);
            modifiedHabit = new Habit();
            modifiedHabit.freqNum = 1;
            modifiedHabit.freqDen = 1;
            modifiedHabit.color = prefs.getInt("pref_default_habit_palette_color", modifiedHabit.color);
        }
        else if (mode == EDIT_MODE)
        {
            Long habitId = (Long) args.get("habitId");
            if(habitId == null) throw new IllegalArgumentException("habitId must be specified");

            originalHabit = Habit.get(habitId);
            modifiedHabit = new Habit(originalHabit);

            getDialog().setTitle(R.string.edit_habit);
            tvName.append(modifiedHabit.name);
            tvDescription.append(modifiedHabit.description);
        }

        if(savedInstanceState != null)
        {
            modifiedHabit.color = savedInstanceState.getInt("color", modifiedHabit.color);
            modifiedHabit.reminderMin = savedInstanceState.getInt("reminderMin", -1);
            modifiedHabit.reminderHour = savedInstanceState.getInt("reminderHour", -1);
            modifiedHabit.reminderDays = savedInstanceState.getInt("reminderDays", -1);

            if(modifiedHabit.reminderMin < 0)
                modifiedHabit.clearReminder();
        }

        tvFreqNum.append(modifiedHabit.freqNum.toString());
        tvFreqDen.append(modifiedHabit.freqDen.toString());

        changeColor(modifiedHabit.color);
        updateFrequency();
        updateReminder();

        return view;
    }

    private void changeColor(int paletteColor)
    {
        modifiedHabit.color = paletteColor;
        tvName.setTextColor(ColorUtils.getColor(getActivity(), paletteColor));

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("pref_default_habit_palette_color", paletteColor);
        editor.apply();
    }

    @SuppressWarnings("ConstantConditions")
    private void updateReminder()
    {
        if (modifiedHabit.hasReminder())
        {
            tvReminderTime.setText(DateUtils.formatTime(getActivity(), modifiedHabit.reminderHour,
                    modifiedHabit.reminderMin));
            llReminderDays.setVisibility(View.VISIBLE);

            boolean weekdays[] = DateUtils.unpackWeekdayList(modifiedHabit.reminderDays);
            tvReminderDays.setText(DateUtils.formatWeekdayList(getActivity(), weekdays));
        }
        else
        {
            tvReminderTime.setText(R.string.reminder_off);
            llReminderDays.setVisibility(View.GONE);
        }
    }

    public void setOnSavedListener(InterfaceUtils.OnSavedListener onSavedListener)
    {
        this.onSavedListener = onSavedListener;
    }

	@Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.inputReminderTime:
                onDateSpinnerClick();
                break;

            case R.id.inputReminderDays:
                onWeekdayClick();
                break;

            case R.id.buttonSave:
                onSaveButtonClick();
                break;

            case R.id.buttonDiscard:
                dismiss();
                break;

            case R.id.buttonPickColor:
                onColorButtonClick();
                break;
        }
    }

    private void onColorButtonClick()
    {
        int originalAndroidColor = ColorUtils.getColor(getActivity(), modifiedHabit.color);

        ColorPickerDialog picker = ColorPickerDialog.newInstance(
                R.string.color_picker_default_title, ColorUtils.getPalette(getActivity()),
                originalAndroidColor, 4, ColorPickerDialog.SIZE_SMALL);

        picker.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener()
        {
            public void onColorSelected(int androidColor)
            {
                int paletteColor = ColorUtils.colorToPaletteIndex(getActivity(), androidColor);
                changeColor(paletteColor);
            }
        });
        picker.show(getFragmentManager(), "picker");
    }

    private void onSaveButtonClick()
    {
        modifiedHabit.name = tvName.getText().toString().trim();
        modifiedHabit.description = tvDescription.getText().toString().trim();
        String freqNum = tvFreqNum.getText().toString();
        String freqDen = tvFreqDen.getText().toString();
        if(!freqNum.isEmpty()) modifiedHabit.freqNum =  Integer.parseInt(freqNum);
        if(!freqDen.isEmpty()) modifiedHabit.freqDen = Integer.parseInt(freqDen);

        if (!validate()) return;

        Command command = null;
        Habit savedHabit = null;

        if (mode == EDIT_MODE)
        {
            command = new EditHabitCommand(originalHabit, modifiedHabit);
            savedHabit = originalHabit;
        }
        else if (mode == CREATE_MODE)
        {
            command = new CreateHabitCommand(modifiedHabit);
        }

        if (onSavedListener != null) onSavedListener.onSaved(command, savedHabit);

        dismiss();
    }

    private boolean validate()
    {
        Boolean valid = true;

        if (modifiedHabit.name.length() == 0)
        {
            tvName.setError(getString(R.string.validation_name_should_not_be_blank));
            valid = false;
        }

        if (modifiedHabit.freqNum <= 0)
        {
            tvFreqNum.setError(getString(R.string.validation_number_should_be_positive));
            valid = false;
        }

        if (modifiedHabit.freqNum > modifiedHabit.freqDen)
        {
            tvFreqNum.setError(getString(R.string.validation_at_most_one_rep_per_day));
            valid = false;
        }

        return valid;
    }

    @SuppressWarnings("ConstantConditions")
    private void onDateSpinnerClick()
    {
        int defaultHour = 8;
        int defaultMin = 0;

        if (modifiedHabit.hasReminder())
        {
            defaultHour = modifiedHabit.reminderHour;
            defaultMin = modifiedHabit.reminderMin;
        }

        TimePickerDialog timePicker =
                TimePickerDialog.newInstance(this, defaultHour, defaultMin, is24HourMode);
        timePicker.show(getFragmentManager(), "timePicker");
    }

    @SuppressWarnings("ConstantConditions")
    private void onWeekdayClick()
    {
        if(!modifiedHabit.hasReminder()) return;

        WeekdayPickerDialog dialog = new WeekdayPickerDialog();
        dialog.setListener(this);
        dialog.setSelectedDays(DateUtils.unpackWeekdayList(modifiedHabit.reminderDays));
        dialog.show(getFragmentManager(), "weekdayPicker");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hour, int minute)
    {
        modifiedHabit.reminderHour = hour;
        modifiedHabit.reminderMin = minute;
        modifiedHabit.reminderDays = DateUtils.ALL_WEEK_DAYS;
        updateReminder();
    }

    @Override
    public void onTimeCleared(RadialPickerLayout view)
    {
        modifiedHabit.clearReminder();
        updateReminder();
    }

    @Override
    public void onWeekdaysPicked(boolean[] selectedDays)
    {
        int count = 0;
        for(int i = 0; i < 7; i++)
            if(selectedDays[i]) count++;

        if(count == 0) Arrays.fill(selectedDays, true);

        modifiedHabit.reminderDays = DateUtils.packWeekdayList(selectedDays);
        updateReminder();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putInt("color", modifiedHabit.color);

        if(modifiedHabit.hasReminder())
        {
            outState.putInt("reminderMin", modifiedHabit.reminderMin);
            outState.putInt("reminderHour", modifiedHabit.reminderHour);
            outState.putInt("reminderDays", modifiedHabit.reminderDays);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if(parent.getId() == R.id.sFrequency)
        {
            switch (position)
            {
                case 0:
                    modifiedHabit.freqNum = 1;
                    modifiedHabit.freqDen = 1;
                    break;

                case 1:
                    modifiedHabit.freqNum = 1;
                    modifiedHabit.freqDen = 7;
                    break;

                case 2:
                    modifiedHabit.freqNum = 2;
                    modifiedHabit.freqDen = 7;
                    break;

                case 3:
                    modifiedHabit.freqNum = 5;
                    modifiedHabit.freqDen = 7;
                    break;

                case 4:
                    modifiedHabit.freqNum = 3;
                    modifiedHabit.freqDen = 7;
                    break;
            }
        }

        updateFrequency();
    }

    @SuppressLint("SetTextI18n")
    private void updateFrequency()
    {
        int quickSelectPosition = -1;

        if(modifiedHabit.freqNum.equals(modifiedHabit.freqDen))
            quickSelectPosition = 0;

        else if(modifiedHabit.freqNum == 1 && modifiedHabit.freqDen == 7)
            quickSelectPosition = 1;

        else if(modifiedHabit.freqNum == 2 && modifiedHabit.freqDen == 7)
            quickSelectPosition = 2;

        else if(modifiedHabit.freqNum == 5 && modifiedHabit.freqDen == 7)
            quickSelectPosition = 3;

        if(quickSelectPosition >= 0)
        {
            sFrequency.setVisibility(View.VISIBLE);
            sFrequency.setSelection(quickSelectPosition);
            llCustomFrequency.setVisibility(View.GONE);
            tvFreqNum.setText(modifiedHabit.freqNum.toString());
            tvFreqDen.setText(modifiedHabit.freqDen.toString());
        }
        else
        {
            sFrequency.setVisibility(View.GONE);
            llCustomFrequency.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}
