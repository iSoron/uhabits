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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.models.Habit;

public class CheckmarkView extends View implements HabitDataView
{
    private Paint pCard;
    private Paint pIcon;

    private int primaryColor;
    private int timesColor;
    private int darkGrey;

    private int width;
    private int height;
    private float leftMargin;
    private float topMargin;
    private float padding;
    private String label;

    private String fa_check;
    private String fa_times;

    private int check_status;

    private Rect rect;
    private TextPaint textPaint;
    private StaticLayout labelLayout;
    private Habit habit;

    public CheckmarkView(Context context)
    {
        super(context);
        init(context);
    }

    public CheckmarkView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    private void init(Context context)
    {
        Typeface fontawesome =
                Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");

        pCard = new Paint();
        pCard.setAntiAlias(true);

        pIcon = new Paint();
        pIcon.setAntiAlias(true);
        pIcon.setTypeface(fontawesome);
        pIcon.setTextAlign(Paint.Align.CENTER);

        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);

        fa_check = context.getString(R.string.fa_check);
        fa_times = context.getString(R.string.fa_times);

        primaryColor = ColorHelper.palette[10];
        timesColor = Color.argb(128, 255, 255, 255);
        darkGrey = Color.argb(64, 0, 0, 0);

        rect = new Rect();
        check_status = 0;
        label = "Habit";
    }

    public void setHabit(Habit habit)
    {
        this.habit = habit;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        drawBackground(canvas);
        drawCheckmark(canvas);
        drawLabel(canvas);
    }

    private void drawBackground(Canvas canvas)
    {
        int color = (check_status == 2 ? primaryColor : darkGrey);

        pCard.setColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            canvas.drawRoundRect(leftMargin, topMargin, width - leftMargin, height - topMargin, padding,
                    padding, pCard);
        else
            canvas.drawRect(leftMargin, topMargin, width - leftMargin, height - topMargin, pCard);
    }

    private void drawCheckmark(Canvas canvas)
    {
        String text = (check_status == 0 ? fa_times : fa_check);
        int color = (check_status == 2 ? Color.WHITE : timesColor);

        pIcon.setColor(color);
        pIcon.setTextSize(width * 0.5f);
        pIcon.getTextBounds(text, 0, 1, rect);

        int y = (int) ((0.67f * height - rect.bottom - rect.top) / 2);
        canvas.drawText(text, width / 2, y, pIcon);
    }

    private void drawLabel(Canvas canvas)
    {
        canvas.save();
        float y;
        int nLines = labelLayout.getLineCount();

        if(nLines == 1)
            y = height * 0.8f - padding;
        else
            y = height * 0.7f - padding;

        canvas.translate(leftMargin + padding, y);

        labelLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, (int) (width * 1.25));
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        this.width = getMeasuredWidth();
        this.height = getMeasuredHeight();

        leftMargin = (width * 0.015f);
        topMargin =  (height * 0.015f);
        padding = 8 * leftMargin;
        textPaint.setTextSize(0.15f * width);

        updateLabel();
    }

    public void refreshData()
    {
        this.check_status = habit.checkmarks.getTodayValue();
        this.primaryColor = Color.argb(230, Color.red(habit.color), Color.green(habit.color),
                Color.blue(habit.color));
        this.label = habit.name;

        updateLabel();
        postInvalidate();
    }

    private void updateLabel()
    {
        textPaint.setColor(Color.WHITE);
        labelLayout = new StaticLayout(label, textPaint,
                (int) (width - 2 * leftMargin - 2 * padding),
                Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
    }
}
