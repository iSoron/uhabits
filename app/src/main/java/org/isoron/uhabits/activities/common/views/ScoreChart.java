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
import android.support.annotation.*;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import java.text.*;
import java.util.*;

import static org.isoron.uhabits.utils.InterfaceUtils.*;

public class ScoreChart extends ScrollableChart
{
    private static final PorterDuffXfermode XFERMODE_CLEAR =
        new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private static final PorterDuffXfermode XFERMODE_SRC =
        new PorterDuffXfermode(PorterDuff.Mode.SRC);

    private Paint pGrid;

    private float em;

    private SimpleDateFormat dfMonth;

    private SimpleDateFormat dfDay;

    private SimpleDateFormat dfYear;

    private Paint pText, pGraph;

    private RectF rect, prevRect;

    private int baseSize;

    private int paddingTop;

    private float columnWidth;

    private int columnHeight;

    private int nColumns;

    private int textColor;

    private int gridColor;

    @Nullable
    private List<Score> scores;

    private int primaryColor;

    @Deprecated
    private int bucketSize = 7;

    private int backgroundColor;

    private Bitmap drawingCache;

    private Canvas cacheCanvas;

    private boolean isTransparencyEnabled;

    private int skipYear = 0;

    private String previousYearText;

    private String previousMonthText;

    public ScoreChart(Context context)
    {
        super(context);
        init();
    }

    public ScoreChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public void populateWithRandomData()
    {
        Random random = new Random();
        scores = new LinkedList<>();

        int previous = Score.MAX_VALUE / 2;
        long timestamp = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;

        for (int i = 1; i < 100; i++)
        {
            int step = Score.MAX_VALUE / 10;
            int current = previous + random.nextInt(step * 2) - step;
            current = Math.max(0, Math.min(Score.MAX_VALUE, current));
            scores.add(new Score(timestamp, current));
            previous = current;
            timestamp -= day;
        }
    }

    @Deprecated
    public void setBucketSize(int bucketSize)
    {
        this.bucketSize = bucketSize;
        postInvalidate();
    }

    public void setIsTransparencyEnabled(boolean enabled)
    {
        this.isTransparencyEnabled = enabled;
        initColors();
        requestLayout();
    }

    public void setColor(int primaryColor)
    {
        this.primaryColor = primaryColor;
        postInvalidate();
    }

    public void setScores(@NonNull List<Score> scores)
    {
        this.scores = scores;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Canvas activeCanvas;

        if (isTransparencyEnabled)
        {
            if (drawingCache == null) initCache(getWidth(), getHeight());

            activeCanvas = cacheCanvas;
            drawingCache.eraseColor(Color.TRANSPARENT);
        }
        else
        {
            activeCanvas = canvas;
        }

        if (scores == null) return;

        rect.set(0, 0, nColumns * columnWidth, columnHeight);
        rect.offset(0, paddingTop);

        drawGrid(activeCanvas, rect);

        pText.setColor(textColor);
        pGraph.setColor(primaryColor);
        prevRect.setEmpty();

        previousMonthText = "";
        previousYearText = "";
        skipYear = 0;

        for (int k = 0; k < nColumns; k++)
        {
            int offset = nColumns - k - 1 + getDataOffset();
            if (offset >= scores.size()) continue;

            int score = scores.get(offset).getValue();
            long timestamp = scores.get(offset).getTimestamp();

            double relativeScore = ((double) score) / Score.MAX_VALUE;
            int height = (int) (columnHeight * relativeScore);

            rect.set(0, 0, baseSize, baseSize);
            rect.offset(k * columnWidth + (columnWidth - baseSize) / 2,
                paddingTop + columnHeight - height - baseSize / 2);

            if (!prevRect.isEmpty())
            {
                drawLine(activeCanvas, prevRect, rect);
                drawMarker(activeCanvas, prevRect);
            }

            if (k == nColumns - 1) drawMarker(activeCanvas, rect);

            prevRect.set(rect);
            rect.set(0, 0, columnWidth, columnHeight);
            rect.offset(k * columnWidth, paddingTop);

            drawFooter(activeCanvas, rect, timestamp);
        }

        if (activeCanvas != canvas) canvas.drawBitmap(drawingCache, 0, 0, null);
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
        if (height < 9) height = 200;

        float maxTextSize = getResources().getDimension(R.dimen.tinyTextSize);
        float textSize = height * 0.06f;
        pText.setTextSize(Math.min(textSize, maxTextSize));
        em = pText.getFontSpacing();

        int footerHeight = (int) (3 * em);
        paddingTop = (int) (em);

        baseSize = (height - footerHeight - paddingTop) / 8;
        columnWidth = baseSize;
        columnWidth = Math.max(columnWidth, getMaxDayWidth() * 1.5f);
        columnWidth = Math.max(columnWidth, getMaxMonthWidth() * 1.2f);

        nColumns = (int) (width / columnWidth);
        columnWidth = (float) width / nColumns;
        setScrollerBucketSize((int) columnWidth);

        columnHeight = 8 * baseSize;

        float minStrokeWidth = dpToPixels(getContext(), 1);
        pGraph.setTextSize(baseSize * 0.5f);
        pGraph.setStrokeWidth(baseSize * 0.1f);
        pGrid.setStrokeWidth(Math.min(minStrokeWidth, baseSize * 0.05f));

        if (isTransparencyEnabled) initCache(width, height);
    }

    private void drawFooter(Canvas canvas, RectF rect, long currentDate)
    {
        String yearText = dfYear.format(currentDate);
        String monthText = dfMonth.format(currentDate);
        String dayText = dfDay.format(currentDate);

        GregorianCalendar calendar = DateUtils.getCalendar(currentDate);

        String text;
        int year = calendar.get(Calendar.YEAR);

        boolean shouldPrintYear = true;
        if (yearText.equals(previousYearText)) shouldPrintYear = false;
        if (bucketSize >= 365 && (year % 2) != 0) shouldPrintYear = false;

        if (skipYear > 0)
        {
            skipYear--;
            shouldPrintYear = false;
        }

        if (shouldPrintYear)
        {
            previousYearText = yearText;
            previousMonthText = "";

            pText.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(yearText, rect.centerX(), rect.bottom + em * 2.2f,
                pText);

            skipYear = 1;
        }

        if (bucketSize < 365)
        {
            if (!monthText.equals(previousMonthText))
            {
                previousMonthText = monthText;
                text = monthText;
            }
            else
            {
                text = dayText;
            }

            pText.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(text, rect.centerX(), rect.bottom + em * 1.2f,
                pText);
        }
    }

    private void drawGrid(Canvas canvas, RectF rGrid)
    {
        int nRows = 5;
        float rowHeight = rGrid.height() / nRows;

        pText.setTextAlign(Paint.Align.LEFT);
        pText.setColor(textColor);
        pGrid.setColor(gridColor);

        for (int i = 0; i < nRows; i++)
        {
            canvas.drawText(String.format("%d%%", (100 - i * 100 / nRows)),
                rGrid.left + 0.5f * em, rGrid.top + 1f * em, pText);
            canvas.drawLine(rGrid.left, rGrid.top, rGrid.right, rGrid.top,
                pGrid);
            rGrid.offset(0, rowHeight);
        }

        canvas.drawLine(rGrid.left, rGrid.top, rGrid.right, rGrid.top, pGrid);
    }

    private void drawLine(Canvas canvas, RectF rectFrom, RectF rectTo)
    {
        pGraph.setColor(primaryColor);
        canvas.drawLine(rectFrom.centerX(), rectFrom.centerY(),
            rectTo.centerX(), rectTo.centerY(), pGraph);
    }

    private void drawMarker(Canvas canvas, RectF rect)
    {
        rect.inset(baseSize * 0.225f, baseSize * 0.225f);
        setModeOrColor(pGraph, XFERMODE_CLEAR, backgroundColor);
        canvas.drawOval(rect, pGraph);

        rect.inset(baseSize * 0.1f, baseSize * 0.1f);
        setModeOrColor(pGraph, XFERMODE_SRC, primaryColor);
        canvas.drawOval(rect, pGraph);

//        rect.inset(baseSize * 0.1f, baseSize * 0.1f);
//        setModeOrColor(pGraph, XFERMODE_CLEAR, backgroundColor);
//        canvas.drawOval(rect, pGraph);

        if (isTransparencyEnabled) pGraph.setXfermode(XFERMODE_SRC);
    }

    private float getMaxDayWidth()
    {
        float maxDayWidth = 0;
        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();

        for (int i = 0; i < 28; i++)
        {
            day.set(Calendar.DAY_OF_MONTH, i);
            float monthWidth = pText.measureText(dfMonth.format(day.getTime()));
            maxDayWidth = Math.max(maxDayWidth, monthWidth);
        }

        return maxDayWidth;
    }

    private float getMaxMonthWidth()
    {
        float maxMonthWidth = 0;
        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();

        for (int i = 0; i < 12; i++)
        {
            day.set(Calendar.MONTH, i);
            float monthWidth = pText.measureText(dfMonth.format(day.getTime()));
            maxMonthWidth = Math.max(maxMonthWidth, monthWidth);
        }

        return maxMonthWidth;
    }

    private void init()
    {
        initPaints();
        initColors();
        initDateFormats();
        initRects();
    }

    private void initCache(int width, int height)
    {
        if (drawingCache != null) drawingCache.recycle();
        drawingCache =
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas(drawingCache);
    }

    private void initColors()
    {
        StyledResources res = new StyledResources(getContext());

        primaryColor = Color.BLACK;
        textColor = res.getColor(R.attr.mediumContrastTextColor);
        gridColor = res.getColor(R.attr.lowContrastTextColor);
        backgroundColor = res.getColor(R.attr.cardBackgroundColor);
    }

    private void initDateFormats()
    {
        dfYear = DateFormats.fromSkeleton("yyyy");
        dfMonth = DateFormats.fromSkeleton("MMM");
        dfDay = DateFormats.fromSkeleton("d");
    }

    private void initPaints()
    {
        pText = new Paint();
        pText.setAntiAlias(true);

        pGraph = new Paint();
        pGraph.setTextAlign(Paint.Align.CENTER);
        pGraph.setAntiAlias(true);

        pGrid = new Paint();
        pGrid.setAntiAlias(true);
    }

    private void initRects()
    {
        rect = new RectF();
        prevRect = new RectF();
    }

    private void setModeOrColor(Paint p, PorterDuffXfermode mode, int color)
    {
        if (isTransparencyEnabled) p.setXfermode(mode);
        else p.setColor(color);
    }
}
