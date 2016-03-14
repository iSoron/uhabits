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
import android.graphics.Rect;
import android.util.AttributeSet;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Streak;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HabitStreakView extends ScrollableDataView implements HabitDataView
{
    private Habit habit;
    private Paint pText, pBar;

    private long[] startTimes;
    private long[] endTimes;
    private long[] lengths;

    private int columnWidth;
    private int columnHeight;
    private int headerHeight;
    private int nColumns;

    private long maxStreakLength;
    private int[] colors;
    private SimpleDateFormat dfMonth;
    private Rect rect;
    private int baseSize;
    private int primaryColor;

    private boolean isBackgroundTransparent;
    private int textColor;
    private Paint pBarText;

    public HabitStreakView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.primaryColor = ColorHelper.palette[7];
        startTimes = endTimes = lengths = new long[0];
        init();
    }

    public void setHabit(Habit habit)
    {
        this.habit = habit;

        createColors();
        refreshData();
        postInvalidate();
    }

    private void init()
    {
        refreshData();
        createPaints();
        createColors();

        dfMonth = new SimpleDateFormat("MMM", Locale.getDefault());
        rect = new Rect();
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
        baseSize = height / 10;
        setScrollerBucketSize(baseSize);

        columnWidth = baseSize;
        columnHeight = 8 * baseSize;
        headerHeight = baseSize;
        nColumns = width / baseSize - 1;

        pText.setTextSize(baseSize * 0.5f);
        pBar.setTextSize(baseSize * 0.5f);
    }

    private void createColors()
    {
        if(habit != null)
            this.primaryColor = habit.color;

        if(isBackgroundTransparent)
        {
            primaryColor = ColorHelper.setSaturation(primaryColor, 0.75f);
            primaryColor = ColorHelper.setValue(primaryColor, 1.0f);
        }

        int red = Color.red(primaryColor);
        int green = Color.green(primaryColor);
        int blue = Color.blue(primaryColor);

        if(isBackgroundTransparent)
        {
            colors = new int[4];
            colors[3] = primaryColor;
            colors[2] = Color.argb(213, red, green, blue);
            colors[1] = Color.argb(170, red, green, blue);
            colors[0] = Color.argb(128, red, green, blue);
            textColor = Color.rgb(255, 255, 255);
            pBarText = pText;
        }
        else
        {
            colors = new int[4];
            colors[3] = primaryColor;
            colors[2] = Color.argb(192, red, green, blue);
            colors[1] = Color.argb(96, red, green, blue);
            colors[0] = Color.argb(32, 0, 0, 0);
            textColor = Color.argb(64, 0, 0, 0);
            pBarText = pBar;
        }
    }

    protected void createPaints()
    {
        pText = new Paint();
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setAntiAlias(true);

        pBar = new Paint();
        pBar.setTextAlign(Paint.Align.CENTER);
        pBar.setAntiAlias(true);
    }

    public void refreshData()
    {
        if(isInEditMode())
            generateRandomData();
        else
        {
            if(habit == null) return;

            List<Streak> streaks = habit.streaks.getAll();
            int size = streaks.size();

            startTimes = new long[size];
            endTimes = new long[size];
            lengths = new long[size];

            int k = 0;
            for (Streak s : streaks)
            {
                startTimes[k] = s.start;
                endTimes[k] = s.end;
                lengths[k] = s.length;
                k++;

                maxStreakLength = Math.max(maxStreakLength, s.length);
            }
        }

        invalidate();
    }

    private void generateRandomData()
    {
        int size = 30;

        startTimes = new long[size];
        endTimes = new long[size];
        lengths = new long[size];

        Random random = new Random();
        Long date = DateHelper.getStartOfToday();

        for(int i = 0; i < size; i++)
        {
            int l = (int) Math.pow(2, random.nextFloat() * 5 + 1);

            endTimes[i] = date;
            date -= l * DateHelper.millisecondsInOneDay;
            lengths[i] = (long) l;
            startTimes[i] = date;

            maxStreakLength = Math.max(maxStreakLength, l);
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        float lineHeight = pText.getFontSpacing();
        float barHeaderOffset = lineHeight * 0.4f;

        int nStreaks = startTimes.length;
        int start = nStreaks - nColumns - getDataOffset();

        pText.setColor(textColor);

        String previousMonth = "";

        for (int offset = 0; offset < nColumns && start + offset < nStreaks; offset++)
        {
            if(start + offset < 0) continue;
            String month = dfMonth.format(startTimes[start + offset]);

            long l = lengths[offset + start];
            double lRelative = ((double) l) / maxStreakLength;

            pBar.setColor(colors[(int) Math.floor(lRelative * 3)]);

            int height = (int) (columnHeight * lRelative);
            rect.set(0, 0, columnWidth - 2, height);
            rect.offset(offset * columnWidth, headerHeight + columnHeight - height);

            canvas.drawRect(rect, pBar);
            canvas.drawText(Long.toString(l), rect.centerX(), rect.top - barHeaderOffset, pBarText);

            if (!month.equals(previousMonth))
                canvas.drawText(month, rect.centerX(), rect.bottom + lineHeight * 1.2f, pText);

            previousMonth = month;
        }
    }

    public void setIsBackgroundTransparent(boolean isBackgroundTransparent)
    {
        this.isBackgroundTransparent = isBackgroundTransparent;
        createColors();
    }
}
