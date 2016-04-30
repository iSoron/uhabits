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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.UIHelper;
import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;

public class CheckmarkWidgetView extends HabitWidgetView implements HabitDataView
{
    private int activeColor;
    private float percentage;

    @Nullable
    private String name;

    @Nullable
    private RingView ring;
    private TextView label;
    private int checkmarkValue;

    public CheckmarkWidgetView(Context context)
    {
        super(context);
        init();
    }

    public CheckmarkWidgetView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        ring = (RingView) findViewById(R.id.scoreRing);
        label = (TextView) findViewById(R.id.label);

        if(ring != null) ring.setIsTransparencyEnabled(true);

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
        super.setHabit(habit);
        this.name = habit.name;
        this.activeColor = ColorHelper.getColor(getContext(), habit.color);
        refresh();
    }

    public void refresh()
    {
        if (backgroundPaint == null || frame == null || ring == null) return;

        Context context = getContext();

        String text;
        int backgroundColor;
        int foregroundColor;

        switch (checkmarkValue)
        {
            case Checkmark.CHECKED_EXPLICITLY:
                text = getResources().getString(R.string.fa_check);
                backgroundColor = activeColor;
                foregroundColor =
                        UIHelper.getStyledColor(context, R.attr.highContrastReverseTextColor);

                setShadowAlpha(0x4f);
                rebuildBackground();

                backgroundPaint.setColor(backgroundColor);
                frame.setBackgroundDrawable(background);
                break;

            case Checkmark.CHECKED_IMPLICITLY:
                text = getResources().getString(R.string.fa_check);
                backgroundColor = UIHelper.getStyledColor(context, R.attr.cardBackgroundColor);
                foregroundColor = UIHelper.getStyledColor(context, R.attr.mediumContrastTextColor);
                break;

            case Checkmark.UNCHECKED:
            default:
                text = getResources().getString(R.string.fa_times);
                backgroundColor = UIHelper.getStyledColor(context, R.attr.cardBackgroundColor);
                foregroundColor = UIHelper.getStyledColor(context, R.attr.mediumContrastTextColor);
                break;
        }

        ring.setPercentage(percentage);
        ring.setColor(foregroundColor);
        ring.setBackgroundColor(backgroundColor);
        ring.setText(text);

        label.setText(name);
        label.setTextColor(foregroundColor);

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

    @NonNull
    protected Integer getInnerLayoutId()
    {
        return R.layout.widget_checkmark;
    }
}
