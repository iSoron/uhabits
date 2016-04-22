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

package org.isoron.uhabits.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.UIHelper;
import org.isoron.uhabits.models.Habit;

import java.util.Arrays;

public abstract  class HabitWidgetView extends FrameLayout implements HabitDataView
{
    @Nullable
    protected InsetDrawable background;

    @Nullable
    protected Paint backgroundPaint;

    @Nullable
    protected Habit habit;
    protected ViewGroup frame;

    public HabitWidgetView(Context context)
    {
        super(context);
        init();
    }

    public HabitWidgetView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        inflate(getContext(), getInnerLayoutId(), this);
        initBackground();
    }

    protected abstract @NonNull Integer getInnerLayoutId();

    private void initBackground()
    {
        Context context = getContext();
        context.setTheme(R.style.AppBaseThemeDark);

        int shadowRadius = (int) UIHelper.dpToPixels(context, 2);
        int shadowOffset = (int) UIHelper.dpToPixels(context, 1);
        int shadowColor = Color.argb(96, 0, 0, 0);

        float cornerRadius = UIHelper.dpToPixels(context, 5);
        float[] radii = new float[8];
        Arrays.fill(radii, cornerRadius);

        RoundRectShape shape = new RoundRectShape(radii, null, null);
        ShapeDrawable innerDrawable = new ShapeDrawable(shape);

        int insetLeftTop = Math.max(shadowRadius - shadowOffset, 0);
        int insetRightBottom = shadowRadius + shadowOffset;

        background = new InsetDrawable(innerDrawable, insetLeftTop, insetLeftTop, insetRightBottom,
                insetRightBottom);
        backgroundPaint = innerDrawable.getPaint();
        backgroundPaint.setShadowLayer(shadowRadius, shadowOffset, shadowOffset, shadowColor);
        backgroundPaint.setColor(UIHelper.getStyledColor(context, R.attr.cardBackgroundColor));

        frame = (ViewGroup) findViewById(R.id.frame);
        frame.setBackgroundDrawable(background);
    }

    @Override
    public void setHabit(@NonNull Habit habit)
    {
        this.habit = habit;
    }
}
