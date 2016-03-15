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

package org.isoron.uhabits.views;

import android.content.Context;
import android.util.AttributeSet;

import org.isoron.helpers.DateHelper;
import org.isoron.helpers.DialogHelper;
import org.isoron.uhabits.models.Habit;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class RepetitionCountView extends NumberView implements HabitDataView
{
    private int interval;
    private Habit habit;

    public RepetitionCountView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.interval = DialogHelper.getIntAttribute(context, attrs, "interval");

        int labelValue = DialogHelper.getIntAttribute(context, attrs, "labelValue");
        String labelFormat = DialogHelper.getAttribute(context, attrs, "labelFormat");

        if(labelValue > 0)
            setLabel(String.format(labelFormat, labelValue));

        refreshData();
    }

    @Override
    public void refreshData()
    {
        if(isInEditMode())
        {
            setNumber(interval);
            return;
        }

        long to = DateHelper.getStartOfToday();
        long from;

        if(interval == 0)
        {
            from = 0;
        }
        else
        {
            GregorianCalendar fromCalendar = DateHelper.getStartOfTodayCalendar();
            fromCalendar.add(Calendar.DAY_OF_YEAR, -interval + 1);
            from = fromCalendar.getTimeInMillis();
        }

        if(habit != null)
            setNumber(habit.repetitions.count(from, to));
    }

    @Override
    public void setHabit(Habit habit)
    {
        this.habit = habit;
        setColor(habit.color);
        refreshData();
    }
}
