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

import android.icu.text.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import butterknife.*;

public class NumericalHabitDialogHelper
{
    private DialogFragment frag;

    private DecimalFormat valueFormatter = new DecimalFormat("#.##");

    @BindView(R.id.tvName)
    TextView tvName;

    @BindView(R.id.tvDescription)
    ExampleEditText tvDescription;

    @BindView(R.id.tvUnit)
    ExampleEditText tvUnit;

    @BindView(R.id.tvTargetCount)
    TextView tvTargetValue;

    @BindView(R.id.tvTargetType)
    Spinner tvTargetType;

    public NumericalHabitDialogHelper(DialogFragment frag, View view)
    {
        this.frag = frag;
        ButterKnife.bind(this, view);
    }

    public void parseForm(Habit habit)
    {
        tvUnit.clearFocus();
        tvDescription.clearFocus();
        habit.setName(tvName.getText().toString().trim());
        habit.setDescription(tvDescription.getRealText().trim());
        habit.setTargetType(tvTargetType.getSelectedItemPosition());
        habit.setTargetValue(
            Double.parseDouble(tvTargetValue.getText().toString()));
        habit.setUnit(tvUnit.getRealText().trim());
    }

    public void populateColor(int paletteColor)
    {
        tvName.setTextColor(
            ColorUtils.getColor(frag.getContext(), paletteColor));
    }

    public void populateForm(final Habit habit)
    {
        tvName.setText(habit.getName());

        if(!habit.getDescription().isEmpty())
            tvDescription.setRealText(habit.getDescription());

        if(!habit.getUnit().isEmpty())
            tvUnit.setRealText(habit.getUnit());

        tvTargetType.setSelection(habit.getTargetType());
        tvTargetValue.setText(valueFormatter.format(habit.getTargetValue()));
        populateColor(habit.getColor());
    }

    public boolean validate(Habit habit)
    {
        Boolean valid = true;
        if (!validateName(habit)) valid = false;
        if (!validateTargetValue()) valid = false;
        return valid;
    }

    private boolean validateTargetValue()
    {
        double value = Double.parseDouble(tvTargetValue.getText().toString());
        if(value <= 0)
        {
            tvTargetValue.setError(frag.getString(R.string.validation_number_should_be_positive));
            return false;
        }
        return true;
    }

    private Boolean validateName(Habit habit)
    {
        if (habit.getName().isEmpty())
        {
            tvName.setError(frag.getString(R.string.validation_name_should_not_be_blank));
            return false;
        }

        return true;
    }
}
