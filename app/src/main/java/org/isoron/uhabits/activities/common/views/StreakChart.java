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
import android.util.*;
import android.view.*;
import android.view.ViewGroup.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import java.text.*;
import java.util.*;

import static android.view.View.MeasureSpec.*;

public class StreakChart extends View
{
    private Paint paint;

    private long minLength;

    private long maxLength;

    private int[] colors;

    private RectF rect;

    private int baseSize;

    private int primaryColor;

    private List<Streak> streaks;

    private boolean isBackgroundTransparent;

    private DateFormat dateFormat;

    private int width;

    private float em;

    private float maxLabelWidth;

    private float textMargin;

    private boolean shouldShowLabels;

    private int textColor;

    private int reverseTextColor;

    public StreakChart(Context context)
    {
        super(context);
        init();
    }

    public StreakChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    /**
     * Returns the maximum number of streaks this view is able to show, given
     * its current size.
     *
     * @return max number of visible streaks
     */
    public int getMaxStreakCount()
    {
        return (int) Math.floor(getMeasuredHeight() / baseSize);
    }

    public void populateWithRandomData()
    {
        long day = DateUtils.millisecondsInOneDay;
        long start = DateUtils.getStartOfToday();
        LinkedList<Streak> streaks = new LinkedList<>();

        for (int i = 0; i < 10; i++)
        {
            int length = new Random().nextInt(100);
            long end = start + length * day;
            streaks.add(new Streak(start, end));
            start = end + day;
        }

        setStreaks(streaks);
    }

    public void setColor(int color)
    {
        this.primaryColor = color;
        postInvalidate();
    }

    public void setIsBackgroundTransparent(boolean isBackgroundTransparent)
    {
        this.isBackgroundTransparent = isBackgroundTransparent;
        initColors();
    }

    public void setStreaks(List<Streak> streaks)
    {
        this.streaks = streaks;
        initColors();
        updateMaxMinLengths();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (streaks.size() == 0) return;

        rect.set(0, 0, width, baseSize);

        for (Streak s : streaks)
        {
            drawRow(canvas, s, rect);
            rect.offset(0, baseSize);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec)
    {
        LayoutParams params = getLayoutParams();

        if (params != null && params.height == LayoutParams.WRAP_CONTENT)
        {
            int width = getSize(widthSpec);
            int height = streaks.size() * baseSize;

            heightSpec = makeMeasureSpec(height, EXACTLY);
            widthSpec = makeMeasureSpec(width, EXACTLY);
        }

        setMeasuredDimension(widthSpec, heightSpec);
    }

    @Override
    protected void onSizeChanged(int width,
                                 int height,
                                 int oldWidth,
                                 int oldHeight)
    {
        this.width = width;

        float minTextSize = getResources().getDimension(R.dimen.tinyTextSize);
        float maxTextSize =
            getResources().getDimension(R.dimen.regularTextSize);
        float textSize = baseSize * 0.5f;

        paint.setTextSize(
            Math.max(Math.min(textSize, maxTextSize), minTextSize));
        em = paint.getFontSpacing();
        textMargin = 0.5f * em;

        updateMaxMinLengths();
    }

    private void drawRow(Canvas canvas, Streak streak, RectF rect)
    {
        if (maxLength == 0) return;

        float percentage = (float) streak.getLength() / maxLength;
        float availableWidth = width - 2 * maxLabelWidth;
        if (shouldShowLabels) availableWidth -= 2 * textMargin;

        float barWidth = percentage * availableWidth;
        float minBarWidth =
            paint.measureText(Long.toString(streak.getLength())) + em;
        barWidth = Math.max(barWidth, minBarWidth);

        float gap = (width - barWidth) / 2;
        float paddingTopBottom = baseSize * 0.05f;

        paint.setColor(percentageToColor(percentage));

        canvas.drawRect(rect.left + gap, rect.top + paddingTopBottom,
            rect.right - gap, rect.bottom - paddingTopBottom, paint);

        float yOffset = rect.centerY() + 0.3f * em;

        paint.setColor(reverseTextColor);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(Long.toString(streak.getLength()), rect.centerX(),
            yOffset, paint);

        if (shouldShowLabels)
        {
            String startLabel = dateFormat.format(new Date(streak.getStart()));
            String endLabel = dateFormat.format(new Date(streak.getEnd()));

            paint.setColor(textColor);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(startLabel, gap - textMargin, yOffset, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(endLabel, width - gap + textMargin, yOffset, paint);
        }
    }

    private void init()
    {
        initPaints();
        initColors();

        streaks = Collections.emptyList();

        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        rect = new RectF();
        baseSize = getResources().getDimensionPixelSize(R.dimen.baseSize);
    }

    private void initColors()
    {
        int red = Color.red(primaryColor);
        int green = Color.green(primaryColor);
        int blue = Color.blue(primaryColor);

        StyledResources res = new StyledResources(getContext());

        colors = new int[4];
        colors[3] = primaryColor;
        colors[2] = Color.argb(192, red, green, blue);
        colors[1] = Color.argb(96, red, green, blue);
        colors[0] = res.getColor(R.attr.lowContrastTextColor);
        textColor = res.getColor(R.attr.mediumContrastTextColor);
        reverseTextColor = res.getColor(R.attr.highContrastReverseTextColor);
    }

    private void initPaints()
    {
        paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
    }

    private int percentageToColor(float percentage)
    {
        if (percentage >= 1.0f) return colors[3];
        if (percentage >= 0.8f) return colors[2];
        if (percentage >= 0.5f) return colors[1];
        return colors[0];
    }

    private void updateMaxMinLengths()
    {
        maxLength = 0;
        minLength = Long.MAX_VALUE;
        shouldShowLabels = true;

        for (Streak s : streaks)
        {
            maxLength = Math.max(maxLength, s.getLength());
            minLength = Math.min(minLength, s.getLength());

            float lw1 =
                paint.measureText(dateFormat.format(new Date(s.getStart())));
            float lw2 =
                paint.measureText(dateFormat.format(new Date(s.getEnd())));
            maxLabelWidth = Math.max(maxLabelWidth, Math.max(lw1, lw2));
        }

        if (width - 2 * maxLabelWidth < width * 0.25f)
        {
            maxLabelWidth = 0;
            shouldShowLabels = false;
        }
    }
}
