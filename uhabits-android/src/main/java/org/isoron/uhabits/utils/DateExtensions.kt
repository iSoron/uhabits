/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.utils

import android.content.Context
import android.text.format.DateFormat
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.utils.DateFormats
import org.isoron.uhabits.core.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun String.toSimpleDataFormat(): SimpleDateFormat {
    val locale = Locale.getDefault()
    return DateFormats.fromSkeleton(DateFormat.getBestDateTimePattern(locale, this), locale)
}

fun WeekdayList.toFormattedString(context: Context): String {
    val shortDayNames = DateUtils.getShortWeekdayNames(Calendar.SATURDAY)
    val longDayNames = DateUtils.getLongWeekdayNames(Calendar.SATURDAY)
    val buffer = StringBuilder()
    var count = 0
    var first = 0
    var isFirst = true
    val array = this.toArray()
    for (i in 0..6) {
        if (array[i]) {
            if (isFirst) first = i else buffer.append(", ")
            buffer.append(shortDayNames[i])
            isFirst = false
            count++
        }
    }
    if (count == 1) return longDayNames[first]
    if (count == 2 && array[0] && array[1]) return context.getString(R.string.weekends)
    if (count == 5 && !array[0] && !array[1]) return context.getString(R.string.any_weekday)
    return if (count == 7) context.getString(R.string.any_day) else buffer.toString()
}

fun formatTime(context: Context, hours: Int, minutes: Int): String? {
    val reminderMilliseconds = (hours * 60 + minutes) * 60 * 1000L
    val date = Date(reminderMilliseconds)
    val df = DateFormat.getTimeFormat(context)
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(date)
}
