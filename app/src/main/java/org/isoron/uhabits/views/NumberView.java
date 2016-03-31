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

package org.isoron.uhabits.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.DialogHelper;

public class NumberView extends View
{
    private int color;
    private int number;
    private float labelMarginTop;
    private TextPaint pText;
    private String label;
    private RectF rect;
    private StaticLayout labelLayout;

    private int width;
    private int height;

    private float textSize;
    private float labelTextSize;
    private float numberTextSize;
    private StaticLayout numberLayout;

    public NumberView(Context context)
    {
        super(context);
        this.textSize = getResources().getDimension(R.dimen.regularTextSize);
        init();
    }

    public NumberView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.textSize = getResources().getDimension(R.dimen.regularTextSize);

        this.label = DialogHelper.getAttribute(context, attrs, "label", "Number");
        this.number = DialogHelper.getIntAttribute(context, attrs, "number", 0);
        this.textSize = DialogHelper.getFloatAttribute(context, attrs, "textSize",
                getResources().getDimension(R.dimen.regularTextSize));

        this.color = ColorHelper.palette[7];
        init();
    }

    public void setColor(int color)
    {
        this.color = color;
        pText.setColor(color);
        postInvalidate();
    }

    public void setLabel(String label)
    {
        this.label = label;
        requestLayout();
        postInvalidate();
    }

    public void setNumber(int number)
    {
        this.number = number;
        requestLayout();
        postInvalidate();
    }

    public void setTextSize(float textSize)
    {
        this.textSize = textSize;
        requestLayout();
        postInvalidate();
    }

    private void init()
    {
        pText = new TextPaint();
        pText.setAntiAlias(true);
        pText.setTextAlign(Paint.Align.CENTER);

        rect = new RectF();
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        labelTextSize = textSize;
        labelMarginTop = textSize * 0.35f;
        numberTextSize = textSize * 2.85f;

        pText.setTextSize(numberTextSize);
        numberLayout = new StaticLayout(Integer.toString(number), pText, width,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false);

        int numberWidth = numberLayout.getWidth();
        int numberHeight = numberLayout.getHeight();

        pText.setTextSize(labelTextSize);
        labelLayout = new StaticLayout(label, pText, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f,
                false);
        int labelWidth = labelLayout.getWidth();
        int labelHeight = labelLayout.getHeight();

        width = Math.max(numberWidth, labelWidth);
        height = (int) (numberHeight + labelHeight + labelMarginTop);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        rect.set(0, 0, width, height);

        canvas.save();
        canvas.translate(rect.centerX(), 0);
        pText.setColor(color);
        pText.setTextSize(numberTextSize);
        numberLayout.draw(canvas);
        canvas.restore();

        canvas.save();
        pText.setColor(Color.GRAY);
        pText.setTextSize(labelTextSize);
        canvas.translate(rect.centerX(), numberLayout.getHeight() + labelMarginTop);
        labelLayout.draw(canvas);
        canvas.restore();
    }
}
