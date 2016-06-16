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

import android.app.*;
import android.content.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.provider.*;
import android.support.annotation.*;
import android.support.v4.app.Fragment;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;

import java.text.*;
import java.util.*;

public abstract class ReminderUtils
{
    public static void createReminderAlarm(Context context,
                                           Habit habit,
                                           @Nullable Long reminderTime)
    {
        if (!habit.hasReminder()) return;
        Reminder reminder = habit.getReminder();

        if (reminderTime == null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
            calendar.set(Calendar.MINUTE, reminder.getMinute());
            calendar.set(Calendar.SECOND, 0);

            reminderTime = calendar.getTimeInMillis();

            if (System.currentTimeMillis() > reminderTime)
                reminderTime += AlarmManager.INTERVAL_DAY;
        }

        long timestamp =
            DateUtils.getStartOfDay(DateUtils.toLocalTime(reminderTime));

        Uri uri = habit.getUri();

        Intent alarmIntent = new Intent(context, HabitBroadcastReceiver.class);
        alarmIntent.setAction(HabitBroadcastReceiver.ACTION_SHOW_REMINDER);
        alarmIntent.setData(uri);
        alarmIntent.putExtra("timestamp", timestamp);
        alarmIntent.putExtra("reminderTime", reminderTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
            ((int) (habit.getId() % Integer.MAX_VALUE)) + 1, alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= 23)
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                reminderTime, pendingIntent);
        else if (Build.VERSION.SDK_INT >= 19)
            manager.setExact(AlarmManager.RTC_WAKEUP, reminderTime,
                pendingIntent);
        else manager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);

        String name = habit.getName().substring(0, Math.min(3, habit.getName().length()));
        Log.d("ReminderHelper", String.format("Setting alarm (%s): %s",
            DateFormat.getDateTimeInstance().format(new Date(reminderTime)),
            name));
    }

    public static void createReminderAlarms(Context context,
                                            HabitList habitList)
    {
        for (Habit habit : habitList.getWithReminder())
            createReminderAlarm(context, habit, null);
    }

    @Nullable
    public static String getRingtoneName(Context context)
    {
        try
        {
            Uri ringtoneUri = getRingtoneUri(context);
            String ringtoneName =
                context.getResources().getString(R.string.none);

            if (ringtoneUri != null)
            {
                Ringtone ringtone =
                    RingtoneManager.getRingtone(context, ringtoneUri);
                if (ringtone != null)
                {
                    ringtoneName = ringtone.getTitle(context);
                    ringtone.stop();
                }
            }

            return ringtoneName;
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Uri getRingtoneUri(Context context)
    {
        Uri ringtoneUri = null;
        Uri defaultRingtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;

        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String prefRingtoneUri =
            prefs.getString("pref_ringtone_uri", defaultRingtoneUri.toString());
        if (prefRingtoneUri.length() > 0)
            ringtoneUri = Uri.parse(prefRingtoneUri);

        return ringtoneUri;
    }

    public static void parseRingtoneData(Context context, @Nullable Intent data)
    {
        if (data == null) return;

        Uri ringtoneUri =
            data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

        if (ringtoneUri != null)
        {
            SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
            prefs
                .edit()
                .putString("pref_ringtone_uri", ringtoneUri.toString())
                .apply();
        }
        else
        {
            String off = context.getResources().getString(R.string.none);
            SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString("pref_ringtone_uri", "").apply();
        }
    }

    public static void startRingtonePickerActivity(Fragment fragment,
                                                   int requestCode)
    {
        Uri existingRingtoneUri =
            ReminderUtils.getRingtoneUri(fragment.getContext());
        Uri defaultRingtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
            RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
            defaultRingtoneUri);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
            existingRingtoneUri);
        fragment.startActivityForResult(intent, requestCode);
    }
}
