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

package org.isoron.uhabits.ui.habits.list.views;

import android.annotation.*;
import android.content.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.habits.list.*;
import org.isoron.uhabits.ui.habits.show.views.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

import static org.isoron.uhabits.utils.InterfaceUtils.*;

public class HabitCardView extends FrameLayout
{
    private Habit habit;

    @BindView(R.id.checkmarkPanel)
    CheckmarkPanelView checkmarkPanel;

    @BindView(R.id.innerFrame)
    LinearLayout innerFrame;

    @BindView(R.id.label)
    TextView label;

    @BindView(R.id.scoreRing)
    RingView scoreRing;

    private final Context context = getContext();

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

    public HabitCardView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCheckmarkValues(int checkmarks[])
    {
        int count = ListHabitsRootView.getCheckmarkCount(this);
        int visibleCheckmarks[] = Arrays.copyOfRange(checkmarks, 0, count);
        checkmarkPanel.setCheckmarkValues(visibleCheckmarks);
        postInvalidate();
    }

    public void setController(Controller controller)
    {
        checkmarkPanel.setController(null);
        if (controller == null) return;

        checkmarkPanel.setController(controller);
    }

    public void setHabit(Habit habit)
    {
        this.habit = habit;
        int color = getActiveColor(habit);

        label.setText(habit.getName());
        label.setTextColor(color);
        scoreRing.setColor(color);
        checkmarkPanel.setColor(color);
        checkmarkPanel.setHabit(habit);

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
        int offset = (int) ((today - timestamp) / day);
        CheckmarkButtonView button = checkmarkPanel.getButton(offset);

        float y = button.getHeight() / 2.0f;
        float x = checkmarkPanel.getX() + button.getX() + button.getWidth() / 2;
        triggerRipple(x, y);
    }

    private int getActiveColor(Habit habit)
    {
        int mediumContrastColor =
            getStyledColor(context, R.attr.mediumContrastTextColor);
        int activeColor = ColorUtils.getColor(context, habit.getColor());
        if (habit.isArchived()) activeColor = mediumContrastColor;

        return activeColor;
    }

    private void init()
    {
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));

        inflate(context, R.layout.list_habits_card, this);
        ButterKnife.bind(this);

        innerFrame.setOnTouchListener((v, event) -> {
            if (android.os.Build.VERSION.SDK_INT >= 21)
                v.getBackground().setHotspot(event.getX(), event.getY());
            return false;
        });

        if (isInEditMode()) initEditMode();
    }

    @SuppressLint("SetTextI18n")
    private void initEditMode()
    {
        String habits[] = {
            "Wake up early",
            "Wash dishes",
            "Exercise",
            "Meditate",
            "Play guitar",
            "Wash clothes",
            "Get a haircut"
        };

        Random rand = new Random();
        int color = ColorUtils.getAndroidTestColor(rand.nextInt(10));
        int[] values = {
            rand.nextInt(3),
            rand.nextInt(3),
            rand.nextInt(3),
            rand.nextInt(3),
            rand.nextInt(3)
        };

        label.setText(habits[rand.nextInt(habits.length)]);
        label.setTextColor(color);
        scoreRing.setColor(color);
        scoreRing.setPercentage(rand.nextFloat());
        checkmarkPanel.setColor(color);
        checkmarkPanel.setCheckmarkValues(values);
    }

    private void triggerRipple(final float x, final float y)
    {
        final Drawable background = innerFrame.getBackground();
        if (android.os.Build.VERSION.SDK_INT >= 21) background.setHotspot(x, y);
        background.setState(new int[]{
            android.R.attr.state_pressed, android.R.attr.state_enabled
        });
        new Handler().postDelayed(() -> background.setState(new int[]{}), 25);
    }

    private void updateBackground(boolean isSelected)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            if (isSelected)
                innerFrame.setBackgroundResource(R.drawable.selected_box);
            else innerFrame.setBackgroundResource(R.drawable.ripple);
        }
        else
        {
            Drawable background;

            if (isSelected) background =
                getStyledDrawable(context, R.attr.selectedBackground);
            else background = getStyledDrawable(context, R.attr.cardBackground);

            innerFrame.setBackgroundDrawable(background);
        }
    }

    public interface Controller extends CheckmarkPanelView.Controller
    {
    }
}
