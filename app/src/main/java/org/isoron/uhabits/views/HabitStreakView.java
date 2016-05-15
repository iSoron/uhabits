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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.UIHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Streak;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HabitStreakView extends View implements HabitDataView
{
    private Habit habit;
    private Paint paint;

    private long minLength;
    private long maxLength;

    private int[] colors;
    private RectF rect;
    private int baseSize;
    private int primaryColor;
    private List<Streak> streaks;

    private boolean isBackgroundTransparent;
    private DateFormat dateFormat;
    private int width;
    private float em;
    private float maxLabelWidth;
    private float textMargin;
    private boolean shouldShowLabels;
    private int maxStreakCount;
    private int textColor;
    private int reverseTextColor;

    public HabitStreakView(Context context)
    {
        super(context);
        init();
    }

    public HabitStreakView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.primaryColor = ColorHelper.getColor(getContext(), 7);
        init();
    }

    public void setHabit(Habit habit)
    {
        this.habit = habit;
        createColors();
    }

    private void init()
    {
        createPaints();
        createColors();

        streaks = Collections.emptyList();

        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        rect = new RectF();
        maxStreakCount = 10;
        baseSize = getResources().getDimensionPixelSize(R.dimen.baseSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        maxStreakCount = height / baseSize;
        this.width = width;

        float minTextSize = getResources().getDimension(R.dimen.tinyTextSize);
        float maxTextSize = getResources().getDimension(R.dimen.regularTextSize);
        float textSize = baseSize * 0.5f;

        paint.setTextSize(Math.max(Math.min(textSize, maxTextSize), minTextSize));
        em = paint.getFontSpacing();
        textMargin = 0.5f * em;

        updateMaxMin();
    }

    private void createColors()
    {
        if(habit != null)
            this.primaryColor = ColorHelper.getColor(getContext(), habit.color);

        int red = Color.red(primaryColor);
        int green = Color.green(primaryColor);
        int blue = Color.blue(primaryColor);

        colors = new int[4];
        colors[3] = primaryColor;
        colors[2] = Color.argb(192, red, green, blue);
        colors[1] = Color.argb(96, red, green, blue);
        colors[0] = UIHelper.getStyledColor(getContext(), R.attr.lowContrastTextColor);
        textColor = UIHelper.getStyledColor(getContext(), R.attr.mediumContrastTextColor);
        reverseTextColor = UIHelper.getStyledColor(getContext(), R.attr.highContrastReverseTextColor);
    }

    protected void createPaints()
    {
        paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
    }

    public void refreshData()
    {
        if(habit == null) return;
        streaks = habit.streaks.getAll(maxStreakCount);
        updateMaxMin();
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if(streaks.size() == 0) return;

        rect.set(0, 0, width, baseSize);

        for(Streak s : streaks)
        {
            drawRow(canvas, s, rect);
            rect.offset(0, baseSize);
        }
    }

    private void updateMaxMin()
    {
        maxLength = 0;
        minLength = Long.MAX_VALUE;
        shouldShowLabels = true;

        for (Streak s : streaks)
        {
            maxLength = Math.max(maxLength, s.length);
            minLength = Math.min(minLength, s.length);

            float lw1 = paint.measureText(dateFormat.format(new Date(s.start)));
            float lw2 = paint.measureText(dateFormat.format(new Date(s.end)));
            maxLabelWidth = Math.max(maxLabelWidth, Math.max(lw1, lw2));
        }

        if(width - 2 * maxLabelWidth < width * 0.25f)
        {
            maxLabelWidth = 0;
            shouldShowLabels = false;
        }
    }

    private void drawRow(Canvas canvas, Streak streak, RectF rect)
    {
        if(maxLength == 0) return;

        float percentage = (float) streak.length / maxLength;
        float availableWidth = width - 2 * maxLabelWidth;
        if(shouldShowLabels) availableWidth -= 2 * textMargin;

        float barWidth = percentage * availableWidth;
        float minBarWidth = paint.measureText(streak.length.toString()) + em;
        barWidth = Math.max(barWidth, minBarWidth);

        float gap = (width - barWidth) / 2;
        float paddingTopBottom = baseSize * 0.05f;

        paint.setColor(percentageToColor(percentage));

        canvas.drawRect(rect.left + gap, rect.top + paddingTopBottom, rect.right - gap,
                rect.bottom - paddingTopBottom, paint);

        float yOffset = rect.centerY() + 0.3f * em;

        paint.setColor(reverseTextColor);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(streak.length.toString(), rect.centerX(), yOffset, paint);

        if(shouldShowLabels)
        {
            String startLabel = dateFormat.format(new Date(streak.start));
            String endLabel = dateFormat.format(new Date(streak.end));

            paint.setColor(textColor);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(startLabel, gap - textMargin, yOffset, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(endLabel, width - gap + textMargin, yOffset, paint);
        }
    }

    private int percentageToColor(float percentage)
    {
        if(percentage >= 1.0f) return colors[3];
        if(percentage >= 0.8f) return colors[2];
        if(percentage >= 0.5f) return colors[1];
        return colors[0];
    }

    public void setIsBackgroundTransparent(boolean isBackgroundTransparent)
    {
        this.isBackgroundTransparent = isBackgroundTransparent;
        createColors();
    }
}
