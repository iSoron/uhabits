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
import android.text.*;
import android.util.*;
import android.view.*;

import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.utils.*;

import java.text.*;

import static org.isoron.uhabits.utils.AttributeSetUtils.*;
import static org.isoron.androidbase.utils.InterfaceUtils.*;

public class NumberButtonView extends View
{
    private static Typeface BOLD_TYPEFACE =
        Typeface.create("sans-serif-condensed", Typeface.BOLD);

    private static Typeface NORMAL_TYPEFACE =
        Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    private int color;

    private double value;

    private double threshold;

    private String unit;

    private RectF rect;

    private TextPaint pRegular;

    private TextPaint pBold;

    private int lightGrey;

    private float em;

    private int darkGrey;

    public NumberButtonView(@Nullable Context context)
    {
        super(context);
        init();
    }

    public NumberButtonView(@Nullable Context ctx, @Nullable AttributeSet attrs)
    {
        super(ctx, attrs);
        init();

        if (ctx != null && attrs != null)
        {
            int color = getIntAttribute(ctx, attrs, "color", 0);
            int value = getIntAttribute(ctx, attrs, "value", 0);
            int threshold = getIntAttribute(ctx, attrs, "threshold", 1);
            String unit = getAttribute(ctx, attrs, "unit", "min");
            setColor(PaletteUtils.getAndroidTestColor(color));
            setThreshold(threshold);
            setValue(value);
            setUnit(unit);
        }
    }

    /**
     * @param v
     * @return
     */
    public static String formatValue(double v)
    {
        if (v >= 1e9) return String.format("%.1fG", v / 1e9);
        if (v >= 1e8) return String.format("%.0fM", v / 1e6);
        if (v >= 1e7) return String.format("%.1fM", v / 1e6);
        if (v >= 1e6) return String.format("%.1fM", v / 1e6);
        if (v >= 1e5) return String.format("%.0fk", v / 1e3);
        if (v >= 1e4) return String.format("%.1fk", v / 1e3);
        if (v >= 1e3) return String.format("%.1fk", v / 1e3);
        if (v >= 1e2) return new DecimalFormat("#").format(v);
        if (v >= 1e1) return new DecimalFormat("#.#").format(v);
        return new DecimalFormat("#.##").format(v);
    }

    public void setColor(int color)
    {
        this.color = color;
        postInvalidate();
    }

    public void setController(final NumberButtonController controller)
    {
        setOnClickListener(v -> controller.onClick());
        setOnLongClickListener(v ->
        {
            controller.onLongClick();
            return true;
        });
    }

    public void setThreshold(double threshold)
    {
        this.threshold = threshold;
        postInvalidate();
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
        postInvalidate();
    }

    public void setValue(double value)
    {
        this.value = value;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int activeColor = lightGrey;
        if (value > 0 && value < threshold) activeColor = darkGrey;
        if (value >= threshold) activeColor = color;

        pRegular.setColor(activeColor);
        pBold.setColor(activeColor);

        String fv = formatValue(value);

        rect.set(0, 0, getWidth(), getHeight());
        canvas.drawText(fv, rect.centerX(), rect.centerY(), pBold);

        rect.offset(0, 1.2f * em);
        canvas.drawText(unit, rect.centerX(), rect.centerY(), pRegular);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = (int) getDimension(getContext(), R.dimen.checkmarkWidth);
        int height = (int) getDimension(getContext(), R.dimen.checkmarkHeight);
        setMeasuredDimension(width, height);
    }

    private void init()
    {
        StyledResources sr = new StyledResources(getContext());

        rect = new RectF();
        pRegular = new TextPaint();
        pRegular.setTextSize(
            getDimension(getContext(), R.dimen.smallerTextSize));
        pRegular.setTypeface(NORMAL_TYPEFACE);
        pRegular.setAntiAlias(true);
        pRegular.setTextAlign(Paint.Align.CENTER);

        pBold = new TextPaint();
        pBold.setTextSize(getDimension(getContext(), R.dimen.smallTextSize));
        pBold.setTypeface(BOLD_TYPEFACE);
        pBold.setAntiAlias(true);
        pBold.setTextAlign(Paint.Align.CENTER);

        em = pBold.measureText("m");
        lightGrey = sr.getColor(R.attr.lowContrastTextColor);
        darkGrey = sr.getColor(R.attr.mediumContrastTextColor);
    }
}
