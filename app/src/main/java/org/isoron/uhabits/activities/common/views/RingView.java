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
import android.support.annotation.*;
import android.text.*;
import android.util.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;

import static org.isoron.uhabits.utils.AttributeSetUtils.*;
import static org.isoron.uhabits.utils.InterfaceUtils.*;

public class RingView extends View
{
    public static final PorterDuffXfermode XFERMODE_CLEAR =
        new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private int color;

    private float precision;

    private float percentage;

    private int diameter;

    private float thickness;

    private RectF rect;

    private TextPaint pRing;

    private Integer backgroundColor;

    private Integer inactiveColor;

    private float em;

    private String text;

    private float textSize;

    private boolean enableFontAwesome;

    @Nullable
    private Bitmap drawingCache;

    private Canvas cacheCanvas;

    private boolean isTransparencyEnabled;

    public RingView(Context context)
    {
        super(context);

        percentage = 0.0f;
        precision = 0.01f;
        color = ColorUtils.getAndroidTestColor(0);
        thickness = dpToPixels(getContext(), 2);
        text = "";
        textSize = context.getResources().getDimension(R.dimen.smallTextSize);

        init();
    }

    public RingView(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);

        percentage = getFloatAttribute(ctx, attrs, "percentage", 0);
        precision = getFloatAttribute(ctx, attrs, "precision", 0.01f);

        color = getColorAttribute(ctx, attrs, "color", 0);
        backgroundColor = getColorAttribute(ctx, attrs, "backgroundColor", null);
        inactiveColor = getColorAttribute(ctx, attrs, "inactiveColor", null);

        thickness = getFloatAttribute(ctx, attrs, "thickness", 0);
        thickness = dpToPixels(ctx, thickness);

        float defaultTextSize =
            ctx.getResources().getDimension(R.dimen.smallTextSize);
        textSize = getFloatAttribute(ctx, attrs, "textSize", defaultTextSize);
        textSize = spToPixels(ctx, textSize);
        text = AttributeSetUtils.getAttribute(ctx, attrs, "text", "");

        enableFontAwesome = AttributeSetUtils.getBooleanAttribute(ctx, attrs,
            "enableFontAwesome", false);

        init();
    }

    @Override
    public void setBackgroundColor(int backgroundColor)
    {
        this.backgroundColor = backgroundColor;
        postInvalidate();
    }

    public void setColor(int color)
    {
        this.color = color;
        postInvalidate();
    }

    public void setIsTransparencyEnabled(boolean isTransparencyEnabled)
    {
        this.isTransparencyEnabled = isTransparencyEnabled;
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

    public void setText(String text)
    {
        this.text = text;
        postInvalidate();
    }

    public void setTextSize(float textSize)
    {
        this.textSize = textSize;
    }

    public void setThickness(float thickness)
    {
        this.thickness = thickness;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Canvas activeCanvas;

        if (isTransparencyEnabled)
        {
            if (drawingCache == null) reallocateCache();
            activeCanvas = cacheCanvas;
            drawingCache.eraseColor(Color.TRANSPARENT);
        }
        else
        {
            activeCanvas = canvas;
        }

        pRing.setColor(color);
        rect.set(0, 0, diameter, diameter);

        float angle = 360 * Math.round(percentage / precision) * precision;

        activeCanvas.drawArc(rect, -90, angle, true, pRing);

        pRing.setColor(inactiveColor);
        activeCanvas.drawArc(rect, angle - 90, 360 - angle, true, pRing);

        if (thickness > 0)
        {
            if (isTransparencyEnabled) pRing.setXfermode(XFERMODE_CLEAR);
            else pRing.setColor(backgroundColor);

            rect.inset(thickness, thickness);
            activeCanvas.drawArc(rect, 0, 360, true, pRing);
            pRing.setXfermode(null);

            pRing.setColor(color);
            pRing.setTextSize(textSize);
            if (enableFontAwesome)
                pRing.setTypeface(getFontAwesome(getContext()));
            activeCanvas.drawText(text, rect.centerX(),
                rect.centerY() + 0.4f * em, pRing);
        }

        if (activeCanvas != canvas) canvas.drawBitmap(drawingCache, 0, 0, null);
    }

    @Override
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        if (isTransparencyEnabled) reallocateCache();
    }

    private void init()
    {
        pRing = new TextPaint();
        pRing.setAntiAlias(true);
        pRing.setColor(color);
        pRing.setTextAlign(Paint.Align.CENTER);

        StyledResources res = new StyledResources(getContext());

        if (backgroundColor == null)
            backgroundColor = res.getColor(R.attr.cardBackgroundColor);

        if (inactiveColor == null)
            inactiveColor = res.getColor(R.attr.highContrastTextColor);

        inactiveColor = ColorUtils.setAlpha(inactiveColor, 0.1f);

        rect = new RectF();
    }

    private void reallocateCache()
    {
        if (drawingCache != null) drawingCache.recycle();
        drawingCache =
            Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas(drawingCache);
    }
}
