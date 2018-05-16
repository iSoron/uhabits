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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Spinner;

import org.isoron.uhabits.R;
import org.isoron.uhabits.core.models.Habit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;


public class TypePanel extends FrameLayout
{
    @BindView(R.id.spinner)
    Spinner spinner;

    private Integer type;

    @NonNull
    private Controller controller;

    public TypePanel(@NonNull Context context,
                     @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        View view = inflate(context, R.layout.edit_habit_type, null);
        ButterKnife.bind(this, view);
        addView(view);

        setType(Habit.YES_NO_HABIT);
        controller = new Controller() {};
    }

    @NonNull
    public Integer getType()
    {
        return type;
    }

    public void setType(@NonNull Integer type)
    {
        this.type = type;
        int position = getQuickSelectPosition(type);
        spinner.setSelection(position);
    }

    @OnItemSelected(R.id.spinner)
    public void onTypeSelected(int position)
    {
        if (position < 0 || position > 1) throw new IllegalArgumentException();
        Integer previousType = type;
        type = getTypeFromQuickSelectPosition(position);
        controller.onTypeSelected(previousType);
    }

    public void setEnabled(boolean enabled)
    {
        spinner.setEnabled(enabled);
    }

    public void setController(@NonNull Controller controller) { this.controller = controller; }

    public interface Controller
    {
        default void onTypeSelected(Integer previousType) {}
    }

    private Integer getTypeFromQuickSelectPosition(@NonNull Integer position)
    {
        if (position.equals(0)) return Habit.YES_NO_HABIT;
        if (position.equals(1)) return Habit.NUMBER_HABIT;
        return -1;
    }

    private int getQuickSelectPosition(@NonNull Integer type)
    {
        if (type.equals(Habit.YES_NO_HABIT)) return 0;
        if (type.equals(Habit.NUMBER_HABIT)) return 1;
        return -1;
    }
}
