/*
 * Copyright (C) 2016 Alinson Santos Xavier
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
 *
 *
 */

package org.isoron.uhabits.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

public abstract class ScrollableDataView extends View implements GestureDetector.OnGestureListener,
        ValueAnimator.AnimatorUpdateListener
{

    protected int dataOffset;
    protected int nColumns;
    protected int columnWidth, columnHeight;
    protected int headerHeight, footerHeight;

    private GestureDetector detector;
    private Scroller scroller;
    private ValueAnimator scrollAnimator;

    public ScrollableDataView(Context context)
    {
        super(context);
        init(context);
    }

    public ScrollableDataView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    private void init(Context context)
    {
        detector = new GestureDetector(context, this);
        scroller = new Scroller(context, null, true);
        scrollAnimator = ValueAnimator.ofFloat(0, 1);
        scrollAnimator.addUpdateListener(this);
    }

    protected abstract void fetchData();

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return detector.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), columnHeight + headerHeight + footerHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        nColumns = w / columnWidth;
        fetchData();
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
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
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy)
    {
        if(Math.abs(dx) > Math.abs(dy))
            getParent().requestDisallowInterceptTouchEvent(true);

        scroller.startScroll(scroller.getCurrX(), scroller.getCurrY(), (int) -dx, (int) dy, 0);
        scroller.computeScrollOffset();
        dataOffset = Math.max(0, scroller.getCurrX() / columnWidth);
        postInvalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        scroller.fling(scroller.getCurrX(), scroller.getCurrY(), (int) velocityX / 2, 0, 0, 100000,
                0, 0);
        invalidate();

        scrollAnimator.setDuration(scroller.getDuration());
        scrollAnimator.start();

        return false;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation)
    {
        if (!scroller.isFinished())
        {
            scroller.computeScrollOffset();
            dataOffset = Math.max(0, scroller.getCurrX() / columnWidth);
            postInvalidate();
        }
        else
        {
            scrollAnimator.cancel();
        }
    }
}
