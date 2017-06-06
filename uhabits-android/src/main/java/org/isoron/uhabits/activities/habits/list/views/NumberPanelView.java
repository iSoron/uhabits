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

package org.isoron.uhabits.activities.habits.list.views;

import android.content.*;
import android.support.annotation.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.utils.*;

import java.util.*;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static org.isoron.androidbase.utils.InterfaceUtils.getDimension;
import static org.isoron.uhabits.utils.AttributeSetUtils.getAttribute;
import static org.isoron.uhabits.utils.AttributeSetUtils.getIntAttribute;
import static org.isoron.uhabits.utils.PaletteUtils.getAndroidTestColor;

public class NumberPanelView extends LinearLayout
    implements Preferences.Listener
{
    private static final int LEFT_TO_RIGHT = 0;

    private static final int RIGHT_TO_LEFT = 1;

    @Nullable
    private Preferences prefs;

    private double values[];

    private double threshold;

    private int nButtons;

    private int color;

    private String unit;

    private int dataOffset;

    @NonNull
    private OnInvalidEditListener onInvalidEditListener;

    private OnEditListener onEditListener;

    public NumberPanelView(Context context)
    {
        super(context);
        init();
    }

    public NumberPanelView(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        init();

        if (ctx != null && attrs != null)
        {
            int paletteColor = getIntAttribute(ctx, attrs, "color", 0);
            setColor(getAndroidTestColor(paletteColor));
            setButtonCount(getIntAttribute(ctx, attrs, "button_count", 5));
            setThreshold(getIntAttribute(ctx, attrs, "threshold", 1));
            setUnit(getAttribute(ctx, attrs, "unit", "min"));
        }

        if (isInEditMode()) initEditMode();
    }

    public NumberButtonView indexToButton(int i)
    {
        int position = i;
        if (getCheckmarkOrder() == RIGHT_TO_LEFT) position = nButtons - i - 1;
        return (NumberButtonView) getChildAt(position);
    }

    public void initEditMode()
    {
        double values[] = new double[nButtons];
        for (int i = 0; i < nButtons; i++)
            values[i] = new Random().nextDouble() * (threshold * 3);
        setValues(values);
    }

    @Override
    public void onCheckmarkOrderChanged()
    {
        setupButtons();
    }

    public void setButtonCount(int newButtonCount)
    {
        if (nButtons != newButtonCount)
        {
            nButtons = newButtonCount;
            addButtons();
        }

        setupButtons();
    }

    public void setColor(int color)
    {
        this.color = color;
        setupButtons();
    }

    public void setDataOffset(int dataOffset)
    {
        this.dataOffset = dataOffset;
        setupButtons();
    }

    public void setOnInvalidEditListener(
        @NonNull OnInvalidEditListener onInvalidEditListener)
    {
        this.onInvalidEditListener = onInvalidEditListener;
    }

    public void setOnEditListener(OnEditListener onEditListener)
    {
        this.onEditListener = onEditListener;
    }

    public void setThreshold(double threshold)
    {
        this.threshold = threshold;
        setupButtons();
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
        setupButtons();
    }

    public void setValues(double[] values)
    {
        this.values = values;
        setupButtons();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (prefs != null) prefs.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (prefs != null) prefs.removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec)
    {
        Context context = getContext();
        float buttonWidth = getDimension(context, R.dimen.checkmarkWidth);
        float buttonHeight = getDimension(context, R.dimen.checkmarkHeight);

        float width = buttonWidth * nButtons;

        widthSpec = makeMeasureSpec((int) width, EXACTLY);
        heightSpec = makeMeasureSpec((int) buttonHeight, EXACTLY);

        super.onMeasure(widthSpec, heightSpec);
    }

    private void addButtons()
    {
        removeAllViews();
        for (int i = 0; i < nButtons; i++)
            addView(new NumberButtonView(getContext()));
    }

    private int getCheckmarkOrder()
    {
        if (prefs == null) return LEFT_TO_RIGHT;
        return prefs.shouldReverseCheckmarks() ? RIGHT_TO_LEFT : LEFT_TO_RIGHT;
    }

    private void init()
    {
        Context appContext = getContext().getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
        }

        setWillNotDraw(false);
        values = new double[0];

        onInvalidEditListener = () -> {};
        onEditListener = (t) -> {};
    }

    private void setupButtonControllers(long timestamp,
                                        NumberButtonView buttonView)
    {
        buttonView.setOnEditListener(
            () -> onEditListener.onEdit(timestamp));

        buttonView.setOnInvalidEditListener(
            () -> onInvalidEditListener.onInvalidEdit());
    }

    private void setupButtons()
    {
        long timestamp = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;
        timestamp -= day * dataOffset;

        for (int i = 0; i < nButtons; i++)
        {
            NumberButtonView buttonView = indexToButton(i);
            if (i + dataOffset >= values.length) break;
            buttonView.setValue(values[i + dataOffset]);
            buttonView.setColor(color);
            buttonView.setThreshold(threshold);
            buttonView.setUnit(unit);
            setupButtonControllers(timestamp, buttonView);
            timestamp -= day;
        }
    }

    public interface OnInvalidEditListener
    {
        void onInvalidEdit();
    }

    public interface OnEditListener
    {
        void onEdit(long timestamp);
    }
}
