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

package org.isoron.uhabits.activities.habits.list.views;

import android.content.*;
import android.graphics.*;
import android.support.annotation.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.utils.*;

import static org.isoron.uhabits.utils.AttributeSetUtils.*;
import static org.isoron.uhabits.utils.ColorUtils.*;

public class NumberButtonView extends TextView
{
    private static Typeface TYPEFACE =
        Typeface.create("sans-serif-condensed", Typeface.BOLD);

    private int color;

    private int value;

    private int threshold;

    private StyledResources res;

    public NumberButtonView(@Nullable Context context)
    {
        super(context);
        init();
    }

    public NumberButtonView(@Nullable Context context,
                            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();

        if (context != null && attrs != null)
        {
            int color = getIntAttribute(context, attrs, "color", 0);
            int value = getIntAttribute(context, attrs, "value", 0);
            int threshold = getIntAttribute(context, attrs, "threshold", 1);
            setColor(getAndroidTestColor(color));
            setThreshold(threshold);
            setValue(value);
        }
    }

    private static String formatValue(int v)
    {
        double fv = (double) v;
        if(v >= 1e9) return String.format("%.2fG", fv / 1e9);
        if(v >= 1e8) return String.format("%.0fM", fv / 1e6);
        if(v >= 1e7) return String.format("%.1fM", fv / 1e6);
        if(v >= 1e6) return String.format("%.2fM", fv / 1e6);
        if(v >= 1e5) return String.format("%.0fk", fv / 1e3);
        if(v >= 1e4) return String.format("%.1fk", fv / 1e3);
        if(v >= 1e3) return String.format("%.2fk", fv / 1e3);
        return String.format("%d", v);
    }

    public void setColor(int color)
    {
        this.color = color;
        postInvalidate();
    }

    public void setController(final NumberButtonController controller)
    {
        setOnClickListener(v -> controller.onClick());
        setOnLongClickListener(v -> controller.onLongClick());
    }

    public void setThreshold(int threshold)
    {
        this.threshold = threshold;
        updateText();
    }

    public void setValue(int value)
    {
        this.value = value;
        updateText();
    }

    private void init()
    {
        res = new StyledResources(getContext());

        setWillNotDraw(false);

        setMinHeight(
            getResources().getDimensionPixelSize(R.dimen.checkmarkHeight));
        setMinWidth(
            getResources().getDimensionPixelSize(R.dimen.checkmarkWidth));

        setFocusable(false);
        setGravity(Gravity.CENTER);
        setTypeface(TYPEFACE);
    }

    private void updateText()
    {
        int lowColor = res.getColor(R.attr.lowContrastTextColor);
        setTextColor(value >= threshold ? color : lowColor);
        setText(formatValue(value));
    }
}
