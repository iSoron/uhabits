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

import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.edit.views.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import butterknife.*;

public class BooleanHabitDialogHelper
{
    private DialogFragment frag;

    @BindView(R.id.tvName)
    TextView tvName;

    @BindView(R.id.tvDescription)
    TextView tvDescription;

    @BindView(R.id.reminderPanel)
    ReminderPanel reminderPanel;

    @BindView(R.id.frequencyPanel)
    FrequencyPanel frequencyPanel;

    public BooleanHabitDialogHelper(DialogFragment frag, View view)
    {
        this.frag = frag;
        ButterKnife.bind(this, view);
    }

    protected void populateForm(final Habit habit)
    {
        tvName.setText(habit.getName());
        tvDescription.setText(habit.getDescription());
        populateColor(habit.getColor());
    }

    void parseFormIntoHabit(Habit habit)
    {
        habit.setName(tvName.getText().toString().trim());
        habit.setDescription(tvDescription.getText().toString().trim());
    }

    void populateColor(int paletteColor)
    {
        tvName.setTextColor(
            ColorUtils.getColor(frag.getContext(), paletteColor));
    }

    boolean validate(Habit habit)
    {
        Boolean valid = true;

        if (habit.getName().isEmpty())
        {
            tvName.setError(frag.getString(R.string.validation_name_should_not_be_blank));
            valid = false;
        }

        return valid;
    }
}
