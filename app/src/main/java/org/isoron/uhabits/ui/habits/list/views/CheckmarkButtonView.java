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

package org.isoron.uhabits.ui.habits.list.views;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.habits.list.controllers.*;
import org.isoron.uhabits.utils.*;

import butterknife.*;

public class CheckmarkButtonView extends FrameLayout
{
    private int color;

    private int value;

    @BindView(R.id.tvCheck)
    TextView tvCheck;

    public CheckmarkButtonView(Context context)
    {
        super(context);
        init();
    }

    public void setColor(int color)
    {
        this.color = color;
        postInvalidate();
    }

    public void setController(final CheckmarkButtonController controller)
    {
        setOnClickListener(v -> controller.onClick());
        setOnLongClickListener(v -> controller.onLongClick());
    }

    public void setValue(int value)
    {
        this.value = value;
        postInvalidate();
    }

    public void toggle()
    {
        value = (value == Checkmark.CHECKED_EXPLICITLY ? Checkmark.UNCHECKED :
                     Checkmark.CHECKED_EXPLICITLY);

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int lowContrastColor = InterfaceUtils.getStyledColor(getContext(),
            R.attr.lowContrastTextColor);

        if (value == Checkmark.CHECKED_EXPLICITLY)
        {
            tvCheck.setText(R.string.fa_check);
            tvCheck.setTextColor(color);
        }

        if (value == Checkmark.CHECKED_IMPLICITLY)
        {
            tvCheck.setText(R.string.fa_check);
            tvCheck.setTextColor(lowContrastColor);
        }

        if (value == Checkmark.UNCHECKED)
        {
            tvCheck.setText(R.string.fa_times);
            tvCheck.setTextColor(lowContrastColor);
        }

        super.onDraw(canvas);
    }

    private void init()
    {
        addView(
            inflate(getContext(), R.layout.list_habits_card_checkmark, null));
        ButterKnife.bind(this);

        setWillNotDraw(false);
        setHapticFeedbackEnabled(false);

        tvCheck.setTypeface(InterfaceUtils.getFontAwesome(getContext()));
    }
}
