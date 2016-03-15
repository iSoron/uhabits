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

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DialogHelper;

public class RingView extends View
{
    private int color;
    private float percentage;
    private float labelMarginTop;
    private TextPaint pRing;
    private String label;
    private RectF rect;
    private StaticLayout labelLayout;

    private int width;
    private int height;
    private float diameter;
    private float maxDiameter;
    private float textSize;

    public RingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.label = DialogHelper.getAttribute(context, attrs, "label");
        this.maxDiameter = DialogHelper.getFloatAttribute(context, attrs, "maxDiameter");
        this.textSize = DialogHelper.getFloatAttribute(context, attrs, "textSize");

        this.maxDiameter = DialogHelper.dpToPixels(context, maxDiameter);
        this.textSize = DialogHelper.spToPixels(context, textSize);
        this.color = ColorHelper.palette[7];
        this.percentage = 0.75f;
        init();
    }

    public void setColor(int color)
    {
        this.color = color;
        pRing.setColor(color);
        postInvalidate();
    }

    public void setPercentage(float percentage)
    {
        this.percentage = percentage;
        postInvalidate();
    }

    private void init()
    {
        pRing = new TextPaint();
        pRing.setAntiAlias(true);
        pRing.setColor(color);
        pRing.setTextAlign(Paint.Align.CENTER);

        rect = new RectF();
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        diameter = Math.min(maxDiameter, width);

        pRing.setTextSize(textSize);
        labelMarginTop = textSize * 0.80f;
        labelLayout = new StaticLayout(label, pRing, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f,
                false);

        width = Math.max(width, labelLayout.getWidth());
        height = (int) (diameter + labelLayout.getHeight() + labelMarginTop);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        float thickness = diameter * 0.15f;

        pRing.setColor(color);
        rect.set(0, 0, diameter, diameter);
        rect.offset((width - diameter) / 2, 0);
        canvas.drawArc(rect, -90, 360 * percentage, true, pRing);

        pRing.setColor(Color.rgb(230, 230, 230));
        canvas.drawArc(rect, 360 * percentage - 90 + 2, 360 * (1 - percentage) - 4, true, pRing);

        pRing.setColor(Color.WHITE);
        rect.inset(thickness, thickness);
        canvas.drawArc(rect, -90, 360, true, pRing);

        pRing.setColor(Color.GRAY);
        pRing.setTextSize(diameter * 0.2f);
        float lineHeight = pRing.getFontSpacing();
        canvas.drawText(String.format("%.0f%%", percentage * 100), rect.centerX(),
                rect.centerY() + lineHeight / 3, pRing);

        pRing.setTextSize(textSize);
        canvas.translate(width / 2, diameter + labelMarginTop);
        labelLayout.draw(canvas);
    }
}
