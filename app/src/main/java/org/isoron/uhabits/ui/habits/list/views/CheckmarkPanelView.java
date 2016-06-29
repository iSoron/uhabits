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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.habits.list.controllers.CheckmarkButtonController;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.Preferences;

import javax.inject.Inject;

public class CheckmarkPanelView extends LinearLayout
{
    private static final int CHECKMARK_LEFT_TO_RIGHT = 0;

    private static final int CHECKMARK_RIGHT_TO_LEFT = 1;

    @Inject
    Preferences prefs;

    private int checkmarkValues[];

    private int nButtons;

    private int color;

    private Controller controller;

    @NonNull
    private Habit habit;

    public CheckmarkPanelView(Context context)
    {
        super(context);
        init();
    }

    public CheckmarkPanelView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public CheckmarkPanelView(Context context,
                              AttributeSet attrs,
                              int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CheckmarkButtonView getButton(int position)
    {
        return (CheckmarkButtonView) getChildAt(position);
    }

    public void setCheckmarkValues(int[] checkmarkValues)
    {
        this.checkmarkValues = checkmarkValues;

        if (this.nButtons != checkmarkValues.length)
        {
            this.nButtons = checkmarkValues.length;
            addCheckmarkButtons();
        }

        setupCheckmarkButtons();
    }

    public void setColor(int color)
    {
        this.color = color;
        setupCheckmarkButtons();
    }

    public void setController(Controller controller)
    {
        this.controller = controller;
    }

    public void setHabit(@NonNull Habit habit)
    {
        this.habit = habit;
    }

    private void addCheckmarkButtons()
    {
        removeAllViews();

        for (int i = 0; i < nButtons; i++)
            addView(new CheckmarkButtonView(getContext()));
    }

    private int getCheckmarkOrder()
    {
        if (isInEditMode()) return CHECKMARK_LEFT_TO_RIGHT;
        return prefs.shouldReverseCheckmarks() ? CHECKMARK_RIGHT_TO_LEFT :
            CHECKMARK_LEFT_TO_RIGHT;
    }

    private CheckmarkButtonView indexToButton(int i)
    {
        int position = i;

        if (getCheckmarkOrder() == CHECKMARK_RIGHT_TO_LEFT)
            position = nButtons - i - 1;

        return (CheckmarkButtonView) getChildAt(position);
    }

    private void init()
    {
        if (isInEditMode()) return;
        HabitsApplication.getComponent().inject(this);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        float buttonWidth = getResources().getDimension(R.dimen.checkmarkWidth);
        float buttonHeight =
            getResources().getDimension(R.dimen.checkmarkHeight);

        float width = buttonWidth * nButtons;

        widthMeasureSpec =
            MeasureSpec.makeMeasureSpec((int) width, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) buttonHeight,
            MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setupButtonControllers(long timestamp,
                                        CheckmarkButtonView buttonView)
    {
        if (controller == null) return;

        CheckmarkButtonController buttonController =
            new CheckmarkButtonController(habit, timestamp);

        buttonController.setListener(controller);
        buttonController.setView(buttonView);
        buttonView.setController(buttonController);
    }

    private void setupCheckmarkButtons()
    {
        long timestamp = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;

        for (int i = 0; i < nButtons; i++)
        {
            CheckmarkButtonView buttonView = indexToButton(i);
            buttonView.setValue(checkmarkValues[i]);
            buttonView.setColor(color);
            setupButtonControllers(timestamp, buttonView);
            timestamp -= day;
        }
    }

    public interface Controller extends CheckmarkButtonController.Listener
    {
    }
}
