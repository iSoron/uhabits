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

package org.isoron.uhabits.utils;

import android.content.Context;
import android.text.format.DateFormat;

import org.isoron.uhabits.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public abstract class DateUtils
{
    public static int ALL_WEEK_DAYS = 127;

    private static Long fixedLocalTime = null;

    /**
     * Number of milliseconds in one day.
     */
    public static long millisecondsInOneDay = 24 * 60 * 60 * 1000;

    public static String formatHeaderDate(GregorianCalendar day)
    {
        String dayOfMonth =
            Integer.toString(day.get(GregorianCalendar.DAY_OF_MONTH));
        String dayOfWeek = day.getDisplayName(GregorianCalendar.DAY_OF_WEEK,
            GregorianCalendar.SHORT, Locale.getDefault());

        return dayOfWeek + "\n" + dayOfMonth;
    }

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
        String shortDayNames[] = getShortDayNames();
        String longDayNames[] = getLongDayNames();
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

    public static SimpleDateFormat getBackupDateFormat()
    {
        SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat;
    }

    public static SimpleDateFormat getCSVDateFormat()
    {
        SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat;
    }

    public static GregorianCalendar getCalendar(long timestamp)
    {
        GregorianCalendar day =
            new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        day.setTimeInMillis(timestamp);
        return day;
    }

    public static SimpleDateFormat getDateFormat(String skeleton)
    {
        String pattern;
        Locale locale = Locale.getDefault();

        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
            pattern = DateFormat.getBestDateTimePattern(locale, skeleton);
        else pattern = skeleton;

        SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        return format;
    }

    public static String[] getDayNames(int format)
    {
        String[] wdays = new String[7];

        Calendar day = new GregorianCalendar();
        day.set(GregorianCalendar.DAY_OF_WEEK, Calendar.SATURDAY);

        for (int i = 0; i < wdays.length; i++)
        {
            wdays[i] = day.getDisplayName(GregorianCalendar.DAY_OF_WEEK, format,
                Locale.getDefault());
            day.add(GregorianCalendar.DAY_OF_MONTH, 1);
        }

        return wdays;
    }

    public static long getLocalTime()
    {
        if (fixedLocalTime != null) return fixedLocalTime;

        TimeZone tz = TimeZone.getDefault();
        long now = new Date().getTime();
        return now + tz.getOffset(now);
    }

    /**
     * @return array with weekday names starting according to locale settings,
     * e.g. [Mo,Di,Mi,Do,Fr,Sa,So] in Europe
     */
    public static String[] getLocaleDayNames(int format)
    {
        String[] days = new String[7];

        Calendar calendar = new GregorianCalendar();
        calendar.set(GregorianCalendar.DAY_OF_WEEK,
            calendar.getFirstDayOfWeek());
        for (int i = 0; i < days.length; i++)
        {
            days[i] =
                calendar.getDisplayName(GregorianCalendar.DAY_OF_WEEK, format,
                    Locale.getDefault());
            calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
        }

        return days;
    }

    /**
     * @return array with week days numbers starting according to locale
     * settings, e.g. [2,3,4,5,6,7,1] in Europe
     */
    public static Integer[] getLocaleWeekdayList()
    {
        Integer[] dayNumbers = new Integer[7];
        Calendar calendar = new GregorianCalendar();
        calendar.set(GregorianCalendar.DAY_OF_WEEK,
            calendar.getFirstDayOfWeek());
        for (int i = 0; i < dayNumbers.length; i++)
        {
            dayNumbers[i] = calendar.get(GregorianCalendar.DAY_OF_WEEK);
            calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
        }
        return dayNumbers;
    }

    public static String[] getLongDayNames()
    {
        return getDayNames(GregorianCalendar.LONG);
    }

    public static String[] getShortDayNames()
    {
        return getDayNames(GregorianCalendar.SHORT);
    }

    public static long getStartOfDay(long timestamp)
    {
        return (timestamp / millisecondsInOneDay) * millisecondsInOneDay;
    }

    public static long getStartOfToday()
    {
        return getStartOfDay(DateUtils.getLocalTime());
    }

    public static GregorianCalendar getStartOfTodayCalendar()
    {
        return getCalendar(getStartOfToday());
    }

    public static int getWeekday(long timestamp)
    {
        GregorianCalendar day = getCalendar(timestamp);
        return day.get(GregorianCalendar.DAY_OF_WEEK) % 7;
    }

    /**
     * Throughout the code, it is assumed that the weekdays are numbered from 0
     * (Saturday) to 6 (Friday). In the Java Calendar they are numbered from 1
     * (Sunday) to 7 (Saturday). This function converts from Java to our
     * internal representation.
     *
     * @return weekday number in the internal interpretation
     */
    public static int javaWeekdayToLoopWeekday(int number)
    {
        return number % 7;
    }

    public static Integer packWeekdayList(boolean weekday[])
    {
        int list = 0;
        int current = 1;

        for (int i = 0; i < 7; i++)
        {
            if (weekday[i]) list |= current;
            current = current << 1;
        }

        return list;
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

    public static Long truncate(TruncateField field, long timestamp)
    {
        GregorianCalendar cal = DateUtils.getCalendar(timestamp);

        switch (field)
        {
            case MONTH:
                cal.set(Calendar.DAY_OF_MONTH, 1);
                return cal.getTimeInMillis();

            case WEEK_NUMBER:
                int firstWeekday = cal.getFirstDayOfWeek();
                int weekday = cal.get(Calendar.DAY_OF_WEEK);
                int delta = weekday - firstWeekday;
                if (delta < 0) delta += 7;
                cal.add(Calendar.DAY_OF_YEAR, -delta);
                return cal.getTimeInMillis();

            case QUARTER:
                int quarter = cal.get(Calendar.MONTH) / 3;
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.MONTH, quarter * 3);
                return cal.getTimeInMillis();

            case YEAR:
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                return cal.getTimeInMillis();

            default:
                throw new IllegalArgumentException();
        }
    }

    public static boolean[] unpackWeekdayList(int list)
    {
        boolean[] weekday = new boolean[7];
        int current = 1;

        for (int i = 0; i < 7; i++)
        {
            if ((list & current) != 0) weekday[i] = true;
            current = current << 1;
        }

        return weekday;
    }

    public enum TruncateField
    {
        MONTH, WEEK_NUMBER, YEAR, QUARTER
    }
}
