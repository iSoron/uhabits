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
import android.graphics.RectF;
import android.view.View;

public class RingView extends View
{

    private int size;
    private int color;
    private float perc;
    private Paint pRing;
    private float lineHeight;
    private String label;

    public RingView(Context context, int size, int color, float perc, String label)
    {
        super(context);
        this.size = size;
        this.color = color;
        this.perc = perc;

        pRing = new Paint();
        pRing.setColor(color);
        pRing.setAntiAlias(true);
        pRing.setTextAlign(Paint.Align.CENTER);

        this.label = label;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(size, size + (int) (2*lineHeight));
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        float thickness = size * 0.15f;

        pRing.setColor(color);
        RectF r = new RectF(0, 0, size, size);
        canvas.drawArc(r, -90, 360 * perc, true, pRing);

        pRing.setColor(Color.rgb(230, 230, 230));
        canvas.drawArc(r, 360 * perc - 90 + 2, 360 * (1 - perc) - 4, true, pRing);

        pRing.setColor(Color.WHITE);
        r.inset(thickness, thickness);
        canvas.drawArc(r, -90, 360, true, pRing);

        pRing.setColor(Color.GRAY);
        pRing.setTextSize(size * 0.2f);
        lineHeight = pRing.getFontSpacing();
        canvas.drawText(String.format("%.0f%%", perc * 100), r.centerX(), r.centerY()+lineHeight/3, pRing);

        pRing.setTextSize(size * 0.15f);
        canvas.drawText(label, size/2, size + lineHeight * 1.2f, pRing);
    }
}
