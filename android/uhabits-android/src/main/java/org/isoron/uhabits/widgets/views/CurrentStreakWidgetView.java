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

package org.isoron.uhabits.widgets.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.isoron.androidbase.utils.ColorUtils;
import org.isoron.androidbase.utils.StyledResources;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.common.views.RingView;
import org.isoron.uhabits.core.models.Checkmark;

import static org.isoron.androidbase.utils.InterfaceUtils.getDimension;

public class CurrentStreakWidgetView extends HabitWidgetView
{
    private float percentage;

    @Nullable
    private String name;

    private RingView ring;

    private TextView label;

    private int checkmarkValue;
    private int startColor = 0x388e3c + (255 << 24); // add full alpha
    private int endColor = 0xc62828 + (255 << 24); // add full alpha
    private String currentStreak;
    private float timeoutPercentage;

    public CurrentStreakWidgetView(Context context)
    {
        super(context);
        init();
    }

    public CurrentStreakWidgetView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public void refresh()
    {
        if (backgroundPaint == null || frame == null || ring == null) return;

        StyledResources res = new StyledResources(getContext());

        int bgColor;
        int fgColor;

        bgColor = ColorUtils.interPolateHSV(startColor, endColor, timeoutPercentage);
        switch (checkmarkValue)
        {
            case Checkmark.CHECKED_EXPLICITLY:
                fgColor = res.getColor(R.attr.highContrastReverseTextColor);
                setShadowAlpha(0x4f);
                break;

            case Checkmark.CHECKED_IMPLICITLY:
            case Checkmark.UNCHECKED:
            default:
                fgColor = res.getColor(R.attr.mediumContrastTextColor);
                break;
        }

        backgroundPaint.setColor(bgColor);
        frame.setBackgroundDrawable(background);
        ring.setPercentage(percentage);
        ring.setColor(fgColor);
        ring.setBackgroundColor(bgColor);
        ring.setText(currentStreak);

        label.setText(name);
        label.setTextColor(fgColor);

        requestLayout();
        postInvalidate();
    }

    public void setCurrentStreak(String currentStreak)
    {
        this.currentStreak = currentStreak;
    }

    public void setCheckmarkValue(int checkmarkValue)
    {
        this.checkmarkValue = checkmarkValue;
    }

    public void setName(@NonNull String name)
    {
        this.name = name;
    }

    public void setPercentage(float percentage)
    {
        this.percentage = percentage;
    }
    public void setTimeOutPercentage(float percentage)
    {
        this.timeoutPercentage = percentage;
    }

    @Override
    @NonNull
    protected Integer getInnerLayoutId()
    {
        return R.layout.widget_checkmark;
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

        if (h < getDimension(getContext(), R.dimen.checkmarkWidget_heightBreakpoint))
            ring.setVisibility(GONE);
        else
            ring.setVisibility(VISIBLE);

        widthMeasureSpec =
            MeasureSpec.makeMeasureSpec((int) w, MeasureSpec.EXACTLY);
        heightMeasureSpec =
            MeasureSpec.makeMeasureSpec((int) h, MeasureSpec.EXACTLY);

        float textSize = 0.15f * h;
        float maxTextSize = getDimension(getContext(), R.dimen.smallerTextSize);
        textSize = Math.min(textSize, maxTextSize);

        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        ring.setTextSize(textSize);
        ring.setThickness(0.15f * textSize);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init()
    {
        ring = (RingView) findViewById(R.id.scoreRing);
        label = (TextView) findViewById(R.id.label);

        if (ring != null) ring.setIsTransparencyEnabled(true);

        if (isInEditMode())
        {
            percentage = 0.75f;
            name = "Wake up early";
            checkmarkValue = Checkmark.CHECKED_EXPLICITLY;
            refresh();
        }
    }
}
