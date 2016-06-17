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

import android.os.*;
import android.support.annotation.*;
import android.support.v7.app.*;
import android.text.format.*;
import android.view.*;

import com.android.colorpicker.*;
import com.android.datetimepicker.time.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;
import org.isoron.uhabits.utils.DateUtils;

import java.util.*;

import javax.inject.*;

import butterknife.*;

public abstract class BaseDialogFragment extends AppCompatDialogFragment
{
    @Nullable
    protected Habit originalHabit;

    @Nullable
    protected Habit modifiedHabit;

    @Nullable
    protected BaseDialogHelper helper;

    @Inject
    protected Preferences prefs;

    @Inject
    protected CommandRunner commandRunner;

    @Inject
    protected HabitList habitList;

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
        int freqNums[] = { 1, 1, 2, 5, 3 };
        int freqDens[] = { 1, 7, 7, 7, 7 };
        modifiedHabit.setFrequency(new Frequency(freqNums[position], freqDens[position]));
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
            Reminder reminder = modifiedHabit.getReminder();
            outState.putInt("reminderMin", reminder.getMinute());
            outState.putInt("reminderHour", reminder.getHour());
            outState.putInt("reminderDays", reminder.getDays());
        }
    }

    protected abstract int getTitle();

    protected abstract void initializeHabits();

    protected void restoreSavedInstance(@Nullable Bundle bundle)
    {
        if (bundle == null) return;
        modifiedHabit.setColor(
            bundle.getInt("color", modifiedHabit.getColor()));


        modifiedHabit.setReminder(null);

        int hour = (bundle.getInt("reminderHour", -1));
        int minute = (bundle.getInt("reminderMin", -1));
        int days = (bundle.getInt("reminderDays", -1));

        if (hour >= 0 && minute >= 0)
        {
            Reminder reminder = new Reminder(hour, minute, days);
            modifiedHabit.setReminder(reminder);
        }
    }

    protected abstract void saveHabit();

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
            Reminder reminder = modifiedHabit.getReminder();
            defaultHour = reminder.getHour();
            defaultMin = reminder.getMinute();
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
        Reminder reminder = modifiedHabit.getReminder();

        WeekdayPickerDialog dialog = new WeekdayPickerDialog();
        dialog.setListener(new OnWeekdaysPickedListener());
        dialog.setSelectedDays(
            DateUtils.unpackWeekdayList(reminder.getDays()));
        dialog.show(getFragmentManager(), "weekdayPicker");
    }

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
            Reminder reminder =
                new Reminder(hour, minute, DateUtils.ALL_WEEK_DAYS);
            modifiedHabit.setReminder(reminder);
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

            Reminder oldReminder = modifiedHabit.getReminder();
            modifiedHabit.setReminder(
                new Reminder(oldReminder.getHour(), oldReminder.getMinute(),
                    DateUtils.packWeekdayList(selectedDays)));
            helper.populateReminderFields(modifiedHabit);
        }

        private boolean isSelectionEmpty(boolean[] selectedDays)
        {
            for (boolean d : selectedDays) if (d) return false;
            return true;
        }
    }
}
