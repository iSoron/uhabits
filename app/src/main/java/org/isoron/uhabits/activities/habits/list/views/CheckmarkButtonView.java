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
import android.content.res.*;
import android.graphics.*;
import android.support.annotation.*;
import android.text.*;
import android.util.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.utils.*;

import static android.view.View.MeasureSpec.*;
import static org.isoron.uhabits.models.Checkmark.*;
import static org.isoron.uhabits.utils.AttributeSetUtils.*;

public class CheckmarkButtonView extends View
{
    private int color;

    private int value;

    private StyledResources styledRes;

    private TextPaint paint;

    private int lowContrastColor;

    private RectF rect;

    public CheckmarkButtonView(Context context)
    {
        super(context);
        init();
    }

    public CheckmarkButtonView(@Nullable Context ctx, @Nullable AttributeSet attrs)
    {
        super(ctx, attrs);
        init();

        if(ctx == null) throw new IllegalStateException();
        if(attrs == null) throw new IllegalStateException();

        int paletteColor = getIntAttribute(ctx, attrs, "color", 0);
        setColor(ColorUtils.getAndroidTestColor(paletteColor));

        int value = getIntAttribute(ctx, attrs, "value", 0);
        setValue(value);
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
        value = (value == CHECKED_EXPLICITLY ? UNCHECKED : CHECKED_EXPLICITLY);
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Resources resources = getResources();

        paint.setColor(value == CHECKED_EXPLICITLY ? color : lowContrastColor);
        int id = (value == UNCHECKED ? R.string.fa_times : R.string.fa_check);
        String label = resources.getString(id);
        float em = paint.measureText("m");

        rect.set(0, 0, getWidth(), getHeight());
        rect.offset(0, 0.4f * em);
        canvas.drawText(label, rect.centerX(), rect.centerY(), paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        Resources res = getResources();
        int height = res.getDimensionPixelSize(R.dimen.checkmarkHeight);
        int width = res.getDimensionPixelSize(R.dimen.checkmarkWidth);

        widthMeasureSpec = makeMeasureSpec(width, EXACTLY);
        heightMeasureSpec = makeMeasureSpec(height, EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init()
    {
        setFocusable(false);

        Resources res = getResources();
        styledRes = new StyledResources(getContext());

        paint = new TextPaint();
        paint.setTypeface(InterfaceUtils.getFontAwesome(getContext()));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(res.getDimension(R.dimen.regularTextSize));

        rect = new RectF();
        color = ColorUtils.getAndroidTestColor(0);
        lowContrastColor = styledRes.getColor(R.attr.lowContrastTextColor);
    }
}
