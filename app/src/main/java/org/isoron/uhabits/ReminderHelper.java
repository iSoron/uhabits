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

package org.isoron.uhabits;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.isoron.uhabits.models.Habit;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class ReminderHelper
{
    public static void createReminderAlarms(Context context)
    {
        for (Habit habit : Habit.getHabitsWithReminder())
            createReminderAlarm(context, habit, null);
    }

    public static void createReminderAlarm(Context context, Habit habit, Long reminderTime)
    {
        Uri uri = Uri.parse("content://org.isoron.uhabits/habit/" + habit.getId());

        if (reminderTime == null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, habit.reminderHour);
            calendar.set(Calendar.MINUTE, habit.reminderMin);
            calendar.set(Calendar.SECOND, 0);

            reminderTime = calendar.getTimeInMillis();

            if (System.currentTimeMillis() > reminderTime)
            {
                reminderTime += AlarmManager.INTERVAL_DAY;
            }
        }

        Intent alarmIntent = new Intent(context, ReminderAlarmReceiver.class);
        alarmIntent.setAction(ReminderAlarmReceiver.ACTION_REMIND);
        alarmIntent.setData(uri);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, ((int) (habit.getId() % Integer.MAX_VALUE)) + 1,
                        alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 19)
        {
            manager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
        else
        {
            manager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }

        Log.d("Alarm", String.format("Setting alarm (%s): %s",
                DateFormat.getDateTimeInstance().format(new Date(reminderTime)), habit.name));
    }
}
