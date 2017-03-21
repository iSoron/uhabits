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
import org.isoron.uhabits.activities.habits.list.*;
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.utils.*;

import static android.view.View.MeasureSpec.*;

public class CheckmarkPanelView extends LinearLayout implements Preferences.Listener
{
    private static final int CHECKMARK_LEFT_TO_RIGHT = 0;

    private static final int CHECKMARK_RIGHT_TO_LEFT = 1;

    @Nullable
    private Preferences prefs;

    private int checkmarkValues[];

    private int nButtons;

    private int color;

    private Controller controller;

    @NonNull
    private Habit habit;

    private int dataOffset;

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

    public CheckmarkButtonView indexToButton(int i)
    {
        int position = i;

        if (getCheckmarkOrder() == CHECKMARK_RIGHT_TO_LEFT)
            position = nButtons - i - 1;

        return (CheckmarkButtonView) getChildAt(position);
    }

    public void setButtonCount(int newButtonCount)
    {
        if(nButtons != newButtonCount)
        {
            nButtons = newButtonCount;
            addCheckmarkButtons();
        }

        setupCheckmarkButtons();
    }

    public void setCheckmarkValues(int[] checkmarkValues)
    {
        this.checkmarkValues = checkmarkValues;
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
        setupCheckmarkButtons();
    }

    public void setDataOffset(int dataOffset)
    {
        this.dataOffset = dataOffset;
        setupCheckmarkButtons();
    }

    public void setHabit(@NonNull Habit habit)
    {
        this.habit = habit;
        setupCheckmarkButtons();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec)
    {
        float buttonWidth = getResources().getDimension(R.dimen.checkmarkWidth);
        float buttonHeight =
            getResources().getDimension(R.dimen.checkmarkHeight);

        float width = buttonWidth * nButtons;

        widthSpec = makeMeasureSpec((int) width, EXACTLY);
        heightSpec = makeMeasureSpec((int) buttonHeight, EXACTLY);

        super.onMeasure(widthSpec, heightSpec);
    }

    private void addCheckmarkButtons()
    {
        removeAllViews();

        for (int i = 0; i < nButtons; i++)
            addView(new CheckmarkButtonView(getContext()));
    }

    private int getCheckmarkOrder()
    {
        if (prefs == null) return CHECKMARK_LEFT_TO_RIGHT;
        return prefs.shouldReverseCheckmarks() ? CHECKMARK_RIGHT_TO_LEFT :
            CHECKMARK_LEFT_TO_RIGHT;
    }

    private void init()
    {
        Context appContext = getContext().getApplicationContext();
        if(appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
        }

        setWillNotDraw(false);
    }

    private void setupButtonControllers(long timestamp,
                                        CheckmarkButtonView buttonView)
    {
        if (controller == null) return;
        if (!(getContext() instanceof ListHabitsActivity)) return;

        ListHabitsActivity activity = (ListHabitsActivity) getContext();
        CheckmarkButtonControllerFactory buttonControllerFactory = activity
            .getListHabitsComponent()
            .getCheckmarkButtonControllerFactory();

        CheckmarkButtonController buttonController =
            buttonControllerFactory.create(habit, timestamp);
        buttonController.setListener(controller);
        buttonController.setView(buttonView);
        buttonView.setController(buttonController);
    }

    private void setupCheckmarkButtons()
    {
        long timestamp = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;
        timestamp -= day * dataOffset;

        for (int i = 0; i < nButtons; i++)
        {
            CheckmarkButtonView buttonView = indexToButton(i);
            if(i + dataOffset >= checkmarkValues.length) break;
            buttonView.setValue(checkmarkValues[i + dataOffset]);
            buttonView.setColor(color);
            setupButtonControllers(timestamp, buttonView);
            timestamp -= day;
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if(prefs != null) prefs.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if(prefs != null) prefs.removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onCheckmarkOrderChanged()
    {
        setupCheckmarkButtons();
    }

    public interface Controller extends CheckmarkButtonController.Listener
    {

    }
}
