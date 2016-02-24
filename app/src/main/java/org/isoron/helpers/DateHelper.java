/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.helpers;

import android.content.Context;
import android.text.format.DateFormat;

import org.isoron.uhabits.R;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper
{
    public static int millisecondsInOneDay = 24 * 60 * 60 * 1000;

    public static long getLocalTime()
    {
        TimeZone tz = TimeZone.getDefault();
        long now = new Date().getTime();
        return now + tz.getOffset(now);
    }

    public static long toLocalTime(long timestamp)
    {
        TimeZone tz = TimeZone.getDefault();
        long now = new Date(timestamp).getTime();
        return now + tz.getOffset(now);
    }

    public static long getStartOfDay(long timestamp)
    {
        return (timestamp / millisecondsInOneDay) * millisecondsInOneDay;
    }

    public static GregorianCalendar getStartOfTodayCalendar()
    {
        GregorianCalendar day = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        day.setTimeInMillis(DateHelper.getStartOfDay(DateHelper.getLocalTime()));
        return day;
    }

    public static GregorianCalendar getCalendar(long timestamp)
    {
        GregorianCalendar day = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        day.setTimeInMillis(timestamp);
        return day;
    }

    public static int getWeekday(long timestamp)
    {
        GregorianCalendar day = getCalendar(timestamp);
        return day.get(GregorianCalendar.DAY_OF_WEEK) % 7;
    }

    public static long getStartOfToday()
    {
        return getStartOfDay(DateHelper.getLocalTime());
    }

    public static String formatTime(Context context, int hours, int minutes)
    {
        int reminderMilliseconds = (hours * 60 + minutes) * 60 * 1000;

        Date date = new Date(reminderMilliseconds);
        java.text.DateFormat df = DateFormat.getTimeFormat(context);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        return df.format(date);
    }

    public static String formatHeaderDate(GregorianCalendar day)
    {
        String dayOfMonth = Integer.toString(day.get(GregorianCalendar.DAY_OF_MONTH));
        String dayOfWeek = day.getDisplayName(GregorianCalendar.DAY_OF_WEEK,
                GregorianCalendar.SHORT, Locale.getDefault());

        return dayOfWeek + "\n" + dayOfMonth;
    }

    public static int differenceInDays(Date from, Date to)
    {
        long milliseconds = getStartOfDay(to.getTime()) - getStartOfDay(from.getTime());
        return (int) (milliseconds / millisecondsInOneDay);
    }

    public static String[] getShortDayNames()
    {
        return getDayNames(GregorianCalendar.SHORT);
    }

    public static String[] getLongDayNames()
    {
        return getDayNames(GregorianCalendar.LONG);
    }


    public static String[] getDayNames(int format)
    {
        String[] wdays = new String[7];

        GregorianCalendar day = new GregorianCalendar();
        day.set(GregorianCalendar.DAY_OF_WEEK, 0);

        for (int i = 0; i < 7; i++)
        {
            wdays[i] = day.getDisplayName(GregorianCalendar.DAY_OF_WEEK, format,
                    Locale.getDefault());
            day.add(GregorianCalendar.DAY_OF_MONTH, 1);
        }

        return wdays;
    }

    public static String formatWeekdayList(Context context, boolean weekday[])
    {
        String shortDayNames[] = getShortDayNames();
        String longDayNames[] = getLongDayNames();
        StringBuilder buffer = new StringBuilder();

        int count = 0;
        int first = 0;
        boolean isFirst = true;
        for(int i = 0; i < 7; i++)
        {
            if(weekday[i])
            {
                if(isFirst) first = i;
                else buffer.append(", ");

                buffer.append(shortDayNames[i]);
                isFirst = false;
                count++;
            }
        }

        if(count == 1) return longDayNames[first];
        if(count == 2 && weekday[0] && weekday[1]) return context.getString(R.string.weekends);
        if(count == 5 && !weekday[0] && !weekday[1]) return context.getString(R.string.any_weekday);
        if(count == 7) return context.getString(R.string.any_day);
        return buffer.toString();
    }

    public static Integer packWeekdayList(boolean weekday[])
    {
        int list = 0;
        int current = 1;

        for(int i = 0; i < 7; i++)
        {
            if(weekday[i]) list |= current;
            current = current << 1;
        }

        return list;
    }

    public static boolean[] unpackWeekdayList(int list)
    {
        boolean[] weekday = new boolean[7];
        int current = 1;

        for(int i = 0; i < 7; i++)
        {
            if((list & current) != 0) weekday[i] = true;
            current = current << 1;
        }

        return weekday;
    }

}
