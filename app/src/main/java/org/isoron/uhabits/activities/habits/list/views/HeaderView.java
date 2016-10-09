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
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

public class HeaderView extends LinearLayout implements Preferences.Listener
{
    private final Context context;

    private int buttonCount;

    @Nullable
    private Preferences prefs;

    public HeaderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;

        if (isInEditMode())
        {
            setButtonCount(5);
        }

        Context appContext = context.getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
        }
    }

    @Override
    public void onCheckmarkOrderChanged()
    {
        createButtons();
    }

    public void setButtonCount(int buttonCount)
    {
        this.buttonCount = buttonCount;
        createButtons();
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

    private void createButtons()
    {
        removeAllViews();
        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();

        for (int i = 0; i < buttonCount; i++)
            addView(
                inflate(context, R.layout.list_habits_header_checkmark, null));

        for (int i = 0; i < getChildCount(); i++)
        {
            int position = i;
            if (shouldReverseCheckmarks()) position = getChildCount() - i - 1;

            View button = getChildAt(position);
            TextView label = (TextView) button.findViewById(R.id.tvCheck);
            label.setText(DateUtils.formatHeaderDate(day));
            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }
    }

    private boolean shouldReverseCheckmarks()
    {
        if (prefs == null) return false;
        return prefs.shouldReverseCheckmarks();
    }
}
