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

import android.annotation.*;
import android.content.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.annotation.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.*;
import static android.view.ViewGroup.LayoutParams.*;
import static org.isoron.androidbase.utils.InterfaceUtils.*;

public class HabitCardView extends FrameLayout
    implements ModelObservable.Listener
{

    private static final String EDIT_MODE_HABITS[] = {
        "Wake up early",
        "Wash dishes",
        "Exercise",
        "Meditate",
        "Play guitar",
        "Wash clothes",
        "Get a haircut"
    };

    CheckmarkPanelView checkmarkPanel;

    NumberPanelView numberPanel;

    LinearLayout innerFrame;

    TextView label;

    RingView scoreRing;

    private final Context context = getContext();

    private StyledResources res;

    @Nullable
    private Habit habit;

    private int dataOffset;

    public HabitCardView(Context context)
    {
        super(context);
        init();
    }

    public HabitCardView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @Override
    public void onModelChange()
    {
        new Handler(Looper.getMainLooper()).post(() -> refresh());
    }

    public void setButtonCount(int buttonCount)
    {
        checkmarkPanel.setButtonCount(buttonCount);
        numberPanel.setButtonCount(buttonCount);
    }

    public void setController(Controller controller)
    {
        checkmarkPanel.setController(null);
        numberPanel.setController(null);
        if (controller == null) return;
        checkmarkPanel.setController(controller);
        numberPanel.setController(controller);
    }

    public void setDataOffset(int dataOffset)
    {
        this.dataOffset = dataOffset;
        checkmarkPanel.setDataOffset(dataOffset);
        numberPanel.setDataOffset(dataOffset);
    }

    public void setHabit(@NonNull Habit habit)
    {
        if (isAttachedToWindow()) stopListening();

        this.habit = habit;
        checkmarkPanel.setHabit(habit);
        numberPanel.setHabit(habit);

        refresh();
        if (isAttachedToWindow()) startListening();
        postInvalidate();
    }

    public void setScore(double score)
    {
        float percentage = (float) score;
        scoreRing.setPercentage(percentage);
        scoreRing.setPrecision(1.0f / 16);
        postInvalidate();
    }

    @Override
    public void setSelected(boolean isSelected)
    {
        super.setSelected(isSelected);
        updateBackground(isSelected);
    }

    public void setThreshold(double threshold)
    {
        numberPanel.setThreshold(threshold);
    }

    public void setUnit(String unit)
    {
        numberPanel.setUnit(unit);
    }

    public void setValues(int values[])
    {
        double dvalues[] = new double[values.length];
        for (int i = 0; i < values.length; i++)
            dvalues[i] = (double) values[i] / 1000;

        checkmarkPanel.setValues(values);
        numberPanel.setValues(dvalues);
        numberPanel.setThreshold(10);
        postInvalidate();
    }

    public void triggerRipple(long timestamp)
    {
        long today = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;
        int offset = (int) ((today - timestamp) / day) - dataOffset;
        CheckmarkButtonView button = checkmarkPanel.indexToButton(offset);

        float y = button.getHeight() / 2.0f;
        float x = checkmarkPanel.getX() + button.getX() + button.getWidth() / 2;
        triggerRipple(x, y);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        stopListening();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        startListening();
        super.onDetachedFromWindow();
    }

    private int getActiveColor(Habit habit)
    {
        int mediumContrastColor = res.getColor(R.attr.mediumContrastTextColor);
        int activeColor = PaletteUtils.getColor(context, habit.getColor());
        if (habit.isArchived()) activeColor = mediumContrastColor;

        return activeColor;
    }

    private void init()
    {
        res = new StyledResources(getContext());
        setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        setClipToPadding(false);

        int margin = (int) dpToPixels(context, 3);
        setPadding(margin, 0, margin, margin);

        initInnerFrame();
        initScoreRing();
        initLabel();

        checkmarkPanel = new CheckmarkPanelView(context);
        numberPanel = new NumberPanelView(context);
        numberPanel.setVisibility(GONE);

        innerFrame.addView(scoreRing);
        innerFrame.addView(label);
        innerFrame.addView(checkmarkPanel);
        innerFrame.addView(numberPanel);
        addView(innerFrame);

        innerFrame.setOnTouchListener((v, event) ->
        {
            if (SDK_INT >= LOLLIPOP)
                v.getBackground().setHotspot(event.getX(), event.getY());
            return false;
        });

        if (isInEditMode()) initEditMode();
    }

    @SuppressLint("SetTextI18n")
    private void initEditMode()
    {
        Random rand = new Random();
        int color = PaletteUtils.getAndroidTestColor(rand.nextInt(10));
        label.setText(EDIT_MODE_HABITS[rand.nextInt(EDIT_MODE_HABITS.length)]);
        label.setTextColor(color);
        scoreRing.setColor(color);
        scoreRing.setPercentage(rand.nextFloat());
        checkmarkPanel.setColor(color);
        numberPanel.setColor(color);
        checkmarkPanel.setButtonCount(5);
    }

    private void initInnerFrame()
    {
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);

        innerFrame = new LinearLayout(context);
        innerFrame.setLayoutParams(params);
        innerFrame.setOrientation(LinearLayout.HORIZONTAL);
        innerFrame.setGravity(Gravity.CENTER_VERTICAL);

        if (SDK_INT >= LOLLIPOP)
            innerFrame.setElevation(dpToPixels(context, 1));
    }

    private void initLabel()
    {
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1);

        label = new TextView(context);
        label.setLayoutParams(params);
        label.setMaxLines(2);
        label.setEllipsize(TextUtils.TruncateAt.END);

        if (SDK_INT >= M)
            label.setBreakStrategy(Layout.BREAK_STRATEGY_BALANCED);
    }

    private void initScoreRing()
    {
        scoreRing = new RingView(context);
        int ringSize = (int) dpToPixels(context, 15);
        int margin = (int) dpToPixels(context, 8);
        float thickness = dpToPixels(context, 3);

        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(ringSize, ringSize);
        params.setMargins(margin, 0, margin, 0);
        params.gravity = Gravity.CENTER;

        scoreRing.setLayoutParams(params);
        scoreRing.setThickness(thickness);
    }

    private void refresh()
    {
        int color = getActiveColor(habit);
        label.setText(habit.getName());
        label.setTextColor(color);
        scoreRing.setColor(color);
        checkmarkPanel.setColor(color);
        numberPanel.setColor(color);

        boolean isNumerical = habit.isNumerical();
        checkmarkPanel.setVisibility(isNumerical ? GONE : VISIBLE);
        numberPanel.setVisibility(isNumerical ? VISIBLE : GONE);

        postInvalidate();
    }

    private void startListening()
    {
        if (habit != null) habit.getObservable().removeListener(this);
    }

    private void stopListening()
    {
        if (habit != null) habit.getObservable().addListener(this);
    }

    private void triggerRipple(final float x, final float y)
    {
        final Drawable background = innerFrame.getBackground();
        if (SDK_INT >= LOLLIPOP) background.setHotspot(x, y);
        background.setState(new int[]{
            android.R.attr.state_pressed, android.R.attr.state_enabled
        });
        new Handler().postDelayed(() -> background.setState(new int[]{}), 25);
    }

    private void updateBackground(boolean isSelected)
    {
        if (SDK_INT >= LOLLIPOP)
        {
            if (isSelected)
                innerFrame.setBackgroundResource(R.drawable.selected_box);
            else innerFrame.setBackgroundResource(R.drawable.ripple);
        }
        else
        {
            Drawable background;
            if (isSelected)
                background = res.getDrawable(R.attr.selectedBackground);
            else background = res.getDrawable(R.attr.cardBackground);
            innerFrame.setBackgroundDrawable(background);
        }
    }

    public interface Controller
        extends CheckmarkPanelView.Controller, NumberPanelView.Controller
    {

    }
}
