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

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

import java.text.SimpleDateFormat;

public class HabitScoreView extends ScrollableDataView
{
    public static final int BUCKET_SIZE = 7;

    private final Paint pGrid;
    private final float em;
    private Habit habit;

    private Paint pText, pGraph;

    private int[] colors;
    private int[] scores;

    public HabitScoreView(Context context, Habit habit, int columnWidth)
    {
        super(context);
        this.habit = habit;

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

        this.columnWidth = columnWidth;
        columnHeight = 8 * columnWidth;
        headerHeight = columnWidth;
        footerHeight = columnWidth;

        em = pText.getFontSpacing();

        colors = new int[4];

        colors[0] = Color.rgb(230, 230, 230);
        colors[3] = habit.color;
        colors[1] = ColorHelper.mixColors(colors[0], colors[3], 0.66f);
        colors[2] = ColorHelper.mixColors(colors[0], colors[3], 0.33f);
    }

    protected void fetchData()
    {
        scores = habit.getAllScores(BUCKET_SIZE * DateHelper.millisecondsInOneDay);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        float lineHeight = pText.getFontSpacing();

        RectF rGrid = new RectF(0, 0, nColumns * columnWidth, columnHeight);
        rGrid.offset(0, headerHeight);
        drawGrid(canvas, rGrid);

        SimpleDateFormat dfMonth = new SimpleDateFormat("MMM");
        SimpleDateFormat dfDay = new SimpleDateFormat("d");

        String previousMonth = "";

        pGraph.setColor(habit.color);
        RectF prevR = null;

        long currentDate = DateHelper.getStartOfToday();

        for(int k = 0; k < nColumns + dataOffset - 1; k++)
            currentDate -= 7 * DateHelper.millisecondsInOneDay;

        for (int k = 0; k < nColumns; k++)
        {
            String month = dfMonth.format(currentDate);
            String day = dfDay.format(currentDate);

            int score = 0;
            int offset = nColumns - k - 1 + dataOffset;
            if(offset < scores.length) score = scores[offset];

            double sRelative = ((double) score) / Habit.MAX_SCORE;
            int height = (int) (columnHeight * sRelative);

            RectF r = new RectF(0, 0, columnWidth, columnWidth);
            r.offset(k * columnWidth,
                    headerHeight + columnHeight - height - columnWidth / 2);

            if (prevR != null)
            {
                drawLine(canvas, prevR, r);
                drawMarker(canvas, prevR);
            }

            if (k == nColumns - 1) drawMarker(canvas, r);

            prevR = r;

            r = new RectF(0, 0, columnWidth, columnHeight);
            r.offset(k * columnWidth, headerHeight);
            if (!month.equals(previousMonth))
                canvas.drawText(month, r.centerX(), r.bottom + lineHeight * 1.2f, pText);
            else
                canvas.drawText(day, r.centerX(), r.bottom + lineHeight * 1.2f, pText);

            previousMonth = month;
            currentDate += 7 * DateHelper.millisecondsInOneDay;
        }
    }

    private void drawGrid(Canvas canvas, RectF rGrid)
    {
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
}
