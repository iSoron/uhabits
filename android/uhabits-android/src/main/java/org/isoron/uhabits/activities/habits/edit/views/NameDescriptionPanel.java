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
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.utils.*;

import butterknife.*;


public class NameDescriptionPanel extends FrameLayout
{
    @BindView(R.id.tvName)
    EditText tvName;

    @BindView(R.id.tvDescription)
    ExampleEditText tvDescription;

    private int color;

    @NonNull
    private Controller controller;

    public NameDescriptionPanel(@NonNull Context context,
                                @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        View view = inflate(context, R.layout.edit_habit_name, null);
        ButterKnife.bind(this, view);
        addView(view);

        controller = new Controller() {};
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        this.color = color;
        tvName.setTextColor(PaletteUtils.getColor(getContext(), color));
    }

    @NonNull
    public String getDescription()
    {
        return tvDescription.getRealText().trim();
    }

    @NonNull
    public String getName()
    {
        return tvName.getText().toString().trim();
    }

    public void populateFrom(@NonNull Habit habit)
    {
        Resources res = getResources();

        if(habit.isNumerical())
            tvDescription.setExample(res.getString(R.string.example_question_numerical));
        else
            tvDescription.setExample(res.getString(R.string.example_question_boolean));

        setColor(habit.getColor());
        tvName.setText(habit.getName());
        tvDescription.setRealText(habit.getDescription());
    }

    public boolean validate()
    {
        Resources res = getResources();

        if (getName().isEmpty())
        {
            tvName.setError(
                res.getString(R.string.validation_name_should_not_be_blank));
            return false;
        }

        return true;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        BundleSavedState bss = (BundleSavedState) state;
        setColor(bss.bundle.getInt("color"));
        super.onRestoreInstanceState(bss.getSuperState());
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putInt("color", color);
        return new BundleSavedState(superState, bundle);
    }

    @OnClick(R.id.buttonPickColor)
    void showColorPicker()
    {
        controller.onColorPickerClicked(color);
    }

    public void setController(@NonNull Controller controller)
    {
        this.controller = controller;
    }

    public interface Controller
    {
        /**
         * Called when the user has clicked the widget to select a new
         * color for the habit.
         *
         * @param previousColor the color previously selected
         */
        default void onColorPickerClicked(int previousColor) {}
    }
}
