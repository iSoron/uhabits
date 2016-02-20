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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static long getStartOfDay(long timestamp)
    {
        return (timestamp / millisecondsInOneDay) * millisecondsInOneDay;
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

//	public static Date getStartOfDay(Date date)
//	{
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(date);
//		calendar.set(Calendar.HOUR_OF_DAY, 0);
//		calendar.set(Calendar.MINUTE, 0);
//		calendar.set(Calendar.SECOND, 0);
//		calendar.set(Calendar.MILLISECOND, 0);
//		return calendar.getTime();
//	}

    public static int differenceInDays(Date from, Date to)
    {
        long milliseconds = getStartOfDay(to.getTime()) - getStartOfDay(from.getTime());
        return (int) (milliseconds / millisecondsInOneDay);
    }

    public static String differenceInWords(Date from, Date to)
    {
        Integer days = differenceInDays(from, to);
        boolean negative = (days < 0);
        days = Math.abs(days);

        Integer weeks = (int) Math.round(days / 7.0);
        Double months = days / 30.4;
        Double years = days / 365.0;

        StringBuffer s = new StringBuffer();
        DecimalFormat df = new DecimalFormat("#.#");

        if (months > 18)
        {
            s.append(df.format(years));
            s.append(" years");
        }
        else if (weeks > 6)
        {
            s.append(df.format(months));
            s.append(" months");
        }
        else if (days > 13)
        {
            s.append(weeks);
            s.append(" weeks");
        }
        else if (days > 6)
        {
            s.append(days);
            s.append(" days");
        }
        else
        {
            if (days == 0) s.append("Today");
            else if (days == 1 && negative) s.append("Yesterday");
            else if (days == 1 && !negative) s.append("Tomorrow");
            else
            {
                if (negative) s.append("past ");
                s.append(new SimpleDateFormat("EEEE").format(to));
            }
        }

        if (negative && days > 6) s.append(" ago");

        return s.toString();
    }
}
