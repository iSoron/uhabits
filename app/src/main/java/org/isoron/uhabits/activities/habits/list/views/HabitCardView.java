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
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.*;

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

    @BindView(R.id.checkmarkPanel)
    CheckmarkPanelView checkmarkPanel;

    @BindView(R.id.innerFrame)
    LinearLayout innerFrame;

    @BindView(R.id.label)
    TextView label;

    @BindView(R.id.scoreRing)
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

    public void setCheckmarkCount(int checkmarkCount)
    {
        checkmarkPanel.setButtonCount(checkmarkCount);
    }

    public void setCheckmarkValues(int checkmarks[])
    {
        checkmarkPanel.setCheckmarkValues(checkmarks);
        postInvalidate();
    }

    public void setController(Controller controller)
    {
        checkmarkPanel.setController(null);
        if (controller == null) return;
        checkmarkPanel.setController(controller);
    }

    public void setDataOffset(int dataOffset)
    {
        this.dataOffset = dataOffset;
        checkmarkPanel.setDataOffset(dataOffset);
    }

    public void setHabit(@NonNull Habit habit)
    {
        if (this.habit != null) detachFromHabit();

        this.habit = habit;
        checkmarkPanel.setHabit(habit);
        refresh();

        attachToHabit();
        postInvalidate();
    }

    public void setScore(int score)
    {
        float percentage = (float) score / Score.MAX_VALUE;
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
    protected void onDetachedFromWindow()
    {
        if (habit != null) detachFromHabit();
        super.onDetachedFromWindow();
    }

    private void attachToHabit()
    {
        if (habit != null) habit.getObservable().addListener(this);
    }

    private void detachFromHabit()
    {
        if (habit != null) habit.getObservable().removeListener(this);
    }

    private int getActiveColor(Habit habit)
    {
        int mediumContrastColor = res.getColor(R.attr.mediumContrastTextColor);
        int activeColor = ColorUtils.getColor(context, habit.getColor());
        if (habit.isArchived()) activeColor = mediumContrastColor;

        return activeColor;
    }

    private void init()
    {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT));

        res = new StyledResources(getContext());

        inflate(context, R.layout.list_habits_card, this);
        ButterKnife.bind(this);

        innerFrame.setOnTouchListener((v, event) -> {
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
        int color = ColorUtils.getAndroidTestColor(rand.nextInt(10));
        int[] values = new int[5];
        for (int i = 0; i < 5; i++) values[i] = rand.nextInt(3);

        label.setText(EDIT_MODE_HABITS[rand.nextInt(EDIT_MODE_HABITS.length)]);
        label.setTextColor(color);
        scoreRing.setColor(color);
        scoreRing.setPercentage(rand.nextFloat());
        checkmarkPanel.setColor(color);
        checkmarkPanel.setCheckmarkValues(values);
    }

    private void refresh()
    {
        int color = getActiveColor(habit);
        label.setText(habit.getName());
        label.setTextColor(color);
        scoreRing.setColor(color);
        checkmarkPanel.setColor(color);
        postInvalidate();
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

    public interface Controller extends CheckmarkPanelView.Controller {}
}
