/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class HabitHistoryView extends ScrollableDataView
{

    private Habit habit;
    private int[] checkmarks;
    private Paint pSquareBg, pSquareFg, pTextHeader;
    private int squareSpacing;

    private float squareTextOffset;
    private float headerTextOffset;

    private String wdays[];
    private SimpleDateFormat dfMonth;
    private SimpleDateFormat dfYear;

    private Calendar baseDate;
    private int nDays;
    private int todayWeekday;
    private int colors[];

    public HabitHistoryView(Context context, Habit habit, int baseSize)
    {
        super(context);
        this.habit = habit;

        setDimensions(baseSize);
        createPaints();
        createColors();

        wdays = DateHelper.getShortDayNames();
        dfMonth = new SimpleDateFormat("MMM");
        dfYear = new SimpleDateFormat("yyyy");
    }

    private void updateDate()
    {
        baseDate = new GregorianCalendar();
        baseDate.add(Calendar.DAY_OF_YEAR, -(dataOffset - 1) * 7);

        nDays = (nColumns - 1) * 7;
        todayWeekday = new GregorianCalendar().get(Calendar.DAY_OF_WEEK) % 7;

        baseDate.add(Calendar.DAY_OF_YEAR, -nDays);
        baseDate.add(Calendar.DAY_OF_YEAR, -todayWeekday);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        updateDate();
    }

    private void createColors()
    {
        int primaryColor = habit.color;
        int primaryColorBright = ColorHelper.mixColors(primaryColor, Color.WHITE, 0.5f);
        int grey = Color.rgb(230, 230, 230);

        colors = new int[3];
        colors[0] = grey;
        colors[1] = primaryColorBright;
        colors[2] = primaryColor;
    }

    private void setDimensions(int baseSize)
    {
        columnWidth = baseSize;
        columnHeight = 8 * baseSize;
        squareSpacing = 2;
    }

    private void createPaints()
    {
        pTextHeader = new Paint();
        pTextHeader.setColor(Color.LTGRAY);
        pTextHeader.setTextAlign(Align.LEFT);
        pTextHeader.setTextSize(columnWidth * 0.5f);
        pTextHeader.setAntiAlias(true);

        pSquareBg = new Paint();
        pSquareBg.setColor(habit.color);

        pSquareFg = new Paint();
        pSquareFg.setColor(Color.WHITE);
        pSquareFg.setAntiAlias(true);
        pSquareFg.setTextSize(columnWidth * 0.5f);
        pSquareFg.setTextAlign(Align.CENTER);

        squareTextOffset = pSquareFg.getFontSpacing() * 0.4f;
        headerTextOffset = pTextHeader.getFontSpacing() * 0.3f;
    }

    protected void fetchData()
    {
        Calendar currentDate = new GregorianCalendar();
        currentDate.add(Calendar.DAY_OF_YEAR, -dataOffset * 7);
        int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) % 7;

        long dateTo = DateHelper.getStartOfToday();
        for (int i = 0; i < 7 - dayOfWeek; i++)
            dateTo += DateHelper.millisecondsInOneDay;

        for (int i = 0; i < dataOffset * 7; i++)
            dateTo -= DateHelper.millisecondsInOneDay;

        long dateFrom = dateTo;
        for (int i = 0; i < (nColumns - 1) * 7; i++)
            dateFrom -= DateHelper.millisecondsInOneDay;

        checkmarks = habit.getCheckmarks(dateFrom, dateTo);
        updateDate();
    }

    private String previousMonth;
    private String previousYear;
    private boolean justPrintedYear;

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        Rect location = new Rect(0, 0, columnWidth - squareSpacing, columnWidth - squareSpacing);

        previousMonth = "";
        previousYear = "";
        justPrintedYear = false;

        GregorianCalendar currentDate = (GregorianCalendar) baseDate.clone();

        for (int column = 0; column < nColumns - 1; column++)
        {
            drawColumn(canvas, location, currentDate, column);
            location.offset(columnWidth, -columnHeight);
        }

        drawAxis(canvas, location);
    }

    private void drawColumn(Canvas canvas, Rect location, GregorianCalendar date, int column)
    {
        drawColumnHeader(canvas, location, date);
        location.offset(0, columnWidth);

        for (int j = 0; j < 7; j++)
        {
            if (!(column == nColumns - 2 && dataOffset == 0 && j > todayWeekday))
            {
                int checkmarkOffset = nDays - 7 * column - j;
                drawSquare(canvas, location, date, checkmarkOffset);
            }

            date.add(Calendar.DAY_OF_MONTH, 1);
            location.offset(0, columnWidth);
        }
    }

    private void drawSquare(Canvas canvas, Rect location, GregorianCalendar date,
                            int checkmarkOffset)
    {
        if (checkmarkOffset >= checkmarks.length) pSquareBg.setColor(colors[0]);
        else pSquareBg.setColor(colors[checkmarks[checkmarkOffset]]);

        canvas.drawRect(location, pSquareBg);
        String text = Integer.toString(date.get(Calendar.DAY_OF_MONTH));
        canvas.drawText(text, location.centerX(), location.centerY() + squareTextOffset, pSquareFg);
    }

    private void drawAxis(Canvas canvas, Rect location)
    {
        for (int i = 0; i < 7; i++)
        {
            location.offset(0, columnWidth);
            canvas.drawText(wdays[i], location.left + headerTextOffset,
                    location.bottom - headerTextOffset, pTextHeader);
        }
    }

    private void drawColumnHeader(Canvas canvas, Rect location, GregorianCalendar date)
    {
        String month = dfMonth.format(date.getTime());
        String year = dfYear.format(date.getTime());

        if (!month.equals(previousMonth))
        {
            int offset = 0;
            if (justPrintedYear) offset += columnWidth;

            canvas.drawText(month, location.left + offset, location.bottom - headerTextOffset,
                    pTextHeader);
            previousMonth = month;
            justPrintedYear = false;
        }
        else if (!year.equals(previousYear))
        {
            canvas.drawText(year, location.left, location.bottom - headerTextOffset, pTextHeader);
            previousYear = year;
            justPrintedYear = true;
        }
        else
        {
            justPrintedYear = false;
        }
    }
}
