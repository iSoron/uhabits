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

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

public abstract class ScrollableDataView extends View
{

    protected int dataOffset;
    protected int nColumns;
    protected int columnWidth, columnHeight;
    protected int headerHeight, footerHeight;

    private float prevX, prevY;

    public ScrollableDataView(Context context)
    {
        super(context);
    }

    protected abstract void fetchData();

    protected boolean move(float dx)
    {
        int newDataOffset = dataOffset + (int) (dx / columnWidth);
        newDataOffset = Math.max(0, newDataOffset);

        if (newDataOffset != dataOffset)
        {
            dataOffset = newDataOffset;
            fetchData();
            invalidate();
            return true;
        }
        else return false;
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
}
