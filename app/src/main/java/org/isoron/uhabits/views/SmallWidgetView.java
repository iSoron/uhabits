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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import org.isoron.helpers.ColorHelper;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;

public class SmallWidgetView extends View
{
    private Paint pCircle;
    private Paint pText;

    private int primaryColor;
    private int grey;
    private int size;

    private String fa_check;
    private String fa_times;
    private String fa_full_star;
    private String fa_half_star;
    private String fa_empty_star;

    private int check_status;
    private int star_status;

    private Rect textBounds;

    public SmallWidgetView(Context context)
    {
        super(context);
        init(context);
    }

    public SmallWidgetView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    private void init(Context context)
    {
        Typeface fontawesome =
                Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");

        pCircle = new Paint();
        pCircle.setAntiAlias(true);

        pText = new Paint();
        pText.setAntiAlias(true);
        pText.setTypeface(fontawesome);
        pText.setTextAlign(Paint.Align.CENTER);

        fa_check = context.getString(R.string.fa_check);
        fa_times = context.getString(R.string.fa_times);
        fa_empty_star = context.getString(R.string.fa_star_o);
        fa_half_star = context.getString(R.string.fa_star_half_o);
        fa_full_star = context.getString(R.string.fa_star);

        primaryColor = ColorHelper.palette[10];
        grey = Color.rgb(175, 175, 175);

        textBounds = new Rect();
        check_status = 0;
        star_status = 0;
    }

    public void setHabit(Habit habit)
    {
        this.check_status = habit.getCurrentCheckmarkStatus();
        this.star_status = habit.getCurrentStarStatus();
        this.primaryColor = habit.color;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        int s = size - (int) (size * 0.025);
        pCircle.setShadowLayer(size * 0.025f, size * 0.01f, size * 0.01f, 0x60000000);

        drawBigCircle(canvas, s);
        drawSmallCircle(canvas, s);
    }

    private void drawSmallCircle(Canvas canvas, int s)
    {
        String text;
        int color = (star_status == 2 ? primaryColor : grey);

        if(star_status == 0)
            text = fa_empty_star;
        else if(star_status == 1)
            text = fa_half_star;
        else
            text = fa_full_star;

        int r2 = (int) (s * 0.20);
        pCircle.setColor(Color.WHITE);
        canvas.drawCircle(s - r2, s - r2, r2, pCircle);

        pText.setTextSize(s * 0.3f);
        pText.setColor(color);
        pText.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, s - r2, s - r2 - textBounds.exactCenterY() - s / 90, pText);
    }

    private void drawBigCircle(Canvas canvas, int s)
    {
        String text = (check_status == 0 ? fa_times : fa_check);
        int color = (check_status == 2 ? primaryColor : grey);

        int r1 = (int) (s * 0.45);
        pCircle.setColor(color);
        canvas.drawCircle(r1, r1, r1, pCircle);

        pText.setTextSize(s * 0.7f);
        pText.setColor(Color.WHITE);
        pText.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, r1, r1 - textBounds.exactCenterY(), pText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        size = Math.min(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
