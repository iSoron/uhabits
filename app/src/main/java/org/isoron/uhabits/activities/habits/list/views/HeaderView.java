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

package org.isoron.uhabits.activities.habits.list.views;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.support.annotation.*;
import android.text.*;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.activities.habits.list.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

public class HeaderView extends ScrollableChart
    implements Preferences.Listener, MidnightTimer.MidnightListener
{

    private int buttonCount;

    @Nullable
    private Preferences prefs;

    @Nullable
    private MidnightTimer midnightTimer;

    private final TextPaint paint;

    private RectF rect;

    private int maxDataOffset;

    public HeaderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        if (isInEditMode())
        {
            setButtonCount(5);
        }

        Context appContext = context.getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
        }

        if (context instanceof ListHabitsActivity)
        {
            ListHabitsActivity activity = (ListHabitsActivity) context;
            midnightTimer = activity.getListHabitsComponent().getMidnightTimer();
        }

        Resources res = context.getResources();
        setScrollerBucketSize((int) res.getDimension(R.dimen.checkmarkWidth));
        setDirection(shouldReverseCheckmarks() ? 1 : -1);

        StyledResources sr = new StyledResources(context);
        paint = new TextPaint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(getResources().getDimension(R.dimen.tinyTextSize));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(sr.getColor(R.attr.mediumContrastTextColor));

        rect = new RectF();
    }

    @Override
    public void atMidnight()
    {
        post(() -> invalidate());
    }

    @Override
    public void onCheckmarkOrderChanged()
    {
        setDirection(shouldReverseCheckmarks() ? 1 : -1);
        postInvalidate();
    }

    public void setButtonCount(int buttonCount)
    {
        this.buttonCount = buttonCount;
        postInvalidate();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (prefs != null) prefs.addListener(this);
        if (midnightTimer != null) midnightTimer.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (midnightTimer != null) midnightTimer.removeListener(this);
        if (prefs != null) prefs.removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) getContext()
            .getResources()
            .getDimension(R.dimen.checkmarkHeight);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();
        Resources res = getContext().getResources();
        float width = res.getDimension(R.dimen.checkmarkWidth);
        float height = res.getDimension(R.dimen.checkmarkHeight);
        boolean reverse = shouldReverseCheckmarks();

        day.add(GregorianCalendar.DAY_OF_MONTH, -getDataOffset());
        float em = paint.measureText("m");

        for (int i = 0; i < buttonCount; i++)
        {
            rect.set(0, 0, width, height);
            rect.offset(canvas.getWidth(), 0);
            if(reverse) rect.offset(- (i + 1) * width, 0);
            else rect.offset((i - buttonCount) * width, 0);

            String text = DateUtils.formatHeaderDate(day).toUpperCase();
            String[] lines = text.split("\n");

            int y1 = (int)(rect.centerY() - 0.25 * em);
            int y2 = (int)(rect.centerY() + 1.25 * em);

            canvas.drawText(lines[0], rect.centerX(), y1, paint);
            canvas.drawText(lines[1], rect.centerX(), y2, paint);
            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }
    }

    private boolean shouldReverseCheckmarks()
    {
        if (prefs == null) return false;
        return prefs.shouldReverseCheckmarks();
    }
}
