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

package org.isoron.uhabits.activities.habits.edit.views;

import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.R;

import java.text.DecimalFormat;

import butterknife.*;


public class TargetPanel extends FrameLayout
{
    private DecimalFormat valueFormatter = new DecimalFormat("#.##");

    @BindView(R.id.tvUnit)
    ExampleEditText tvUnit;

    @BindView(R.id.tvTargetCount)
    TextView tvTargetValue;

    public TargetPanel(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        View view = inflate(context, R.layout.edit_habit_target, null);
        ButterKnife.bind(this, view);
        addView(view);
    }

    public double getTargetValue()
    {
        String sValue = tvTargetValue.getText().toString();
        return Double.parseDouble(sValue);
    }

    public void setTargetValue(double targetValue)
    {
        tvTargetValue.setText(valueFormatter.format(targetValue));
    }

    public String getUnit()
    {
        return tvUnit.getRealText();
    }

    public void setUnit(String unit)
    {
        tvUnit.setRealText(unit);
    }

    public boolean validate()
    {
        Resources res = getResources();
        String sValue = tvTargetValue.getText().toString();
        double value = Double.parseDouble(sValue);

        if (value <= 0)
        {
            tvTargetValue.setError(
                res.getString(R.string.validation_number_should_be_positive));
            return false;
        }

        return true;
    }
}
