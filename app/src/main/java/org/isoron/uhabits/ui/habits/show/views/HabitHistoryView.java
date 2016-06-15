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
import android.graphics.Paint.*;
import android.util.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.text.*;
import java.util.*;

public class HabitHistoryView extends ScrollableDataView implements
                                                         HabitDataView,
                                                         ToggleRepetitionTask.Listener,
                                                         ModelObservable.Listener
{
    private Habit habit;

    private int[] checkmarks;

    private Paint pSquareBg, pSquareFg, pTextHeader;

    private float squareSpacing;

    private float squareTextOffset;

    private float headerTextOffset;

    private float columnWidth;

    private float columnHeight;

    private int nColumns;

    private SimpleDateFormat dfMonth;

    private SimpleDateFormat dfYear;

    private Calendar baseDate;

    private int nDays;

    /**
     * 0-based-position of today in the column
     */
    private int todayPositionInColumn;

    private int colors[];

    private RectF baseLocation;

    private int primaryColor;

    private boolean isBackgroundTransparent;

    private int textColor;

    private int reverseTextColor;

    private boolean isEditable;

    private String previousMonth;

    private String previousYear;

    private float headerOverflow = 0;

    public HabitHistoryView(Context context)
    {
        super(context);
        init();
    }

    public HabitHistoryView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
        onSingleTapUp(e);
    }

    @Override
    public void onModelChange()
    {
        refreshData();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        if (!isEditable) return false;

        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

        int pointerId = e.getPointerId(0);
        float x = e.getX(pointerId);
        float y = e.getY(pointerId);

        final Long timestamp = positionToTimestamp(x, y);
        if (timestamp == null) return false;

        ToggleRepetitionTask task = new ToggleRepetitionTask(habit, timestamp);
        task.setListener(this);
        task.execute();

        return true;
    }

    @Override
    public void onToggleRepetitionFinished()
    {
        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                refreshData();
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                invalidate();
                super.onPostExecute(null);
            }
        }.execute();
    }

    @Override
    public void refreshData()
    {
        if (isInEditMode()) generateRandomData();
        else
        {
            if (habit == null) return;
            checkmarks = habit.getCheckmarks().getAllValues();
            createColors();
        }

        updateDate();
        postInvalidate();
    }

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

    public void setIsEditable(boolean isEditable)
    {
        this.isEditable = isEditable;
    }

    protected void createPaints()
    {
        pTextHeader = new Paint();
        pTextHeader.setTextAlign(Align.LEFT);
        pTextHeader.setAntiAlias(true);

        pSquareBg = new Paint();

        pSquareFg = new Paint();
        pSquareFg.setAntiAlias(true);
        pSquareFg.setTextAlign(Align.CENTER);
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
        habit.getCheckmarks().observable.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        habit.getCheckmarks().observable.removeListener(this);
        habit.getObservable().removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        baseLocation.set(0, 0, columnWidth - squareSpacing,
            columnWidth - squareSpacing);
        baseLocation.offset(getPaddingLeft(), getPaddingTop());

        headerOverflow = 0;
        previousMonth = "";
        previousYear = "";
        pTextHeader.setColor(textColor);

        updateDate();
        GregorianCalendar currentDate = (GregorianCalendar) baseDate.clone();

        for (int column = 0; column < nColumns - 1; column++)
        {
            drawColumn(canvas, baseLocation, currentDate, column);
            baseLocation.offset(columnWidth, -columnHeight);
        }

        drawAxis(canvas, baseLocation);
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
        if (height < 8) height = 200;
        float baseSize = height / 8.0f;
        setScrollerBucketSize((int) baseSize);

        squareSpacing = InterfaceUtils.dpToPixels(getContext(), 1.0f);
        float maxTextSize =
            getResources().getDimension(R.dimen.regularTextSize);
        float textSize = height * 0.06f;
        textSize = Math.min(textSize, maxTextSize);

        pSquareFg.setTextSize(textSize);
        pTextHeader.setTextSize(textSize);
        squareTextOffset = pSquareFg.getFontSpacing() * 0.4f;
        headerTextOffset = pTextHeader.getFontSpacing() * 0.3f;

        float rightLabelWidth = getWeekdayLabelWidth() + headerTextOffset;
        float horizontalPadding = getPaddingRight() + getPaddingLeft();

        columnWidth = baseSize;
        columnHeight = 8 * baseSize;
        nColumns =
            (int) ((width - rightLabelWidth - horizontalPadding) / baseSize) +
            1;

        updateDate();
    }

    private void createColors()
    {
        if (habit != null) this.primaryColor =
            ColorUtils.getColor(getContext(), habit.getColor());

        if (isBackgroundTransparent)
            primaryColor = ColorUtils.setMinValue(primaryColor, 0.75f);

        int red = Color.red(primaryColor);
        int green = Color.green(primaryColor);
        int blue = Color.blue(primaryColor);

        if (isBackgroundTransparent)
        {
            colors = new int[3];
            colors[0] = Color.argb(16, 255, 255, 255);
            colors[1] = Color.argb(128, red, green, blue);
            colors[2] = primaryColor;
            textColor = Color.WHITE;
            reverseTextColor = Color.WHITE;
        }
        else
        {
            colors = new int[3];
            colors[0] = InterfaceUtils.getStyledColor(getContext(),
                R.attr.lowContrastTextColor);
            colors[1] = Color.argb(127, red, green, blue);
            colors[2] = primaryColor;
            textColor = InterfaceUtils.getStyledColor(getContext(),
                R.attr.mediumContrastTextColor);
            reverseTextColor = InterfaceUtils.getStyledColor(getContext(),
                R.attr.highContrastReverseTextColor);
        }
    }

    private void drawAxis(Canvas canvas, RectF location)
    {
        float verticalOffset = pTextHeader.getFontSpacing() * 0.4f;

        for (String day : DateUtils.getLocaleDayNames(Calendar.SHORT))
        {
            location.offset(0, columnWidth);
            canvas.drawText(day, location.left + headerTextOffset,
                location.centerY() + verticalOffset, pTextHeader);
        }
    }

    private void drawColumn(Canvas canvas,
                            RectF location,
                            GregorianCalendar date,
                            int column)
    {
        drawColumnHeader(canvas, location, date);
        location.offset(0, columnWidth);

        for (int j = 0; j < 7; j++)
        {
            if (!(column == nColumns - 2 && getDataOffset() == 0 &&
                  j > todayPositionInColumn))
            {
                int checkmarkOffset =
                    getDataOffset() * 7 + nDays - 7 * (column + 1) +
                    todayPositionInColumn - j;
                drawSquare(canvas, location, date, checkmarkOffset);
            }

            date.add(Calendar.DAY_OF_MONTH, 1);
            location.offset(0, columnWidth);
        }
    }

    private void drawColumnHeader(Canvas canvas,
                                  RectF location,
                                  GregorianCalendar date)
    {
        String month = dfMonth.format(date.getTime());
        String year = dfYear.format(date.getTime());

        String text = null;
        if (!month.equals(previousMonth)) text = previousMonth = month;
        else if (!year.equals(previousYear)) text = previousYear = year;

        if (text != null)
        {
            canvas.drawText(text, location.left + headerOverflow,
                location.bottom - headerTextOffset, pTextHeader);
            headerOverflow +=
                pTextHeader.measureText(text) + columnWidth * 0.2f;
        }

        headerOverflow = Math.max(0, headerOverflow - columnWidth);
    }

    private void drawSquare(Canvas canvas,
                            RectF location,
                            GregorianCalendar date,
                            int checkmarkOffset)
    {
        if (checkmarkOffset >= checkmarks.length) pSquareBg.setColor(colors[0]);
        else pSquareBg.setColor(colors[checkmarks[checkmarkOffset]]);

        pSquareFg.setColor(reverseTextColor);
        canvas.drawRect(location, pSquareBg);
        String text = Integer.toString(date.get(Calendar.DAY_OF_MONTH));
        canvas.drawText(text, location.centerX(),
            location.centerY() + squareTextOffset, pSquareFg);
    }

    private void generateRandomData()
    {
        Random random = new Random();
        checkmarks = new int[100];

        for (int i = 0; i < 100; i++)
            if (random.nextFloat() < 0.3) checkmarks[i] = 2;

        for (int i = 0; i < 100 - 7; i++)
        {
            int count = 0;
            for (int j = 0; j < 7; j++)
                if (checkmarks[i + j] != 0) count++;

            if (count >= 3) checkmarks[i] = Math.max(checkmarks[i], 1);
        }
    }

    private float getWeekdayLabelWidth()
    {
        float width = 0;

        for (String w : DateUtils.getLocaleDayNames(Calendar.SHORT))
            width = Math.max(width, pSquareFg.measureText(w));

        return width;
    }

    private void init()
    {
        createColors();
        createPaints();

        isEditable = false;
        checkmarks = new int[0];
        primaryColor = ColorUtils.getColor(getContext(), 7);
        dfMonth = DateUtils.getDateFormat("MMM");
        dfYear = DateUtils.getDateFormat("yyyy");

        baseLocation = new RectF();
    }

    private Long positionToTimestamp(float x, float y)
    {
        int col = (int) (x / columnWidth);
        int row = (int) (y / columnWidth);

        if (row == 0) return null;
        if (col == nColumns - 1) return null;

        int offset = col * 7 + (row - 1);
        Calendar date = (Calendar) baseDate.clone();
        date.add(Calendar.DAY_OF_YEAR, offset);

        if (DateUtils.getStartOfDay(date.getTimeInMillis()) >
            DateUtils.getStartOfToday()) return null;

        return date.getTimeInMillis();
    }

    private void updateDate()
    {
        baseDate = DateUtils.getStartOfTodayCalendar();
        baseDate.add(Calendar.DAY_OF_YEAR, -(getDataOffset() - 1) * 7);

        nDays = (nColumns - 1) * 7;
        int realWeekday =
            DateUtils.getStartOfTodayCalendar().get(Calendar.DAY_OF_WEEK);
        todayPositionInColumn =
            (7 + realWeekday - baseDate.getFirstDayOfWeek()) % 7;

        baseDate.add(Calendar.DAY_OF_YEAR, -nDays);
        baseDate.add(Calendar.DAY_OF_YEAR, -todayPositionInColumn);
    }
}
