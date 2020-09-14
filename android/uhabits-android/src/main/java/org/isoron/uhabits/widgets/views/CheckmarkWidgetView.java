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

package org.isoron.uhabits.widgets.views;


import android.content.*;
import android.util.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.habits.list.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.utils.*;

import static org.isoron.androidbase.utils.InterfaceUtils.getDimension;




public class CheckmarkWidgetView extends HabitWidgetView {
    protected int activeColor;

    protected float percentage;

    @Nullable
    protected String name;

    protected RingView ring;

    protected TextView label;

    protected int checkmarkValue;

    protected CheckmarkState checkmarkState;

    protected boolean isNumerical;


    public CheckmarkWidgetView(Context context) {
        super(context);
        init();
    }


    public CheckmarkWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public void refresh() {
        if (backgroundPaint == null || frame == null || ring == null) {
            return;
        }

        StyledResources res = new StyledResources(getContext());

        int bgColor;
        int fgColor;

        if (checkmarkState.isManualInput()) {
            bgColor = activeColor;
            fgColor = res.getColor(R.attr.highContrastReverseTextColor);
            setShadowAlpha(0x4f);
            backgroundPaint.setColor(bgColor);
            frame.setBackgroundDrawable(background);
        } else {
            bgColor = res.getColor(R.attr.cardBgColor);
            fgColor = res.getColor(R.attr.mediumContrastTextColor);
            setShadowAlpha(0x00);
        }

        ring.setPercentage(percentage);
        ring.setColor(fgColor);
        ring.setBackgroundColor(bgColor);
        ring.setText(


            getText());

        label.setText(name);
        label.setTextColor(fgColor);


        requestLayout();


        postInvalidate();

    }


    public void setCheckmarkState(CheckmarkState checkmarkState) {
        this.checkmarkState = checkmarkState;
    }


    protected String getText() {
        if (isNumerical) {
            return NumberButtonViewKt.toShortString(checkmarkState.getValue() / 1000.0);
        }
        switch (checkmarkState.getValue()) {
            case Checkmark.YES:
                return getResources().getString(R.string.fa_check);
            case Checkmark.SKIP:
                return getResources().getString(R.string.fa_skipped);
            case Checkmark.NO:
            default:
                return getResources().getString(R.string.fa_times);
        }
    }


    public void setActiveColor(int activeColor) {
        this.activeColor = activeColor;
    }


    public void setName(@NonNull String name) {
        this.name = name;
    }


    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }


    public void setNumerical(boolean isNumerical) {
        this.isNumerical = isNumerical;
    }


    @Override
    @NonNull
    protected Integer getInnerLayoutId() {
        return R.layout.widget_checkmark;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        float w = width;
        float h = width * 1.25f;
        float scale = Math.min(width / w, height / h);

        w *= scale;
        h *= scale;

        if (h < getDimension(getContext(), R.dimen.checkmarkWidget_heightBreakpoint)) {
            ring.setVisibility(GONE);
        } else {
            ring.setVisibility(VISIBLE);
        }

        widthMeasureSpec =
            MeasureSpec.makeMeasureSpec((int) w, MeasureSpec.EXACTLY);
        heightMeasureSpec =
            MeasureSpec.makeMeasureSpec((int) h, MeasureSpec.EXACTLY);

        float textSize = 0.15f * h;
        float maxTextSize = getDimension(getContext(), R.dimen.smallerTextSize);
        textSize = Math.min(textSize, maxTextSize);

        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        ring.setTextSize(textSize);
        ring.setThickness(0.15f * textSize);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private void init() {
        ring = (RingView) findViewById(R.id.scoreRing);
        label = (TextView) findViewById(R.id.label);

        if (ring != null) {
            ring.setIsTransparencyEnabled(true);
        }

        if (isInEditMode()) {
            percentage = 0.75f;
            name = "Wake up early";
            activeColor = PaletteUtils.getAndroidTestColor(6);
            checkmarkState = new CheckmarkState(Checkmark.YES, true);
            refresh();
        }
    }
}
