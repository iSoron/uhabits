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
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.utils.*;

import static org.isoron.androidbase.utils.InterfaceUtils.getDimension;
import static org.isoron.uhabits.activities.habits.list.views.NumberButtonViewKt.*;

public class CheckmarkWidgetView extends HabitWidgetView {
    protected int activeColor;

    protected float percentage;

    @Nullable
    protected String name;

    protected RingView ring;

    protected TextView label;

    protected int checkmarkValue;

    protected int checkmarkState;

    protected boolean isNumerical;

    public boolean isNumerical() {
        return isNumerical;
    }

    public void setNumerical(boolean numerical) {
        isNumerical = numerical;
    }

    public CheckmarkWidgetView(Context context) {
        super(context);
        init();
    }

    public CheckmarkWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void refresh() {
        if (backgroundPaint == null || frame == null || ring == null) return;

        StyledResources res = new StyledResources(getContext());

        String text;
        int bgColor;
        int fgColor;

        switch (getCheckmarkState()) {
            case Checkmark.CHECKED_EXPLICITLY:
                bgColor = activeColor;
                fgColor = res.getColor(R.attr.highContrastReverseTextColor);
                setShadowAlpha(0x4f);
                backgroundPaint.setColor(bgColor);
                frame.setBackgroundDrawable(background);
                break;

            case Checkmark.CHECKED_IMPLICITLY:
                text = getResources().getString(R.string.fa_check);
                bgColor = res.getColor(R.attr.cardBgColor);
                fgColor = res.getColor(R.attr.mediumContrastTextColor);
                setShadowAlpha(0x00);
                break;

            case Checkmark.UNCHECKED:
            default:
                text = getResources().getString(R.string.fa_times);
                bgColor = res.getColor(R.attr.cardBgColor);
                fgColor = res.getColor(R.attr.mediumContrastTextColor);
                setShadowAlpha(0x00);
                break;
        }

        ring.setPercentage(percentage);
        ring.setColor(fgColor);
        ring.setBackgroundColor(bgColor);
        ring.setText(getText());

        label.setText(name);
        label.setTextColor(fgColor);

        requestLayout();
        postInvalidate();
    }

    /**
     * @Return the state of the checkmark, either:
     * - Checkmark.CHECKED_EXPLICITLY
     * - Checkmark.CHECKED_IMPLICITLY
     * - Checkmark.UNCHECKED
     */
    public int getCheckmarkState() {
        return checkmarkState;
    }

    /**
     * @brief set the state of the checkmark to either:
     * - Checkmark.CHECKED_EXPLICITLY
     * - Checkmark.CHECKED_IMPLICITLY
     * - Checkmark.UNCHECKED
     */
    public void setCheckmarkState(int checkmarkState) {
        this.checkmarkState = checkmarkState;
    }


    /**
     * @Return the text that should be displayed in the middle of the widget
     */
    protected String getText() {
        if (isNumerical) {
            return doubleToShortString(Checkmark.checkMarkValueToDouble(checkmarkValue));
        } else {
            switch (getCheckmarkState()) {
                case Checkmark.CHECKED_EXPLICITLY:
                    return getResources().getString(R.string.fa_check);

                case Checkmark.CHECKED_IMPLICITLY:
                    return getResources().getString(R.string.fa_check);

                case Checkmark.UNCHECKED:
                default:
                    return getResources().getString(R.string.fa_times);
            }
        }
    }

    public void setActiveColor(int activeColor) {
        this.activeColor = activeColor;
    }

    public void setCheckmarkValue(int checkmarkValue) {
        this.checkmarkValue = checkmarkValue;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
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

        if (h < getDimension(getContext(), R.dimen.checkmarkWidget_heightBreakpoint))
            ring.setVisibility(GONE);
        else
            ring.setVisibility(VISIBLE);

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

        if (ring != null) ring.setIsTransparencyEnabled(true);

        if (isInEditMode()) {
            percentage = 0.75f;
            name = "Wake up early";
            activeColor = PaletteUtils.getAndroidTestColor(6);
            checkmarkValue = Checkmark.CHECKED_EXPLICITLY;
            refresh();
        }
    }
}
