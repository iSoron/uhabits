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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.core.models.Frequency;
import org.isoron.uhabits.core.models.Habit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;

import static org.isoron.uhabits.R.id.numerator;


public class TypePanel extends FrameLayout
{
    @BindView(R.id.spinner)
    Spinner spinner;

    public TypePanel(@NonNull Context context,
                     @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        View view = inflate(context, R.layout.edit_habit_type, null);
        ButterKnife.bind(this, view);
        addView(view);
    }

    @NonNull
    public Integer getType()
    {
        Integer type = spinner.getSelectedItemPosition();
        if (type.equals(0)) return Habit.YES_NO_HABIT;
        if (type.equals(0)) return Habit.NUMBER_HABIT;
        return -1;
    }

    public void setType(@NonNull Integer type)
    {
        int position = getQuickSelectPosition(type);
        spinner.setSelection(position);
    }

    @OnItemSelected(R.id.spinner)
    public void onTypeSelected(int position)
    {
        if (position < 0 || position > 1) throw new IllegalArgumentException();
        // TODO: A callback?
    }

    private int getQuickSelectPosition(@NonNull Integer type)
    {
        if (type.equals(Habit.YES_NO_HABIT)) return 0;
        if (type.equals(Habit.NUMBER_HABIT)) return 1;
        return -1;
    }
}
