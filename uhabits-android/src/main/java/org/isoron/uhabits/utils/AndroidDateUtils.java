/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.utils;

import android.content.*;
import android.text.format.*;

import org.isoron.uhabits.*;

import java.util.*;

public class AndroidDateUtils
{
    public static String formatTime(Context context, int hours, int minutes)
    {
        int reminderMilliseconds = (hours * 60 + minutes) * 60 * 1000;

        Date date = new Date(reminderMilliseconds);
        java.text.DateFormat df = DateFormat.getTimeFormat(context);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        return df.format(date);
    }

    public static String formatWeekdayList(Context context, boolean weekday[])
    {
        String shortDayNames[] = org.isoron.uhabits.core.utils.DateUtils.getShortDayNames();
        String longDayNames[] = org.isoron.uhabits.core.utils.DateUtils.getLongDayNames();
        StringBuilder buffer = new StringBuilder();

        int count = 0;
        int first = 0;
        boolean isFirst = true;
        for (int i = 0; i < 7; i++)
        {
            if (weekday[i])
            {
                if (isFirst) first = i;
                else buffer.append(", ");

                buffer.append(shortDayNames[i]);
                isFirst = false;
                count++;
            }
        }

        if (count == 1) return longDayNames[first];
        if (count == 2 && weekday[0] && weekday[1])
            return context.getString(R.string.weekends);
        if (count == 5 && !weekday[0] && !weekday[1])
            return context.getString(R.string.any_weekday);
        if (count == 7) return context.getString(R.string.any_day);
        return buffer.toString();
    }
}
