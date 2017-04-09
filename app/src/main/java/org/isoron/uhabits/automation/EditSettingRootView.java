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

package org.isoron.uhabits.automation;

import android.content.*;
import android.support.annotation.*;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.widget.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

import static android.R.layout.*;

public class EditSettingRootView extends BaseRootView
{
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.habitSpinner)
    AppCompatSpinner habitSpinner;

    @BindView(R.id.actionSpinner)
    AppCompatSpinner actionSpinner;

    @NonNull
    private final HabitList habitList;

    @NonNull
    private final EditSettingController controller;

    public EditSettingRootView(@NonNull Context context,
                               @NonNull HabitList habitList,
                               @NonNull EditSettingController controller)
    {
        super(context);
        this.habitList = habitList;
        this.controller = controller;

        addView(inflate(getContext(), R.layout.automation, null));
        ButterKnife.bind(this);
        populateHabitSpinner();
    }

    @NonNull
    @Override
    public Toolbar getToolbar()
    {
        return toolbar;
    }

    @Override
    public int getToolbarColor()
    {
        StyledResources res = new StyledResources(getContext());
        if (!res.getBoolean(R.attr.useHabitColorAsPrimary))
            return super.getToolbarColor();

        return res.getColor(R.attr.aboutScreenColor);
    }

    @OnClick(R.id.buttonSave)
    public void onClickSave()
    {
        int action = actionSpinner.getSelectedItemPosition();
        int habitPosition = habitSpinner.getSelectedItemPosition();
        Habit habit = habitList.getByPosition(habitPosition);
        controller.onSave(habit, action);
    }

    private void populateHabitSpinner()
    {
        List<String> names = new LinkedList<>();
        for (Habit h : habitList) names.add(h.getName());

        ArrayAdapter<String> adapter =
            new ArrayAdapter<>(getContext(), simple_spinner_item, names);
        adapter.setDropDownViewResource(simple_spinner_dropdown_item);
        habitSpinner.setAdapter(adapter);
    }
}
