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
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;

import butterknife.*;

public abstract class NumericalHabitDialog extends AppCompatDialogFragment
{
    @Nullable
    protected Habit originalHabit;

    @Nullable
    protected Habit modifiedHabit;

    protected Preferences prefs;

    protected CommandRunner commandRunner;

    protected HabitList habitList;

    protected AppComponent appComponent;

    protected ModelFactory modelFactory;

    private ColorPickerDialogFactory colorPickerDialogFactory;

    private NumericalHabitDialogHelper helper;

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
        ActivityComponent component = activity.getComponent();
        colorPickerDialogFactory = component.getColorPickerDialogFactory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view =
            inflater.inflate(R.layout.edit_numerical_habit, container, false);

        HabitsApplication app =
            (HabitsApplication) getContext().getApplicationContext();

        appComponent = app.getComponent();
        prefs = appComponent.getPreferences();
        habitList = appComponent.getHabitList();
        commandRunner = appComponent.getCommandRunner();
        modelFactory = appComponent.getModelFactory();

        ButterKnife.bind(this, view);

        helper = new NumericalHabitDialogHelper(this, view);
        getDialog().setTitle(getTitle());
        initializeHabits();
        restoreSavedInstance(savedInstanceState);
        helper.populateForm(modifiedHabit);
        return view;
    }

    @Override
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
        if (modifiedHabit == null) return;

        int color = bundle.getInt("color", modifiedHabit.getColor());

        modifiedHabit.setColor(color);
        modifiedHabit.setReminder(null);
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
        helper.parseForm(modifiedHabit);
        if (!helper.validate(modifiedHabit)) return;
        saveHabit();
        dismiss();
    }

    @OnClick(R.id.buttonPickColor)
    void showColorPicker()
    {
        if (modifiedHabit == null) return;

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
