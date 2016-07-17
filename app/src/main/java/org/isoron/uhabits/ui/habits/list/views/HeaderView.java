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

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import javax.inject.*;

public class HeaderView extends LinearLayout
{
    private final Context context;

    private int buttonCount;

    @Inject
    Preferences prefs;

    public HeaderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;

        if (isInEditMode())
        {
            setButtonCount(5);
            return;
        }

        HabitsApplication.getComponent().inject(this);
    }

    public void setButtonCount(int buttonCount)
    {
        this.buttonCount = buttonCount;
        createButtons();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();

        for (int i = 0; i < getChildCount(); i++)
        {
            int position = i;
            if (shouldReverseCheckmarks()) position = getChildCount() - i - 1;

            View button = getChildAt(position);
            TextView label = (TextView) button.findViewById(R.id.tvCheck);
            label.setText(DateUtils.formatHeaderDate(day));
            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        createButtons();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void createButtons()
    {
        int layout = R.layout.list_habits_header_checkmark;

        removeAllViews();
        for (int i = 0; i < buttonCount; i++)
            addView(inflate(context, layout, null));
    }

    private boolean shouldReverseCheckmarks()
    {
        if (isInEditMode()) return false;
        return prefs.shouldReverseCheckmarks();
    }
}
