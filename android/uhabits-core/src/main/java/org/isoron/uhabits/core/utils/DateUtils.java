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

import androidx.annotation.*;

import org.isoron.uhabits.core.models.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static java.util.Calendar.*;

public abstract class DateUtils
{

    private static Long fixedLocalTime = null;

    private static TimeZone fixedTimeZone = null;

    private static Locale fixedLocale = null;

    /**
     * Number of milliseconds in one day.
     */
    public static final long DAY_LENGTH = 24 * 60 * 60 * 1000;

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

    public static long getLocalTime()
    {
        if (fixedLocalTime != null) return fixedLocalTime;

        TimeZone tz = getTimezone();
        long now = new Date().getTime();
        return now + tz.getOffset(now);
    }

    /**
     * Returns an array of strings with the names for each day of the week,
     * in either SHORT or LONG format. The first entry corresponds to the
     * first day of the week, according to the provided argument.
     *
     * @param format Either GregorianCalendar.SHORT or LONG
     * @param firstWeekday An integer representing the first day of the week,
     *                     following java.util.Calendar conventions. That is,
     *                     Saturday corresponds to 7, and Sunday corresponds
     *                     to 1.
     */
    @NotNull
    private static String[] getWeekdayNames(int format, int firstWeekday)
    {
        String[] days = new String[7];
        Calendar calendar = new GregorianCalendar();
        calendar.set(DAY_OF_WEEK, firstWeekday);
        for (int i = 0; i < days.length; i++) {
            days[i] = calendar.getDisplayName(DAY_OF_WEEK, format,
                    getLocale());
            calendar.add(DAY_OF_MONTH, 1);
        }

        return days;
    }

    /**
     * Returns a vector of exactly seven integers, where the first integer is
     * the provided firstWeekday number, and each subsequent number is the
     * previous number plus 1, wrapping back to 1 after 7. For example,
     * providing 3 as firstWeekday returns {3,4,5,6,7,1,2}
     *
     * This function is supposed to be used to construct a sequence of weekday
     * number following java.util.Calendar conventions.
     */
    public static int[] getWeekdaySequence(int firstWeekday)
    {
        return new int[]
        {
                (firstWeekday - 1) % 7 + 1,
                (firstWeekday) % 7 + 1,
                (firstWeekday + 1) % 7 + 1,
                (firstWeekday + 2) % 7 + 1,
                (firstWeekday + 3) % 7 + 1,
                (firstWeekday + 4) % 7 + 1,
                (firstWeekday + 5) % 7 + 1,
        };
    }

    /**
     * @return An integer representing the first day of the week, according to
     * the current locale. Sunday corresponds to 1, Monday to 2, and so on,
     * until Saturday, which is represented by 7. This is consistent
     * with java.util.Calendar constants.
     */
    public static int getFirstWeekdayNumberAccordingToLocale()
    {
        return new GregorianCalendar().getFirstDayOfWeek();
    }

    /**
     * @return A vector of strings with the long names for the week days,
     * according to the current locale. The first entry corresponds to Saturday,
     * the second entry corresponds to Monday, and so on.
     *
     * @param firstWeekday Either Calendar.SATURDAY, Calendar.MONDAY, or other
     *                     weekdays defined in this class.
     */
    public static String[] getLongWeekdayNames(int firstWeekday)
    {
        return getWeekdayNames(GregorianCalendar.LONG, firstWeekday);
    }

    /**
     * Returns a vector of strings with the short names for the week days,
     * according to the current locale. The first entry corresponds to Saturday,
     * the second entry corresponds to Monday, and so on.
     *
     * @param firstWeekday Either Calendar.SATURDAY, Calendar.MONDAY, or other
     *                     weekdays defined in this class.
     */
    public static String[] getShortWeekdayNames(int firstWeekday)
    {
        return getWeekdayNames(GregorianCalendar.SHORT, firstWeekday);
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
        return getStartOfDay(getLocalTime());
    }

    public static long getTomorrowStart()
    {
        return getUpcomingTimeInMillis(0, 0);
    }

    public static long millisecondsUntilTomorrow()
    {
        return getTomorrowStart() - getLocalTime();
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

    public static Long truncate(TruncateField field,
                                long timestamp,
                                int firstWeekday)
    {
        GregorianCalendar cal = DateUtils.getCalendar(timestamp);


        switch (field)
        {
            case MONTH:
                cal.set(DAY_OF_MONTH, 1);
                return cal.getTimeInMillis();

            case WEEK_NUMBER:
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
        long time = calendar.getTimeInMillis();

        if (DateUtils.getLocalTime() > time)
            time += DateUtils.DAY_LENGTH;

        return applyTimezone(time);
    }

    public enum TruncateField
    {
        MONTH, WEEK_NUMBER, YEAR, QUARTER
    }
}
