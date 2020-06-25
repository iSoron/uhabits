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
import android.util.*;
import android.view.*;

import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.habits.list.views.*;

import java.util.*;

import static android.view.View.MeasureSpec.*;
import static org.isoron.androidbase.utils.InterfaceUtils.*;

public class TargetChart extends View
{
    private Paint paint;
    private int baseSize;
    private int primaryColor;
    private int mediumContrastTextColor;
    private int highContrastReverseTextColor;
    private int lowContrastTextColor;
    private RectF rect = new RectF();
    private RectF barRect = new RectF();
    private List<Double> values = Collections.emptyList();
    private List<String> labels = Collections.emptyList();
    private List<Double> targets = Collections.emptyList();
    private float maxLabelSize;
    private float tinyTextSize;

    public TargetChart(Context context)
    {
        super(context);
        init();
    }

    public TargetChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public void populateWithRandomData()
    {
        labels = new ArrayList<>();
        values = new ArrayList<>();
        targets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            double percentage = new Random().nextDouble();
            targets.add(new Random().nextDouble() * 1000.0);
            values.add(targets.get(i) * percentage * 1.2);
            labels.add(String.format(Locale.US, "Label %d", i + 1));
        }
    }

    public void setColor(int color)
    {
        this.primaryColor = color;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (labels.size() == 0) return;

        maxLabelSize = 0;
        for (String label : labels) {
            paint.setTextSize(tinyTextSize);
            float len = paint.measureText(label);
            maxLabelSize = Math.max(maxLabelSize, len);
        }

        rect.set(0, 0, getWidth(), baseSize);
        for (int i = 0; i < labels.size(); i++) {
            drawRow(canvas, i, rect);
            rect.offset(0, baseSize);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec)
    {
        int width = getSize(widthSpec);
        int height = labels.size() * baseSize;
        heightSpec = makeMeasureSpec(height, EXACTLY);
        widthSpec = makeMeasureSpec(width, EXACTLY);
        setMeasuredDimension(widthSpec, heightSpec);
    }

    private void drawRow(Canvas canvas, int row, RectF rect)
    {
        float padding = dpToPixels(getContext(), 4);
        float round = dpToPixels(getContext(), 2);
        float stop = maxLabelSize + padding * 2;

        paint.setColor(mediumContrastTextColor);

        // Draw label
        paint.setTextSize(tinyTextSize);
        paint.setTextAlign(Paint.Align.RIGHT);
        float yTextAdjust = (paint.descent() + paint.ascent()) / 2.0f;
        canvas.drawText(labels.get(row),
                        rect.left + stop - padding,
                        rect.centerY() - yTextAdjust,
                        paint);

        // Draw background box
        paint.setColor(lowContrastTextColor);
        barRect.set(rect.left + stop + padding,
                    rect.top + baseSize * 0.05f,
                    rect.right - padding,
                    rect.bottom - baseSize * 0.05f);
        canvas.drawRoundRect(barRect, round, round, paint);

        float percentage = (float) (values.get(row) / targets.get(row));
        percentage = Math.min(1.0f, percentage);

        // Draw completed box
        float completedWidth = percentage * barRect.width();
        if (completedWidth > 0 && completedWidth < 2 * round) {
            completedWidth = 2 * round;
        }
        float remainingWidth = barRect.width() - completedWidth;

        paint.setColor(primaryColor);
        barRect.set(barRect.left,
                    barRect.top,
                    barRect.left + completedWidth,
                    barRect.bottom);
        canvas.drawRoundRect(barRect, round, round, paint);

        // Draw values
        paint.setColor(Color.WHITE);
        paint.setTextSize(tinyTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        yTextAdjust = (paint.descent() + paint.ascent()) / 2.0f;

        double remaining = targets.get(row) - values.get(row);
        String completedText = NumberButtonViewKt.toShortString(values.get(row));
        String remainingText = NumberButtonViewKt.toShortString(remaining);

        if (completedWidth > paint.measureText(completedText) + 2 * padding) {
            paint.setColor(highContrastReverseTextColor);
            canvas.drawText(completedText,
                            barRect.centerX(),
                            barRect.centerY() - yTextAdjust,
                            paint);
        }

        if (remainingWidth > paint.measureText(remainingText) + 2 * padding) {
            paint.setColor(mediumContrastTextColor);
            barRect.set(rect.left + stop + padding + completedWidth,
                        barRect.top,
                        rect.right - padding,
                        barRect.bottom);
            canvas.drawText(remainingText,
                            barRect.centerX(),
                            barRect.centerY() - yTextAdjust,
                            paint);
        }
    }

    private void init()
    {
        paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);

        StyledResources res = new StyledResources(getContext());
        lowContrastTextColor = res.getColor(R.attr.lowContrastTextColor);
        mediumContrastTextColor = res.getColor(R.attr.mediumContrastTextColor);
        highContrastReverseTextColor = res.getColor(R.attr.highContrastReverseTextColor);
        tinyTextSize = getDimension(getContext(), R.dimen.tinyTextSize);
        baseSize = getResources().getDimensionPixelSize(R.dimen.baseSize);
    }

    public void setValues(List<Double> values)
    {
        this.values = values;
        requestLayout();
    }

    public void setLabels(List<String> labels)
    {
        this.labels = labels;
        requestLayout();
    }

    public void setTargets(List<Double> targets)
    {
        this.targets = targets;
        requestLayout();
    }
}
