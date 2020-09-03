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

package org.isoron.uhabits.activities.common.views;

import android.content.*;
import android.graphics.*;
import android.graphics.Paint.*;
import android.util.*;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.utils.*;

import java.text.*;
import java.util.*;

import static org.isoron.androidbase.utils.InterfaceUtils.*;
import static org.isoron.uhabits.core.models.Checkmark.*;

public class HistoryChart extends ScrollableChart
{
    private int[] checkmarks;

    private int target;

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

    private int textColors[];

    private RectF baseLocation;

    private int primaryColor;

    private boolean isBackgroundTransparent;

    private int reverseTextColor;

    private int backgroundColor;

    private boolean isEditable;

    private String previousMonth;

    private String previousYear;

    private float headerOverflow = 0;

    private boolean isNumerical = false;

    private int firstWeekday = Calendar.SUNDAY;

    @NonNull
    private Controller controller;

    public HistoryChart(Context context)
    {
        super(context);
        init();
    }

    public HistoryChart(Context context, AttributeSet attrs)
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
    public boolean onSingleTapUp(MotionEvent e)
    {
        float x, y;
        try
        {
            int pointerId = e.getPointerId(0);
            x = e.getX(pointerId);
            y = e.getY(pointerId);
        }
        catch (RuntimeException ex)
        {
            // Android often throws IllegalArgumentException here. Apparently,
            // the pointer id may become invalid shortly after calling
            // e.getPointerId.
            return false;
        }
        return tap(x, y);
    }

    public boolean tap(float x, float y)
    {
        if (!isEditable) return false;
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

        final Timestamp timestamp = positionToTimestamp(x, y);
        if (timestamp == null) return false;

        Timestamp today = DateUtils.getToday();
        int newValue = CHECKED_EXPLICITLY;
        int offset = timestamp.daysUntil(today);
        if (offset < checkmarks.length)
        {
            newValue = Repetition.nextToggleValue(checkmarks[offset]);
            checkmarks[offset] = newValue;
        }

        controller.onToggleCheckmark(timestamp, newValue);
        postInvalidate();
        return true;

    }

    public void populateWithRandomData()
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

    public void setCheckmarks(int[] checkmarks)
    {
        this.checkmarks = checkmarks;
        postInvalidate();
    }

    public void setColor(int color)
    {
        this.primaryColor = color;
        initColors();
        postInvalidate();
    }

    public void setController(@NonNull Controller controller)
    {
        this.controller = controller;
    }

    public void setNumerical(boolean numerical)
    {
        isNumerical = numerical;
    }

    public void setIsBackgroundTransparent(boolean isBackgroundTransparent)
    {
        this.isBackgroundTransparent = isBackgroundTransparent;
        initColors();
    }

    public void setIsEditable(boolean isEditable)
    {
        this.isEditable = isEditable;
    }

    public void setTarget(int target)
    {
        this.target = target;
        postInvalidate();
    }

    public void setFirstWeekday(int firstWeekday)
    {
        this.firstWeekday = firstWeekday;
        postInvalidate();
    }

    protected void initPaints()
    {
        pTextHeader = new Paint();
        pTextHeader.setTextAlign(Align.LEFT);
        pTextHeader.setAntiAlias(true);

        pSquareBg = new Paint();
        pSquareBg.setAntiAlias(true);

        pSquareFg = new Paint();
        pSquareFg.setAntiAlias(true);
        pSquareFg.setTextAlign(Align.CENTER);
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
        pTextHeader.setColor(textColors[1]);

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

        squareSpacing = dpToPixels(getContext(), 1.0f);
        float maxTextSize = getDimension(getContext(), R.dimen.regularTextSize);
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

    private void drawAxis(Canvas canvas, RectF location)
    {
        float verticalOffset = pTextHeader.getFontSpacing() * 0.4f;

        for (String day : DateUtils.getShortWeekdayNames(firstWeekday))
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

        int checkmark = 0;
        if (checkmarkOffset >= checkmarks.length)
        {
            pSquareBg.setColor(colors[0]);
            pSquareFg.setColor(textColors[1]);
        }
        else
        {
            checkmark = checkmarks[checkmarkOffset];
            if(checkmark == 0)
            {
                pSquareBg.setColor(colors[0]);
                pSquareFg.setColor(textColors[1]);
            }
            else if ((isNumerical && checkmark < target) || checkmark != CHECKED_EXPLICITLY)
            {
                pSquareBg.setColor(colors[1]);
                pSquareFg.setColor(textColors[2]);
            }
            else
            {
                pSquareBg.setColor(colors[2]);
                pSquareFg.setColor(textColors[2]);
            }
        }

        float round = dpToPixels(getContext(), 2);
        canvas.drawRoundRect(location, round, round, pSquareBg);

        if (!isNumerical && checkmark == SKIPPED)
        {
            pSquareBg.setColor(backgroundColor);
            pSquareBg.setStrokeWidth(columnWidth * 0.025f);

            canvas.save();
            canvas.clipRect(location);
            float offset = - columnWidth;
            for (int k = 0; k < 10; k++)
            {
                offset += columnWidth / 5;
                canvas.drawLine(location.left + offset,
                                location.bottom,
                                location.right + offset,
                                location.top,
                                pSquareBg);
            }
            canvas.restore();
        }

        String text = Integer.toString(date.get(Calendar.DAY_OF_MONTH));
        canvas.drawText(text, location.centerX(),
            location.centerY() + squareTextOffset, pSquareFg);
    }

    private float getWeekdayLabelWidth()
    {
        float width = 0;

        for (String w : DateUtils.getShortWeekdayNames(firstWeekday))
            width = Math.max(width, pSquareFg.measureText(w));

        return width;
    }

    private void init()
    {
        isEditable = false;
        checkmarks = new int[0];
        controller = new Controller() {};
        target = 2;

        initColors();
        initPaints();
        initDateFormats();
        initRects();
    }

    private void initColors()
    {
        StyledResources res = new StyledResources(getContext());

        if (isBackgroundTransparent)
            primaryColor = ColorUtils.setMinValue(primaryColor, 0.75f);

        int red = Color.red(primaryColor);
        int green = Color.green(primaryColor);
        int blue = Color.blue(primaryColor);

        backgroundColor = res.getColor(R.attr.cardBgColor);

        if (isBackgroundTransparent)
        {
            colors = new int[3];
            colors[0] = Color.argb(16, 255, 255, 255);
            colors[1] = Color.argb(128, red, green, blue);
            colors[2] = primaryColor;

            textColors = new int[3];
            textColors[0] = Color.WHITE;
            textColors[1] = Color.WHITE;
            textColors[2] = Color.WHITE;
            reverseTextColor = Color.WHITE;
        }
        else
        {
            colors = new int[3];
            colors[0] = res.getColor(R.attr.lowContrastTextColor);
            colors[1] = Color.argb(127, red, green, blue);
            colors[2] = primaryColor;

            textColors = new int[3];
            textColors[0] = res.getColor(R.attr.lowContrastReverseTextColor);
            textColors[1] = res.getColor(R.attr.mediumContrastTextColor);
            textColors[2] = res.getColor(R.attr.highContrastReverseTextColor);
            reverseTextColor = res.getColor(R.attr.highContrastReverseTextColor);
        }
    }

    private void initDateFormats()
    {
        if (isInEditMode())
        {
            dfMonth = new SimpleDateFormat("MMM", Locale.getDefault());
            dfYear = new SimpleDateFormat("yyyy", Locale.getDefault());
        }
        else
        {
            dfMonth = AndroidDateFormats.fromSkeleton("MMM");
            dfYear = AndroidDateFormats.fromSkeleton("yyyy");
        }
    }

    private void initRects()
    {
        baseLocation = new RectF();
    }

    @Nullable
    private Timestamp positionToTimestamp(float x, float y)
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

        return new Timestamp(date.getTimeInMillis());
    }

    private void updateDate()
    {
        baseDate = DateUtils.getStartOfTodayCalendar();
        baseDate.add(Calendar.DAY_OF_YEAR, -(getDataOffset() - 1) * 7);

        nDays = (nColumns - 1) * 7;
        int realWeekday =
            DateUtils.getStartOfTodayCalendar().get(Calendar.DAY_OF_WEEK);
        todayPositionInColumn =
            (7 + realWeekday - firstWeekday) % 7;

        baseDate.add(Calendar.DAY_OF_YEAR, -nDays);
        baseDate.add(Calendar.DAY_OF_YEAR, -todayPositionInColumn);
    }

    public interface Controller
    {
        default void onToggleCheckmark(Timestamp timestamp, int value) {}
    }
}
