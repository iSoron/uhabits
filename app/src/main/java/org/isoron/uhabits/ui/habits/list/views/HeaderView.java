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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.utils.DateUtils;

import java.util.GregorianCalendar;

public class HeaderView extends LinearLayout
{
    private static final int CHECKMARK_LEFT_TO_RIGHT = 0;

    private static final int CHECKMARK_RIGHT_TO_LEFT = 1;

    private final Context context;

    public HeaderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
    }

    private void createButtons()
    {
        removeAllViews();
        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();

        for (int i = 0; i < getButtonCount(); i++)
        {
            int position = 0;

            if (getCheckmarkOrder() == CHECKMARK_LEFT_TO_RIGHT) position = i;

            View tvDay =
                inflate(context, R.layout.list_habits_header_checkmark, null);
            TextView btCheck = (TextView) tvDay.findViewById(R.id.tvCheck);
            btCheck.setText(DateUtils.formatHeaderDate(day));
            addView(tvDay, position);
            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }
    }

    private int getButtonCount()
    {
        float labelWidth = getResources().getDimension(R.dimen.habitNameWidth);
        float buttonWidth = getResources().getDimension(R.dimen.checkmarkWidth);
        return Math.max(0,
            (int) ((getMeasuredWidth() - labelWidth) / buttonWidth));
    }

    private int getCheckmarkOrder()
    {
        if (isInEditMode()) return CHECKMARK_LEFT_TO_RIGHT;

        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean reverse =
            prefs.getBoolean("pref_checkmark_reverse_order", false);
        return reverse ? CHECKMARK_RIGHT_TO_LEFT : CHECKMARK_LEFT_TO_RIGHT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        createButtons();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
