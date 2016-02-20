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
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;

import java.text.SimpleDateFormat;
import java.util.List;

public class HabitScoreView extends View
{
    public static final int BUCKET_SIZE = 7;

    private final Paint pGrid;
    private final float em;
    private Habit habit;
    private int columnWidth, columnHeight, nColumns;

    private Paint pText, pGraph;
    private int dataOffset;

    private int barHeaderHeight;

    private int[] colors;
    private float prevX;
    private float prevY;
    private List<Score> scores;

    public HabitScoreView(Context context, Habit habit, int columnWidth)
    {
        super(context);
        this.habit = habit;
        this.columnWidth = columnWidth;

        pText = new Paint();
        pText.setColor(Color.LTGRAY);
        pText.setTextAlign(Paint.Align.LEFT);
        pText.setTextSize(columnWidth * 0.5f);
        pText.setAntiAlias(true);

        pGraph = new Paint();
        pGraph.setTextAlign(Paint.Align.CENTER);
        pGraph.setTextSize(columnWidth * 0.5f);
        pGraph.setAntiAlias(true);
        pGraph.setStrokeWidth(columnWidth * 0.1f);

        pGrid = new Paint();
        pGrid.setColor(Color.LTGRAY);
        pGrid.setAntiAlias(true);
        pGrid.setStrokeWidth(columnWidth * 0.05f);

        columnHeight = 8 * columnWidth;
        barHeaderHeight = columnWidth;
        em = pText.getFontSpacing();

        colors = new int[4];

        colors[0] = Color.rgb(230, 230, 230);
        colors[3] = habit.color;
        colors[1] = ColorHelper.mixColors(colors[0], colors[3], 0.66f);
        colors[2] = ColorHelper.mixColors(colors[0], colors[3], 0.33f);
    }

    private void fetchScores()
    {

        long toTimestamp = DateHelper.getStartOfToday();
        for (int i = 0; i < dataOffset * BUCKET_SIZE; i++)
            toTimestamp -= DateHelper.millisecondsInOneDay;

        long fromTimestamp = toTimestamp;
        for (int i = 0; i < nColumns * BUCKET_SIZE; i++)
            fromTimestamp -= DateHelper.millisecondsInOneDay;

        scores = habit.getScores(fromTimestamp, toTimestamp,
                BUCKET_SIZE * DateHelper.millisecondsInOneDay, toTimestamp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), columnHeight + 2 * barHeaderHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        nColumns = w / columnWidth;
        fetchScores();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        float lineHeight = pText.getFontSpacing();
        float barHeaderOffset = lineHeight * 0.4f;

        RectF rGrid = new RectF(0, 0, nColumns * columnWidth, columnHeight);
        rGrid.offset(0, barHeaderHeight);
        drawGrid(canvas, rGrid);

        SimpleDateFormat dfMonth = new SimpleDateFormat("MMM");
        SimpleDateFormat dfDay = new SimpleDateFormat("d");

        String previousMonth = "";

        pGraph.setColor(habit.color);
        RectF prevR = null;

        for (int offset = nColumns - scores.size(); offset < nColumns; offset++)
        {
            Score score = scores.get(offset - nColumns + scores.size());
            String month = dfMonth.format(score.timestamp);
            String day = dfDay.format(score.timestamp);

            long s = score.score;
            double sRelative = ((double) s) / Habit.MAX_SCORE;

            int height = (int) (columnHeight * sRelative);

            RectF r = new RectF(0, 0, columnWidth, columnWidth);
            r.offset(offset * columnWidth,
                    barHeaderHeight + columnHeight - height - columnWidth / 2);

            if (prevR != null)
            {
                drawLine(canvas, prevR, r);
                drawMarker(canvas, prevR);
            }

            if (offset == nColumns - 1) drawMarker(canvas, r);

            prevR = r;

            r = new RectF(0, 0, columnWidth, columnHeight);
            r.offset(offset * columnWidth, barHeaderHeight);
            if (!month.equals(previousMonth))
            {
                canvas.drawText(month, r.centerX(), r.bottom + lineHeight * 1.2f, pText);
            }
            else
            {
                canvas.drawText(day, r.centerX(), r.bottom + lineHeight * 1.2f, pText);
            }

            previousMonth = month;

        }
    }

    private void drawGrid(Canvas canvas, RectF rGrid)
    {
//        pGrid.setColor(Color.rgb(230, 230, 230));
//        pGrid.setStyle(Paint.Style.STROKE);
//        canvas.drawRect(rGrid, pGrid);

        int nRows = 5;
        float rowHeight = rGrid.height() / nRows;

        pGrid.setColor(Color.rgb(240, 240, 240));
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
        pGraph.setColor(habit.color);
        canvas.drawLine(rectFrom.centerX(), rectFrom.centerY(), rectTo.centerX(), rectTo.centerY(),
                pGraph);
    }

    private void drawMarker(Canvas canvas, RectF rect)
    {
        rect.inset(columnWidth * 0.15f, columnWidth * 0.15f);
        pGraph.setColor(Color.WHITE);
        canvas.drawOval(rect, pGraph);

        rect.inset(columnWidth * 0.1f, columnWidth * 0.1f);
        pGraph.setColor(habit.color);
        canvas.drawOval(rect, pGraph);

        rect.inset(columnWidth * 0.1f, columnWidth * 0.1f);
        pGraph.setColor(Color.WHITE);
        canvas.drawOval(rect, pGraph);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();

        int pointerIndex = MotionEventCompat.getActionIndex(event);
        final float x = MotionEventCompat.getX(event, pointerIndex);
        final float y = MotionEventCompat.getY(event, pointerIndex);

        if (action == MotionEvent.ACTION_DOWN)
        {
            prevX = x;
            prevY = y;
        }

        if (action == MotionEvent.ACTION_MOVE)
        {
            float dx = x - prevX;
            float dy = y - prevY;

            if (Math.abs(dy) > Math.abs(dx)) return false;
            getParent().requestDisallowInterceptTouchEvent(true);
            if (move(dx))
            {
                prevX = x;
                prevY = y;
            }
        }

        return true;
    }

    private boolean move(float dx)
    {
        int newDataOffset = dataOffset + (int) (dx / columnWidth);
        newDataOffset = Math.max(0, newDataOffset);

        if (newDataOffset != dataOffset)
        {
            dataOffset = newDataOffset;
            fetchScores();
            invalidate();
            return true;
        }
        else return false;
    }
}
