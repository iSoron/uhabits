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
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.UIHelper;
import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;

import java.util.Arrays;

public class CheckmarkView extends FrameLayout implements HabitDataView
{
    @Nullable
    private InsetDrawable background;

    @Nullable
    private Paint backgroundPaint;

    @Nullable
    private Habit habit;

    private int activeColor;
    private float percentage;

    @Nullable
    private String name;

    @Nullable
    private RingView ring;
    private ViewGroup frame;
    private TextView label;
    private int checkmarkValue;
    private int inactiveColor;

    public CheckmarkView(Context context)
    {
        super(context);
        init();
    }

    public CheckmarkView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.widget_checkmark_inner, this);

        int shadowRadius = (int) UIHelper.dpToPixels(getContext(), 2);
        int shadowOffset = (int) UIHelper.dpToPixels(getContext(), 1);
        int shadowColor = Color.argb(96, 0, 0, 0);

        float cornerRadius = UIHelper.dpToPixels(getContext(), 5);
        float[] radii = new float[8];
        Arrays.fill(radii, cornerRadius);

        RoundRectShape shape = new RoundRectShape(radii, null, null);
        ShapeDrawable innerDrawable = new ShapeDrawable(shape);

        int insetLeftTop = Math.max(shadowRadius - shadowOffset, 0);
        int insetRightBottom = shadowRadius + shadowOffset;

        background = new InsetDrawable(innerDrawable, insetLeftTop, insetLeftTop, insetRightBottom,
                insetRightBottom);
        backgroundPaint = innerDrawable.getPaint();
        backgroundPaint.setAlpha(100);

        if (backgroundPaint != null)
            backgroundPaint.setShadowLayer(shadowRadius, shadowOffset, shadowOffset, shadowColor);

        ring = (RingView) findViewById(R.id.scoreRing);
        frame = (ViewGroup) findViewById(R.id.frame);
        label = (TextView) findViewById(R.id.label);

        inactiveColor = ColorHelper.CSV_PALETTE[11];

        if(isInEditMode())
        {
            percentage = 0.75f;
            name = "Wake up early";
            activeColor = ColorHelper.CSV_PALETTE[6];
            checkmarkValue = Checkmark.CHECKED_EXPLICITLY;
            refresh();
        }
    }

    @Override
    public void setHabit(@NonNull Habit habit)
    {
        this.habit = habit;
        this.name = habit.name;
        this.activeColor = ColorHelper.getColor(getContext(), habit.color);
        refresh();
    }

    public void refresh()
    {
        if (backgroundPaint == null || frame == null || ring == null) return;

        String text;
        int color;
        switch (checkmarkValue)
        {
            case Checkmark.CHECKED_EXPLICITLY:
                text = getResources().getString(R.string.fa_check);
                color = activeColor;
                break;

            case Checkmark.CHECKED_IMPLICITLY:
                text = getResources().getString(R.string.fa_check);
                color = inactiveColor;
                break;

            case Checkmark.UNCHECKED:
            default:
                text = getResources().getString(R.string.fa_times);
                color = inactiveColor;
                break;
        }

        backgroundPaint.setColor(color);
        frame.setBackgroundDrawable(background);

        ring.setPercentage(percentage);
        ring.setPrecision(0.125f);
        ring.setColor(Color.WHITE);
        ring.setBackgroundColor(color);
        ring.setText(text);

        label.setText(name);

        requestLayout();
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        float w = width;
        float h = width * 1.25f;
        float scale = Math.min(width / w, height / h);

        w *= scale;
        h *= scale;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) w, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) h, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void refreshData()
    {
        if(habit == null) return;
        this.percentage = (float) habit.scores.getTodayValue() / Score.MAX_VALUE;
        this.checkmarkValue = habit.checkmarks.getTodayValue();

        refresh();
    }
}
