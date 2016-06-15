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

package org.isoron.uhabits.ui.habits.show.views;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.text.*;
import java.util.*;

public class HabitStreakView extends View
    implements HabitDataView, ModelObservable.Listener
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
        this.primaryColor = ColorUtils.getColor(getContext(), 7);
        init();
    }

    @Override
    public void onModelChange()
    {
        refreshData();
    }

    @Override
    public void refreshData()
    {
        if (habit == null) return;
        streaks = habit.getStreaks().getBest(maxStreakCount);
        createColors();
        updateMaxMin();
        postInvalidate();
    }

    @Override
    public void setHabit(Habit habit)
    {
        this.habit = habit;
        createColors();
    }

    public void setIsBackgroundTransparent(boolean isBackgroundTransparent)
    {
        this.isBackgroundTransparent = isBackgroundTransparent;
        createColors();
    }

    protected void createPaints()
    {
        paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                refreshData();
            }
        }.execute();
        habit.getObservable().addListener(this);
        habit.getStreaks().getObservable().addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        habit.getStreaks().getObservable().removeListener(this);
        habit.getObservable().removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (streaks.size() == 0) return;

        rect.set(0, 0, width, baseSize);

        for (Streak s : streaks)
        {
            drawRow(canvas, s, rect);
            rect.offset(0, baseSize);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int width,
                                 int height,
                                 int oldWidth,
                                 int oldHeight)
    {
        maxStreakCount = height / baseSize;
        this.width = width;

        float minTextSize = getResources().getDimension(R.dimen.tinyTextSize);
        float maxTextSize =
            getResources().getDimension(R.dimen.regularTextSize);
        float textSize = baseSize * 0.5f;

        paint.setTextSize(
            Math.max(Math.min(textSize, maxTextSize), minTextSize));
        em = paint.getFontSpacing();
        textMargin = 0.5f * em;

        updateMaxMin();
    }

    private void createColors()
    {
        if (habit != null) this.primaryColor =
            ColorUtils.getColor(getContext(), habit.getColor());

        int red = Color.red(primaryColor);
        int green = Color.green(primaryColor);
        int blue = Color.blue(primaryColor);

        colors = new int[4];
        colors[3] = primaryColor;
        colors[2] = Color.argb(192, red, green, blue);
        colors[1] = Color.argb(96, red, green, blue);
        colors[0] = InterfaceUtils.getStyledColor(getContext(),
            R.attr.lowContrastTextColor);
        textColor = InterfaceUtils.getStyledColor(getContext(),
            R.attr.mediumContrastTextColor);
        reverseTextColor = InterfaceUtils.getStyledColor(getContext(),
            R.attr.highContrastReverseTextColor);
    }

    private void drawRow(Canvas canvas, Streak streak, RectF rect)
    {
        if (maxLength == 0) return;

        float percentage = (float) streak.getLength() / maxLength;
        float availableWidth = width - 2 * maxLabelWidth;
        if (shouldShowLabels) availableWidth -= 2 * textMargin;

        float barWidth = percentage * availableWidth;
        float minBarWidth =
            paint.measureText(Long.toString(streak.getLength())) + em;
        barWidth = Math.max(barWidth, minBarWidth);

        float gap = (width - barWidth) / 2;
        float paddingTopBottom = baseSize * 0.05f;

        paint.setColor(percentageToColor(percentage));

        canvas.drawRect(rect.left + gap, rect.top + paddingTopBottom,
            rect.right - gap, rect.bottom - paddingTopBottom, paint);

        float yOffset = rect.centerY() + 0.3f * em;

        paint.setColor(reverseTextColor);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(Long.toString(streak.getLength()), rect.centerX(),
            yOffset, paint);

        if (shouldShowLabels)
        {
            String startLabel = dateFormat.format(new Date(streak.getStart()));
            String endLabel = dateFormat.format(new Date(streak.getEnd()));

            paint.setColor(textColor);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(startLabel, gap - textMargin, yOffset, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(endLabel, width - gap + textMargin, yOffset, paint);
        }
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

    private int percentageToColor(float percentage)
    {
        if (percentage >= 1.0f) return colors[3];
        if (percentage >= 0.8f) return colors[2];
        if (percentage >= 0.5f) return colors[1];
        return colors[0];
    }

    private void updateMaxMin()
    {
        maxLength = 0;
        minLength = Long.MAX_VALUE;
        shouldShowLabels = true;

        for (Streak s : streaks)
        {
            maxLength = Math.max(maxLength, s.getLength());
            minLength = Math.min(minLength, s.getLength());

            float lw1 =
                paint.measureText(dateFormat.format(new Date(s.getStart())));
            float lw2 =
                paint.measureText(dateFormat.format(new Date(s.getEnd())));
            maxLabelWidth = Math.max(maxLabelWidth, Math.max(lw1, lw2));
        }

        if (width - 2 * maxLabelWidth < width * 0.25f)
        {
            maxLabelWidth = 0;
            shouldShowLabels = false;
        }
    }
}
