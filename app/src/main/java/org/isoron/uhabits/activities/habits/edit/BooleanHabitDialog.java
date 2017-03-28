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

package org.isoron.uhabits.activities.habits.edit;

import android.os.*;
import android.support.annotation.*;
import android.support.v7.app.*;
import android.text.format.*;
import android.view.*;

import com.android.datetimepicker.time.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.activities.habits.edit.views.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;

import butterknife.*;

public abstract class BooleanHabitDialog extends AppCompatDialogFragment
{
    protected Habit originalHabit;

    protected Habit modifiedHabit;

    protected BooleanHabitDialogHelper helper;

    protected Preferences prefs;

    protected CommandRunner commandRunner;

    protected HabitList habitList;

    protected AppComponent appComponent;

    protected ModelFactory modelFactory;

    private ColorPickerDialogFactory colorPickerDialogFactory;

    @Override
    public int getTheme()
    {
        return R.style.DialogWithTitle;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        colorPickerDialogFactory =
            activity.getComponent().getColorPickerDialogFactory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view =
            inflater.inflate(R.layout.edit_boolean_habit, container, false);

        HabitsApplication app =
            (HabitsApplication) getContext().getApplicationContext();

        appComponent = app.getComponent();
        prefs = appComponent.getPreferences();
        habitList = appComponent.getHabitList();
        commandRunner = appComponent.getCommandRunner();
        modelFactory = appComponent.getModelFactory();

        ButterKnife.bind(this, view);

        helper = new BooleanHabitDialogHelper(this, view);
        getDialog().setTitle(getTitle());
        initializeHabits();
        restoreSavedInstance(savedInstanceState);
        helper.populateForm(modifiedHabit);

        helper.frequencyPanel.setFrequency(modifiedHabit.getFrequency());

        if (modifiedHabit.hasReminder())
            helper.reminderPanel.setReminder(modifiedHabit.getReminder());

        helper.reminderPanel.setController(new ReminderPanel.Controller()
        {
            @Override
            public void onTimeClicked(int currentHour, int currentMin)
            {
                TimePickerDialog timePicker;
                boolean is24HourMode = DateFormat.is24HourFormat(getContext());
                timePicker = TimePickerDialog.newInstance(helper.reminderPanel,
                    currentHour, currentMin, is24HourMode);
                timePicker.show(getFragmentManager(), "timePicker");
            }

            @Override
            public void onWeekdayClicked(WeekdayList currentDays)
            {
                WeekdayPickerDialog dialog = new WeekdayPickerDialog();
                dialog.setListener(helper.reminderPanel);
                dialog.setSelectedDays(currentDays);
                dialog.show(getFragmentManager(), "weekdayPicker");
            }
        });

        return view;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("color", modifiedHabit.getColor());
    }

    protected abstract int getTitle();

    protected abstract void initializeHabits();

    protected void restoreSavedInstance(@Nullable Bundle bundle)
    {
        if (bundle == null) return;
        modifiedHabit.setColor(
            bundle.getInt("color", modifiedHabit.getColor()));
    }

    protected abstract void saveHabit();

    @OnClick(R.id.buttonDiscard)
    void onButtonDiscardClick()
    {
        dismiss();
    }

    @OnClick(R.id.buttonSave)
    void onSaveButtonClick()
    {
        helper.parseFormIntoHabit(modifiedHabit);
        modifiedHabit.setReminder(helper.reminderPanel.getReminder());
        modifiedHabit.setFrequency(helper.frequencyPanel.getFrequency());

        if (!helper.frequencyPanel.validate()) return;
        if (!helper.validate(modifiedHabit)) return;

        saveHabit();
        dismiss();
    }

    @OnClick(R.id.buttonPickColor)
    void showColorPicker()
    {
        int color = modifiedHabit.getColor();
        ColorPickerDialog picker = colorPickerDialogFactory.create(color);

        picker.setListener(c ->
        {
            prefs.setDefaultHabitColor(c);
            modifiedHabit.setColor(c);
            helper.populateColor(c);
        });

        picker.show(getFragmentManager(), "picker");
    }
}
