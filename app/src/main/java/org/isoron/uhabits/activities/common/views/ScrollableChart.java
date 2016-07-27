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

import android.animation.*;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public abstract class ScrollableChart extends View
    implements GestureDetector.OnGestureListener,
               ValueAnimator.AnimatorUpdateListener
{

    private int dataOffset;

    private int scrollerBucketSize;

    private GestureDetector detector;

    private Scroller scroller;

    private ValueAnimator scrollAnimator;

    public ScrollableChart(Context context)
    {
        super(context);
        init(context);
    }

    public ScrollableChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public int getDataOffset()
    {
        return dataOffset;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation)
    {
        if (!scroller.isFinished())
        {
            scroller.computeScrollOffset();
            dataOffset = Math.max(0, scroller.getCurrX() / scrollerBucketSize);
            postInvalidate();
        }
        else
        {
            scrollAnimator.cancel();
        }
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1,
                           MotionEvent e2,
                           float velocityX,
                           float velocityY)
    {
        scroller.fling(scroller.getCurrX(), scroller.getCurrY(),
            (int) velocityX / 2, 0, 0, 100000, 0, 0);
        invalidate();

        scrollAnimator.setDuration(scroller.getDuration());
        scrollAnimator.start();

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy)
    {
        if (scrollerBucketSize == 0) return false;

        if (Math.abs(dx) > Math.abs(dy))
        {
            ViewParent parent = getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
        }

        scroller.startScroll(scroller.getCurrX(), scroller.getCurrY(),
            (int) -dx, (int) dy, 0);
        scroller.computeScrollOffset();
        dataOffset = Math.max(0, scroller.getCurrX() / scrollerBucketSize);
        postInvalidate();

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return detector.onTouchEvent(event);
    }

    public void setScrollerBucketSize(int scrollerBucketSize)
    {
        this.scrollerBucketSize = scrollerBucketSize;
    }

    private void init(Context context)
    {
        detector = new GestureDetector(context, this);
        scroller = new Scroller(context, null, true);
        scrollAnimator = ValueAnimator.ofFloat(0, 1);
        scrollAnimator.addUpdateListener(this);
    }
}
