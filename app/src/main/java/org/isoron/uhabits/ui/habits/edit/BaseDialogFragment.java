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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.CommandRunner;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.utils.ColorUtils;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.Preferences;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public abstract class BaseDialogFragment extends AppCompatDialogFragment
{
    protected Habit originalHabit;

    protected Habit modifiedHabit;

    protected BaseDialogHelper helper;

    @Inject
    Preferences prefs;

    @Inject
    CommandRunner commandRunner;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.edit_habit, container, false);
        HabitsApplication.getComponent().inject(this);
        ButterKnife.bind(this, view);

        helper = new BaseDialogHelper(this, view);
        getDialog().setTitle(getTitle());
        initializeHabits();
        restoreSavedInstance(savedInstanceState);
        helper.populateForm(modifiedHabit);
        return view;
    }

    @OnItemSelected(R.id.sFrequency)
    public void onFrequencySelected(int position)
    {
        if (position < 0 || position > 4) throw new IllegalArgumentException();
        int freqNums[] = {1, 1, 2, 5, 3};
        int freqDens[] = {1, 7, 7, 7, 7};
        modifiedHabit.setFreqNum(freqNums[position]);
        modifiedHabit.setFreqDen(freqDens[position]);
        helper.populateFrequencyFields(modifiedHabit);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("color", modifiedHabit.getColor());
        if (modifiedHabit.hasReminder())
        {
            outState.putInt("reminderMin", modifiedHabit.getReminderMin());
            outState.putInt("reminderHour", modifiedHabit.getReminderHour());
            outState.putInt("reminderDays", modifiedHabit.getReminderDays());
        }
    }

    protected abstract int getTitle();

    protected abstract void initializeHabits();

    @OnClick(R.id.buttonDiscard)
    void onButtonDiscardClick()
    {
        dismiss();
    }

    @OnClick(R.id.tvReminderTime)
    @SuppressWarnings("ConstantConditions")
    void onDateSpinnerClick()
    {
        int defaultHour = 8;
        int defaultMin = 0;

        if (modifiedHabit.hasReminder())
        {
            defaultHour = modifiedHabit.getReminderHour();
            defaultMin = modifiedHabit.getReminderMin();
        }

        showTimePicker(defaultHour, defaultMin);
    }

    @OnClick(R.id.buttonSave)
    void onSaveButtonClick()
    {
        helper.parseFormIntoHabit(modifiedHabit);
        if (!helper.validate(modifiedHabit)) return;
        saveHabit();
        dismiss();
    }

    @OnClick(R.id.tvReminderDays)
    @SuppressWarnings("ConstantConditions")
    void onWeekdayClick()
    {
        if (!modifiedHabit.hasReminder()) return;
        WeekdayPickerDialog dialog = new WeekdayPickerDialog();
        dialog.setListener(new OnWeekdaysPickedListener());
        dialog.setSelectedDays(
            DateUtils.unpackWeekdayList(modifiedHabit.getReminderDays()));
        dialog.show(getFragmentManager(), "weekdayPicker");
    }

    protected void restoreSavedInstance(@Nullable Bundle bundle)
    {
        if (bundle == null) return;
        modifiedHabit.setColor(
            bundle.getInt("color", modifiedHabit.getColor()));
        modifiedHabit.setReminderMin(bundle.getInt("reminderMin", -1));
        modifiedHabit.setReminderHour(bundle.getInt("reminderHour", -1));
        modifiedHabit.setReminderDays(bundle.getInt("reminderDays", -1));
        if (modifiedHabit.getReminderMin() < 0) modifiedHabit.clearReminder();
    }

    protected abstract void saveHabit();

    @OnClick(R.id.buttonPickColor)
    void showColorPicker()
    {
        int androidColor =
            ColorUtils.getColor(getContext(), modifiedHabit.getColor());

        ColorPickerDialog picker =
            ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                ColorUtils.getPalette(getContext()), androidColor, 4,
                ColorPickerDialog.SIZE_SMALL);

        picker.setOnColorSelectedListener(new OnColorSelectedListener());
        picker.show(getFragmentManager(), "picker");
    }

    private void showTimePicker(int defaultHour, int defaultMin)
    {
        boolean is24HourMode = DateFormat.is24HourFormat(getContext());
        TimePickerDialog timePicker =
            TimePickerDialog.newInstance(new OnTimeSetListener(), defaultHour,
                defaultMin, is24HourMode);
        timePicker.show(getFragmentManager(), "timePicker");
    }

    private class OnColorSelectedListener
        implements ColorPickerSwatch.OnColorSelectedListener
    {
        @Override
        public void onColorSelected(int androidColor)
        {
            int paletteColor =
                ColorUtils.colorToPaletteIndex(getActivity(), androidColor);
            prefs.setDefaultHabitColor(paletteColor);
            modifiedHabit.setColor(paletteColor);
            helper.populateColor(paletteColor);
        }
    }

    private class OnTimeSetListener
        implements TimePickerDialog.OnTimeSetListener
    {
        @Override
        public void onTimeCleared(RadialPickerLayout view)
        {
            modifiedHabit.clearReminder();
            helper.populateReminderFields(modifiedHabit);
        }

        @Override
        public void onTimeSet(RadialPickerLayout view, int hour, int minute)
        {
            modifiedHabit.setReminderHour(hour);
            modifiedHabit.setReminderMin(minute);
            modifiedHabit.setReminderDays(DateUtils.ALL_WEEK_DAYS);
            helper.populateReminderFields(modifiedHabit);
        }
    }

    private class OnWeekdaysPickedListener
        implements WeekdayPickerDialog.OnWeekdaysPickedListener
    {
        @Override
        public void onWeekdaysPicked(boolean[] selectedDays)
        {
            if (isSelectionEmpty(selectedDays)) Arrays.fill(selectedDays, true);

            modifiedHabit.setReminderDays(
                DateUtils.packWeekdayList(selectedDays));
            helper.populateReminderFields(modifiedHabit);
        }

        private boolean isSelectionEmpty(boolean[] selectedDays)
        {
            for (boolean d : selectedDays) if (d) return false;
            return true;
        }
    }
}
