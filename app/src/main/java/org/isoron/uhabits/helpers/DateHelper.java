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

package org.isoron.uhabits.helpers;

import android.content.Context;
import android.text.format.DateFormat;

import org.isoron.uhabits.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper
{
    public static long millisecondsInOneDay = 24 * 60 * 60 * 1000;
    public static int ALL_WEEK_DAYS = 127;

    private static Long fixedLocalTime = null;

    public static long getLocalTime()
    {
        if(fixedLocalTime != null) return fixedLocalTime;

        TimeZone tz = TimeZone.getDefault();
        long now = new Date().getTime();
        return now + tz.getOffset(now);
    }

    public static void setFixedLocalTime(Long timestamp)
    {
        fixedLocalTime = timestamp;
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
        return getCalendar(getStartOfToday());
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

    public static SimpleDateFormat getDateFormat(String skeleton)
    {
        String pattern;
        Locale locale = Locale.getDefault();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
            pattern = DateFormat.getBestDateTimePattern(locale, skeleton);
        else
            pattern = skeleton;

        SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        return format;
    }

    public static SimpleDateFormat getCSVDateFormat()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat;
    }

    public static SimpleDateFormat getBackupDateFormat()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat;
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


    /**
     * Throughout the code, it is assumed that the weekdays are numbered
     * from 0 (Saturday) to 6 (Friday).
     *
     * see https://github.com/iSoron/uhabits/issues/74
     *
     * In the Java Calendar they are numbered from 1 (Sunday) to 7 (Saturday)
     *
     * <table>
     * <thead>
     * <tr><th>day</th><th>dayNumber</th><th>wdaysIndex</th></tr>
     * <thead>
     * <tbody>
     * <tr><td>Su</td><td>1</td><td>1</td></tr>
     * <tr><td>Mo</td><td>2</td><td>2</td></tr>
     * <tr><td>Tu</td><td>3</td><td>3</td></tr>
     * <tr><td>We</td><td>4</td><td>4</td></tr>
     * <tr><td>Th</td><td>5</td><td>5</td></tr>
     * <tr><td>Fr</td><td>6</td><td>6</td></tr>
     * <tr><td>Sa</td><td>7</td><td>0</td></tr>
     * </tbody>
     * </table>
     *
     * So we have {@code wdaysIndex = dayNumber % 7}
     *
     * @return array with names from Saturday to Friday according to the current locale
     *
     * @see #getWeekday(long)
     * @see java.util.Calendar#SUNDAY
     *
     */
    public static String[] getDayNames(int format)
    {
        String[] wdays = new String[7];

        Calendar day = new GregorianCalendar();
        // we start with Saturday
        day.set(GregorianCalendar.DAY_OF_WEEK, Calendar.SATURDAY);

        for (int i = 0; i < wdays.length; i++)
        {
            wdays[i] = day.getDisplayName(GregorianCalendar.DAY_OF_WEEK, format,
                    Locale.getDefault());
            // advance in time by one day
            day.add(GregorianCalendar.DAY_OF_MONTH, 1);
        }

        return wdays;
    }


    /**
     *
     * @return array with week days numbers starting according to locale settings,
     * e.g. [2,3,4,5,6,7,1] in Europe
     *
     * @see java.util.Calendar#SUNDAY
     *
     */
    public static Integer[] getLocaleWeekdayList()
    {
        Integer[] dayNumbers = new Integer[7];
        // a dummy calendar
        Calendar calendar = new GregorianCalendar();
        // set staring day according to locale
        calendar.set(GregorianCalendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        for (int i = 0; i < dayNumbers.length; i++) {
            dayNumbers[i] = calendar.get(GregorianCalendar.DAY_OF_WEEK);
            // advance in time by one day
            calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
        }
        return dayNumbers;
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
