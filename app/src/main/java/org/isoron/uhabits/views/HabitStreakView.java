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
import android.graphics.Rect;
import android.util.AttributeSet;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Streak;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HabitStreakView extends ScrollableDataView
{
    private Habit habit;
    private Paint pText, pBar;

    private long[] startTimes;
    private long[] endTimes;
    private long[] lengths;

    private long maxStreakLength;
    private int[] colors;
    private SimpleDateFormat dfMonth;
    private Rect rect;
    private int baseSize;
    private int primaryColor;

    public HabitStreakView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.baseSize = (int) context.getResources().getDimension(R.dimen.small_square_size);
        this.primaryColor = ColorHelper.palette[7];
        init();
    }

    public void setHabit(Habit habit)
    {
        this.habit = habit;
        this.primaryColor = habit.color;
        createColors();
        fetchData();
        postInvalidate();
    }

    private void init()
    {
        setDimensions(baseSize);
        createPaints();
        createColors();

        dfMonth = new SimpleDateFormat("MMM", Locale.getDefault());
        rect = new Rect();
    }

    private void setDimensions(int baseSize)
    {
        this.columnWidth = baseSize;
        columnHeight = 8 * baseSize;
        headerHeight = baseSize;
        footerHeight = baseSize;
    }

    private void createColors()
    {
        colors = new int[4];
        colors[0] = Color.rgb(230, 230, 230);
        colors[3] = primaryColor;
        colors[1] = ColorHelper.mixColors(colors[0], colors[3], 0.66f);
        colors[2] = ColorHelper.mixColors(colors[0], colors[3], 0.33f);
    }

    private void createPaints()
    {
        pText = new Paint();
        pText.setColor(Color.LTGRAY);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTextSize(columnWidth * 0.5f);
        pText.setAntiAlias(true);

        pBar = new Paint();
        pBar.setTextAlign(Paint.Align.CENTER);
        pBar.setTextSize(columnWidth * 0.5f);
        pBar.setAntiAlias(true);
    }

    protected void fetchData()
    {
        if(isInEditMode())
            generateRandomData();
        else
        {
            if(habit == null)
            {
                startTimes = endTimes = lengths = new long[0];
                return;
            }

            List<Streak> streaks = habit.getStreaks();
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
        int start = nStreaks - nColumns - dataOffset;

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
            canvas.drawText(Long.toString(l), rect.centerX(), rect.top - barHeaderOffset, pBar);

            if (!month.equals(previousMonth))
                canvas.drawText(month, rect.centerX(), rect.bottom + lineHeight * 1.2f, pText);

            previousMonth = month;
        }
    }
}
