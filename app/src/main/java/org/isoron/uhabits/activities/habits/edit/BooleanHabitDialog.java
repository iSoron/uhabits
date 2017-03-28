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

import android.content.*;
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

public class BooleanHabitDialog extends AppCompatDialogFragment
{
    public static final String BUNDLE_HABIT_ID = "habitId";

    protected Habit originalHabit;

    protected Preferences prefs;

    protected CommandRunner commandRunner;

    protected HabitList habitList;

    protected AppComponent component;

    protected ModelFactory modelFactory;

    @BindView(R.id.namePanel)
    NameDescriptionPanel namePanel;

    @BindView(R.id.reminderPanel)
    ReminderPanel reminderPanel;

    @BindView(R.id.frequencyPanel)
    FrequencyPanel frequencyPanel;

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
        View view;
        view = inflater.inflate(R.layout.edit_boolean_habit, container, false);

        initDependencies();
        ButterKnife.bind(this, view);

        getDialog().setTitle(getTitle());
        originalHabit = parseHabitFromArguments();

        populateForm();
        setupReminderController();
        setupNameController();

        return view;
    }

    protected int getTitle()
    {
        if (originalHabit == null) return R.string.edit_habit;
        else return R.string.create_habit;
    }

    protected void saveHabit(@NonNull Habit habit)
    {
        if (originalHabit == null)
        {
            commandRunner.execute(component
                .getCreateHabitCommandFactory()
                .create(habitList, habit), null);
        }
        else
        {
            commandRunner.execute(component.getEditHabitCommandFactory().
                create(habitList, originalHabit, habit), originalHabit.getId());
        }
    }

    private void initDependencies()
    {
        Context appContext = getContext().getApplicationContext();
        HabitsApplication app = (HabitsApplication) appContext;

        component = app.getComponent();
        prefs = component.getPreferences();
        habitList = component.getHabitList();
        commandRunner = component.getCommandRunner();
        modelFactory = component.getModelFactory();
    }

    @OnClick(R.id.buttonDiscard)
    void onButtonDiscardClick()
    {
        dismiss();
    }

    @OnClick(R.id.buttonSave)
    void onSaveButtonClick()
    {
        if (!namePanel.validate()) return;
        if (!frequencyPanel.validate()) return;

        Habit habit = modelFactory.buildHabit();
        habit.setName(namePanel.getName());
        habit.setDescription(namePanel.getDescription());
        habit.setColor(namePanel.getColor());
        habit.setReminder(reminderPanel.getReminder());
        habit.setFrequency(frequencyPanel.getFrequency());

        saveHabit(habit);
        dismiss();
    }

    @Nullable
    private Habit parseHabitFromArguments()
    {
        Bundle arguments = getArguments();
        if (arguments == null) return null;

        Long id = (Long) arguments.get(BUNDLE_HABIT_ID);
        if (id == null) return null;

        Habit habit = habitList.getById(id);
        if (habit == null) throw new IllegalStateException();

        return habit;
    }

    private void populateForm()
    {
        Habit habit = modelFactory.buildHabit();
        habit.setFrequency(Frequency.DAILY);
        habit.setColor(prefs.getDefaultHabitColor(habit.getColor()));

        if (originalHabit != null) habit.copyFrom(originalHabit);

        namePanel.populateFrom(habit);
        frequencyPanel.setFrequency(habit.getFrequency());
        if (habit.hasReminder()) reminderPanel.setReminder(habit.getReminder());
    }

    private void setupNameController()
    {
        namePanel.setController(new NameDescriptionPanel.Controller()
        {
            @Override
            public void onColorPickerClicked(int previousColor)
            {
                ColorPickerDialog picker =
                    colorPickerDialogFactory.create(previousColor);

                picker.setListener(c ->
                {
                    prefs.setDefaultHabitColor(c);
                    namePanel.setColor(c);
                });

                picker.show(getFragmentManager(), "picker");
            }
        });
    }

    private void setupReminderController()
    {
        reminderPanel.setController(new ReminderPanel.Controller()
        {
            @Override
            public void onTimeClicked(int currentHour, int currentMin)
            {
                TimePickerDialog timePicker;
                boolean is24HourMode = DateFormat.is24HourFormat(getContext());
                timePicker =
                    TimePickerDialog.newInstance(reminderPanel, currentHour,
                        currentMin, is24HourMode);
                timePicker.show(getFragmentManager(), "timePicker");
            }

            @Override
            public void onWeekdayClicked(WeekdayList currentDays)
            {
                WeekdayPickerDialog dialog = new WeekdayPickerDialog();
                dialog.setListener(reminderPanel);
                dialog.setSelectedDays(currentDays);
                dialog.show(getFragmentManager(), "weekdayPicker");
            }
        });
    }
}
