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

import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HabitFrequencyView extends ScrollableDataView implements HabitDataView
{

    private Paint pGrid;
    private float em;
    private Habit habit;
    private SimpleDateFormat dfMonth;
    private SimpleDateFormat dfYear;

    private Paint pText, pGraph;
    private RectF rect, prevRect;
    private int baseSize;
    private int paddingTop;

    private int columnWidth;
    private int columnHeight;
    private int nColumns;

    private int textColor;
    private int dimmedTextColor;
    private int[] colors;
    private int primaryColor;
    private boolean isBackgroundTransparent;

    private HashMap<Long, Integer[]> frequency;

    public HabitFrequencyView(Context context)
    {
        super(context);
        init();
    }

    public HabitFrequencyView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.primaryColor = ColorHelper.palette[7];
        this.frequency = new HashMap<>();
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

        dfMonth = DateHelper.getDateFormat("MMM");
        dfYear = DateHelper.getDateFormat("yyyy");

        rect = new RectF();
        prevRect = new RectF();
    }

    private void createColors()
    {
        if(habit != null)
            this.primaryColor = habit.color;

        if (isBackgroundTransparent)
        {
            primaryColor = ColorHelper.setSaturation(primaryColor, 0.75f);
            primaryColor = ColorHelper.setValue(primaryColor, 1.0f);

            textColor = Color.argb(192, 255, 255, 255);
            dimmedTextColor = Color.argb(128, 255, 255, 255);
        }
        else
        {
            textColor = Color.argb(64, 0, 0, 0);
            dimmedTextColor = Color.argb(16, 0, 0, 0);
        }

        colors = new int[4];

        colors[0] = Color.rgb(230, 230, 230);
        colors[3] = primaryColor;
        colors[1] = ColorHelper.mixColors(colors[0], colors[3], 0.66f);
        colors[2] = ColorHelper.mixColors(colors[0], colors[3], 0.33f);
    }

    protected void createPaints()
    {
        pText = new Paint();
        pText.setAntiAlias(true);

        pGraph = new Paint();
        pGraph.setTextAlign(Paint.Align.CENTER);
        pGraph.setAntiAlias(true);

        pGrid = new Paint();
        pGrid.setAntiAlias(true);
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
        if(height < 9) height = 200;

        baseSize = height / 8;
        setScrollerBucketSize(baseSize);

        columnWidth = baseSize;
        columnHeight = 8 * baseSize;
        nColumns = width / baseSize;
        paddingTop = 0;

        pText.setTextSize(baseSize * 0.4f);
        pGraph.setTextSize(baseSize * 0.4f);
        pGraph.setStrokeWidth(baseSize * 0.1f);
        pGrid.setStrokeWidth(baseSize * 0.05f);
        em = pText.getFontSpacing();
    }

    public void refreshData()
    {
        if(isInEditMode())
            generateRandomData();
        else if(habit != null)
            frequency = habit.repetitions.getWeekdayFrequency();

        postInvalidate();
    }

    private void generateRandomData()
    {
        GregorianCalendar date = DateHelper.getStartOfTodayCalendar();
        date.set(Calendar.DAY_OF_MONTH, 1);
        Random rand = new Random();
        frequency.clear();

        for(int i = 0; i < 40; i++)
        {
            Integer values[] = new Integer[7];
            for(int j = 0; j < 7; j++)
                values[j] = rand.nextInt(5);

            frequency.put(date.getTimeInMillis(), values);
            date.add(Calendar.MONTH, -1);
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        rect.set(0, 0, nColumns * columnWidth, columnHeight);
        rect.offset(0, paddingTop);

        drawGrid(canvas, rect);

        pText.setTextAlign(Paint.Align.CENTER);
        pText.setColor(textColor);
        pGraph.setColor(primaryColor);
        prevRect.setEmpty();

        GregorianCalendar currentDate = DateHelper.getStartOfTodayCalendar();

        currentDate.set(Calendar.DAY_OF_MONTH, 1);
        currentDate.add(Calendar.MONTH, -nColumns + 2 - getDataOffset());

        for(int i = 0; i < nColumns - 1; i++)
        {
            rect.set(0, 0, columnWidth, columnHeight);
            rect.offset(i * columnWidth, 0);

            drawColumn(canvas, rect, currentDate);
            currentDate.add(Calendar.MONTH, 1);
        }
    }

    private void drawColumn(Canvas canvas, RectF rect, GregorianCalendar date)
    {
        Integer values[] = frequency.get(date.getTimeInMillis());
        float rowHeight = rect.height() / 8.0f;
        prevRect.set(rect);

        Integer[] localeWeekdayList = DateHelper.getLocaleWeekdayList();
        for (int j = 0; j < localeWeekdayList.length; j++)
        {
            rect.set(0, 0, baseSize, baseSize);
            rect.offset(prevRect.left, prevRect.top + columnWidth * j);

            int i = DateHelper.weekDayNumber2wdays(localeWeekdayList[j]);
            if(values != null)
                drawMarker(canvas, rect, values[i]);

            rect.offset(0, rowHeight);
        }

        drawFooter(canvas, rect, date);
    }

    private void drawFooter(Canvas canvas, RectF rect, GregorianCalendar date)
    {
        Date time = date.getTime();

        canvas.drawText(dfMonth.format(time), rect.centerX(), rect.centerY() - 0.1f * em, pText);

        if(date.get(Calendar.MONTH) == 1)
            canvas.drawText(dfYear.format(time), rect.centerX(), rect.centerY() + 0.9f * em, pText);
    }

    private void drawMarker(Canvas canvas, RectF rect, Integer value)
    {
        float padding = rect.height() * 0.2f;
        float radius = (rect.height() - 2 * padding) / 2.0f / 4.0f * Math.min(value, 4);

        pGraph.setColor(colors[Math.min(3, Math.max(0, value - 1))]);
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius, pGraph);
    }

    private void drawGrid(Canvas canvas, RectF rGrid)
    {
        int nRows = 7;
        float rowHeight = rGrid.height() / (nRows + 1);

        pText.setTextAlign(Paint.Align.LEFT);
        pText.setColor(textColor);
        pGrid.setColor(dimmedTextColor);

        for (String day : DateHelper.getLocaleDayNames(Calendar.SHORT)) {
            canvas.drawText(day, rGrid.right - columnWidth,
                    rGrid.top + rowHeight / 2 + 0.25f * em, pText);

            pGrid.setStrokeWidth(1f);
            canvas.drawLine(rGrid.left, rGrid.top, rGrid.right, rGrid.top, pGrid);

            rGrid.offset(0, rowHeight);
        }

        canvas.drawLine(rGrid.left, rGrid.top, rGrid.right, rGrid.top, pGrid);
    }

    public void setIsBackgroundTransparent(boolean isBackgroundTransparent)
    {
        this.isBackgroundTransparent = isBackgroundTransparent;
        createColors();
    }
}
