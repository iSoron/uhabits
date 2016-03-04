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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class HabitScoreView extends ScrollableDataView
{
    public static final int BUCKET_SIZE = 7;
    public static final PorterDuffXfermode XFERMODE_CLEAR =
            new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    public static final PorterDuffXfermode XFERMODE_SRC =
            new PorterDuffXfermode(PorterDuff.Mode.SRC);

    private Paint pGrid;
    private float em;
    private Habit habit;
    private SimpleDateFormat dfMonth;
    private SimpleDateFormat dfDay;

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
    private int[] scores;
    private int primaryColor;
    private boolean isBackgroundTransparent;

    public HabitScoreView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.primaryColor = ColorHelper.palette[7];
        init();
    }

    public void setHabit(Habit habit)
    {
        this.habit = habit;
        createColors();
        fetchData();
        postInvalidate();
    }

    private void init()
    {
        createPaints();
        createColors();

        dfMonth = new SimpleDateFormat("MMM", Locale.getDefault());
        dfDay = new SimpleDateFormat("d", Locale.getDefault());

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

        baseSize = height / 9;
        setScrollerBucketSize(baseSize);

        columnWidth = baseSize;
        columnHeight = 8 * baseSize;
        nColumns = width / baseSize;
        paddingTop = (int) (baseSize * 0.15f);

        pText.setTextSize(baseSize * 0.5f);
        pGraph.setTextSize(baseSize * 0.5f);
        pGraph.setStrokeWidth(baseSize * 0.1f);
        pGrid.setStrokeWidth(baseSize * 0.05f);
        em = pText.getFontSpacing();
    }

    protected void fetchData()
    {
        if(isInEditMode())
            generateRandomData();
        else
        {
            if (habit == null)
            {
                scores = new int[0];
                return;
            }

            scores = habit.scores.getAllValues(BUCKET_SIZE * DateHelper.millisecondsInOneDay);
        }

    }

    private void generateRandomData()
    {
        Random random = new Random();
        scores = new int[100];
        scores[0] = Score.MAX_SCORE / 2;

        for(int i = 1; i < 100; i++)
        {
            int step = Score.MAX_SCORE / 10;
            scores[i] = scores[i - 1] + random.nextInt(step * 2) - step;
            scores[i] = Math.max(0, Math.min(Score.MAX_SCORE, scores[i]));
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        float lineHeight = pText.getFontSpacing();

        rect.set(0, 0, nColumns * columnWidth, columnHeight);
        rect.offset(0, paddingTop);

        drawGrid(canvas, rect);

        String previousMonth = "";

        pText.setTextAlign(Paint.Align.CENTER);
        pText.setColor(textColor);
        pGraph.setColor(primaryColor);
        prevRect.setEmpty();

        long currentDate = DateHelper.getStartOfToday();

        for(int k = 0; k < nColumns + getDataOffset() - 1; k++)
            currentDate -= 7 * DateHelper.millisecondsInOneDay;

        for (int k = 0; k < nColumns; k++)
        {
            String month = dfMonth.format(currentDate);
            String day = dfDay.format(currentDate);

            int score = 0;
            int offset = nColumns - k - 1 + getDataOffset();
            if(offset < scores.length) score = scores[offset];

            double sRelative = ((double) score) / Score.MAX_SCORE;
            int height = (int) (columnHeight * sRelative);

            rect.set(0, 0, baseSize, baseSize);
            rect.offset(k * columnWidth, paddingTop + columnHeight - height - columnWidth / 2);

            if (!prevRect.isEmpty())
            {
                drawLine(canvas, prevRect, rect);
                drawMarker(canvas, prevRect);
            }

            if (k == nColumns - 1) drawMarker(canvas, rect);

            prevRect.set(rect);

            rect.set(0, 0, columnWidth, columnHeight);
            rect.offset(k * columnWidth, paddingTop);

            if (!month.equals(previousMonth))
                canvas.drawText(month, rect.centerX(), rect.bottom + lineHeight * 1.2f, pText);
            else
                canvas.drawText(day, rect.centerX(), rect.bottom + lineHeight * 1.2f, pText);

            previousMonth = month;
            currentDate += 7 * DateHelper.millisecondsInOneDay;
        }
    }

    private void drawGrid(Canvas canvas, RectF rGrid)
    {
        int nRows = 5;
        float rowHeight = rGrid.height() / nRows;

        pText.setTextAlign(Paint.Align.LEFT);
        pText.setColor(textColor);
        pGrid.setColor(dimmedTextColor);

        for (int i = 0; i < nRows; i++)
        {
            canvas.drawText(String.format("%d%%", (100 - i * 100 / nRows)), rGrid.left + 0.5f * em,
                    rGrid.top + 1f * em, pText);
            canvas.drawLine(rGrid.left, rGrid.top, rGrid.right, rGrid.top, pGrid);
            rGrid.offset(0, rowHeight);
        }

        canvas.drawLine(rGrid.left, rGrid.top, rGrid.right, rGrid.top, pGrid);
    }

    private void drawLine(Canvas canvas, RectF rectFrom, RectF rectTo)
    {
        pGraph.setColor(primaryColor);
        canvas.drawLine(rectFrom.centerX(), rectFrom.centerY(), rectTo.centerX(), rectTo.centerY(),
                pGraph);
    }

    private void drawMarker(Canvas canvas, RectF rect)
    {
        rect.inset(columnWidth * 0.15f, columnWidth * 0.15f);
        setModeOrColor(pGraph, XFERMODE_CLEAR, Color.WHITE);
        canvas.drawOval(rect, pGraph);

        rect.inset(columnWidth * 0.1f, columnWidth * 0.1f);
        setModeOrColor(pGraph, XFERMODE_SRC, primaryColor);
        canvas.drawOval(rect, pGraph);

        rect.inset(columnWidth * 0.1f, columnWidth * 0.1f);
        setModeOrColor(pGraph, XFERMODE_CLEAR, Color.WHITE);
        canvas.drawOval(rect, pGraph);

        if(isBackgroundTransparent)
            pGraph.setXfermode(XFERMODE_SRC);
    }

    public void setIsBackgroundTransparent(boolean isBackgroundTransparent)
    {
        this.isBackgroundTransparent = isBackgroundTransparent;
        createColors();
    }

    private void setModeOrColor(Paint p, PorterDuffXfermode mode, int color)
    {
        if(isBackgroundTransparent)
            p.setXfermode(mode);
        else
            p.setColor(color);
    }
}
