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
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.UIHelper;

public class RingView extends View
{
    private float precision;
    private boolean enableFontAwesome;

    private int color;
    private float percentage;
    private TextPaint pRing;
    private RectF rect;

    private int diameter;

    private float textSize;

    private float thickness;

    private Integer backgroundColor;
    private Integer inactiveColor;
    private float em;
    private String text;

    public RingView(Context context)
    {
        super(context);
        init();
    }

    public RingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        percentage = UIHelper.getFloatAttribute(context, attrs, "percentage", 0);
        precision = UIHelper.getFloatAttribute(context, attrs, "precision", 0.01f);

        color = UIHelper.getColorAttribute(context, attrs, "color", 0);
        backgroundColor = UIHelper.getColorAttribute(context, attrs, "backgroundColor", null);
        inactiveColor = UIHelper.getColorAttribute(context, attrs, "inactiveColor", null);

        thickness = UIHelper.getFloatAttribute(context, attrs, "thickness", 0);
        thickness = UIHelper.dpToPixels(context, thickness);

        float defaultTextSize = context.getResources().getDimension(R.dimen.smallTextSize);
        textSize = UIHelper.getFloatAttribute(context, attrs, "textSize", defaultTextSize);
        textSize = UIHelper.spToPixels(context, textSize);

        text = UIHelper.getAttribute(context, attrs, "text", "");

        enableFontAwesome = UIHelper.getBooleanAttribute(context, attrs, "enableFontAwesome", false);

        init();
    }

    public void setColor(int color)
    {
        this.color = color;
        postInvalidate();
    }

    @Override
    public void setBackgroundColor(int backgroundColor)
    {
        this.backgroundColor = backgroundColor;
        postInvalidate();
    }

    public void setPercentage(float percentage)
    {
        this.percentage = percentage;
        postInvalidate();
    }

    public void setPrecision(float precision)
    {
        this.precision = precision;
        postInvalidate();
    }

    public void setThickness(float thickness)
    {
        this.thickness = thickness;
        postInvalidate();
    }

    public void setText(String text)
    {
        this.text = text;
        postInvalidate();
    }

    private void init()
    {
        pRing = new TextPaint();
        pRing.setAntiAlias(true);
        pRing.setColor(color);
        pRing.setTextAlign(Paint.Align.CENTER);

        if(backgroundColor == null)
            backgroundColor = UIHelper.getStyledColor(getContext(), R.attr.cardBackgroundColor);

        if(inactiveColor == null)
            inactiveColor = UIHelper.getStyledColor(getContext(), R.attr.highContrastTextColor);

        inactiveColor = ColorHelper.setAlpha(inactiveColor, 0.1f);

        rect = new RectF();
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        diameter = Math.min(height, width);

        pRing.setTextSize(textSize);
        em = pRing.measureText("M");

        setMeasuredDimension(diameter, diameter);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        pRing.setColor(color);
        rect.set(0, 0, diameter, diameter);

        float angle = 360 * Math.round(percentage / precision) * precision;

        canvas.drawArc(rect, -90, angle, true, pRing);

        pRing.setColor(inactiveColor);
        canvas.drawArc(rect, angle - 90, 360 - angle, true, pRing);

        if(thickness > 0)
        {
            pRing.setColor(backgroundColor);
            rect.inset(thickness, thickness);
            canvas.drawArc(rect, 0, 360, true, pRing);

            pRing.setColor(color);
            pRing.setTextSize(textSize);
            if(enableFontAwesome) pRing.setTypeface(UIHelper.getFontAwesome(getContext()));
            canvas.drawText(text, rect.centerX(), rect.centerY() + 0.4f * em, pRing);
        }
    }
}
