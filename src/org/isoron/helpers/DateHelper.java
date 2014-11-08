package org.isoron.helpers;

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
		int days = (int) (milliseconds / millisecondsInOneDay);
		return days;
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

		if(months > 18)
		{
			s.append(df.format(years));
			s.append(" years");
		}
		else if(weeks > 6)
		{
			s.append(df.format(months));
			s.append(" months");
		}
		else if(days > 13)
		{
			s.append(weeks);
			s.append(" weeks");
		}
		else if(days > 6)
		{
			s.append(days);
			s.append(" days");
		}
		else
		{
			if(days == 0)
				s.append("Today");
			else if(days == 1 && negative)
				s.append("Yesterday");
			else if(days == 1 && !negative)
				s.append("Tomorrow");
			else
			{
				if(negative)
					s.append("past ");
				s.append(new SimpleDateFormat("EEEE").format(to));
			}
		}

		if(negative && days > 6)
			s.append(" ago");

		return s.toString();
	}
}
