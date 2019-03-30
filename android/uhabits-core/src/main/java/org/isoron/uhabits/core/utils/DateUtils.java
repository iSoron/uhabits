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

package org.isoron.uhabits.core.utils;

import android.support.annotation.*;

import org.isoron.uhabits.core.models.*;

import java.util.*;

import static java.util.Calendar.*;

public abstract class DateUtils
{

    private static Long fixedLocalTime = null;

    private static TimeZone fixedTimeZone = null;

    private static Locale fixedLocale = null;

    /**
     * Time of the day when the new day starts.
     */
    public static final int NEW_DAY_OFFSET = 3;

    /**
     * Number of milliseconds in one day.
     */
    public static final long DAY_LENGTH = 24 * 60 * 60 * 1000;

    /**
     * Number of milliseconds in one hour.
     */
    public static final long HOUR_LENGTH = 60 * 60 * 1000;

    public static long applyTimezone(long localTimestamp)
    {
        TimeZone tz = getTimezone();
        return localTimestamp - tz.getOffset(localTimestamp - tz.getOffset(localTimestamp));
    }

    public static String formatHeaderDate(GregorianCalendar day)
    {
        Locale locale = getLocale();
        String dayOfMonth = Integer.toString(day.get(DAY_OF_MONTH));
        String dayOfWeek = day.getDisplayName(DAY_OF_WEEK, SHORT, locale);
        return dayOfWeek + "\n" + dayOfMonth;
    }

    private static GregorianCalendar getCalendar(long timestamp)
    {
        GregorianCalendar day =
            new GregorianCalendar(TimeZone.getTimeZone("GMT"), getLocale());
        day.setTimeInMillis(timestamp);
        return day;
    }

    private static String[] getDayNames(int format)
    {
        String[] wdays = new String[7];

        Calendar day = new GregorianCalendar();
        day.set(DAY_OF_WEEK, Calendar.SATURDAY);

        for (int i = 0; i < wdays.length; i++)
        {
            wdays[i] =
                day.getDisplayName(DAY_OF_WEEK, format, getLocale());
            day.add(DAY_OF_MONTH, 1);
        }

        return wdays;
    }

    public static long getLocalTime()
    {
        if (fixedLocalTime != null) return fixedLocalTime;

        TimeZone tz = getTimezone();
        long now = new Date().getTime();
        return now + tz.getOffset(now);
    }

    /**
     * @return array with weekday names starting according to locale settings,
     * e.g. [Mo,Di,Mi,Do,Fr,Sa,So] in Germany
     */
    public static String[] getLocaleDayNames(int format)
    {
        String[] days = new String[7];

        Calendar calendar = new GregorianCalendar();
        calendar.set(DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        for (int i = 0; i < days.length; i++)
        {
            days[i] = calendar.getDisplayName(DAY_OF_WEEK, format,
                getLocale());
            calendar.add(DAY_OF_MONTH, 1);
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
        calendar.set(DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        for (int i = 0; i < dayNumbers.length; i++)
        {
            dayNumbers[i] = calendar.get(DAY_OF_WEEK);
            calendar.add(DAY_OF_MONTH, 1);
        }
        return dayNumbers;
    }

    public static String[] getLongDayNames()
    {
        return getDayNames(GregorianCalendar.LONG);
    }

    public static String[] getShortDayNames()
    {
        return getDayNames(SHORT);
    }

    @NonNull
    public static Timestamp getToday()
    {
        return new Timestamp(getStartOfToday());
    }

    public static long getStartOfDay(long timestamp)
    {
        return (timestamp / DAY_LENGTH) * DAY_LENGTH;
    }

    public static long getStartOfToday()
    {
        return getStartOfDay(getLocalTime() - NEW_DAY_OFFSET * HOUR_LENGTH);
    }

    public static long millisecondsUntilTomorrow()
    {
        return getStartOfToday() + DAY_LENGTH -
               (getLocalTime() - NEW_DAY_OFFSET * HOUR_LENGTH);
    }

    public static GregorianCalendar getStartOfTodayCalendar()
    {
        return getCalendar(getStartOfToday());
    }

    private static TimeZone getTimezone()
    {
        if(fixedTimeZone != null) return fixedTimeZone;
        return TimeZone.getDefault();
    }

    public static void setFixedTimeZone(TimeZone tz)
    {
        fixedTimeZone = tz;
    }

    public static long removeTimezone(long timestamp)
    {
        TimeZone tz = getTimezone();
        return timestamp + tz.getOffset(timestamp);
    }

    public static void setFixedLocalTime(Long timestamp)
    {
        fixedLocalTime = timestamp;
    }

    public static void setFixedLocale(Locale locale)
    {
        fixedLocale = locale;
    }

    private static Locale getLocale()
    {
        if(fixedLocale != null) return fixedLocale;
        return Locale.getDefault();
    }

    public static Long truncate(TruncateField field, long timestamp)
    {
        GregorianCalendar cal = DateUtils.getCalendar(timestamp);

        switch (field)
        {
            case MONTH:
                cal.set(DAY_OF_MONTH, 1);
                return cal.getTimeInMillis();

            case WEEK_NUMBER:
                int firstWeekday = cal.getFirstDayOfWeek();
                int weekday = cal.get(DAY_OF_WEEK);
                int delta = weekday - firstWeekday;
                if (delta < 0) delta += 7;
                cal.add(Calendar.DAY_OF_YEAR, -delta);
                return cal.getTimeInMillis();

            case QUARTER:
                int quarter = cal.get(Calendar.MONTH) / 3;
                cal.set(DAY_OF_MONTH, 1);
                cal.set(Calendar.MONTH, quarter * 3);
                return cal.getTimeInMillis();

            case YEAR:
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(DAY_OF_MONTH, 1);
                return cal.getTimeInMillis();

            default:
                throw new IllegalArgumentException();
        }
    }

    public static long getUpcomingTimeInMillis(int hour, int minute)
    {
        Calendar calendar = DateUtils.getStartOfTodayCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Long time = calendar.getTimeInMillis();

        if (DateUtils.getLocalTime() > time)
            time += DateUtils.DAY_LENGTH;

        return applyTimezone(time);
    }

    public enum TruncateField
    {
        MONTH, WEEK_NUMBER, YEAR, QUARTER
    }
}
