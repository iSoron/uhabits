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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.isoron.uhabits.HabitBroadcastReceiver;
import org.isoron.uhabits.R;
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

    public static void createReminderAlarm(Context context, Habit habit, @Nullable Long reminderTime)
    {
        if(!habit.hasReminder()) return;

        if (reminderTime == null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            //noinspection ConstantConditions
            calendar.set(Calendar.HOUR_OF_DAY, habit.reminderHour);
            //noinspection ConstantConditions
            calendar.set(Calendar.MINUTE, habit.reminderMin);
            calendar.set(Calendar.SECOND, 0);

            reminderTime = calendar.getTimeInMillis();

            if (System.currentTimeMillis() > reminderTime)
                reminderTime += AlarmManager.INTERVAL_DAY;
        }

        long timestamp = DateHelper.getStartOfDay(DateHelper.toLocalTime(reminderTime));

        Uri uri = habit.getUri();

        Intent alarmIntent = new Intent(context, HabitBroadcastReceiver.class);
        alarmIntent.setAction(HabitBroadcastReceiver.ACTION_SHOW_REMINDER);
        alarmIntent.setData(uri);
        alarmIntent.putExtra("timestamp", timestamp);
        alarmIntent.putExtra("reminderTime", reminderTime);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, ((int) (habit.getId() % Integer.MAX_VALUE)) + 1,
                        alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= 23)
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        else if (Build.VERSION.SDK_INT >= 19)
            manager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        else
            manager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);

        String name = habit.name.substring(0, Math.min(3, habit.name.length()));
        Log.d("ReminderHelper", String.format("Setting alarm (%s): %s",
                DateFormat.getDateTimeInstance().format(new Date(reminderTime)), name));
    }

    @Nullable
    public static Uri getRingtoneUri(Context context)
    {
        Uri ringtoneUri = null;
        Uri defaultRingtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefRingtoneUri = prefs.getString("pref_ringtone_uri", defaultRingtoneUri.toString());
        if (prefRingtoneUri.length() > 0) ringtoneUri = Uri.parse(prefRingtoneUri);

        return ringtoneUri;
    }

    public static void parseRingtoneData(Context context, @Nullable Intent data)
    {
        if(data == null) return;

        Uri ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

        if (ringtoneUri != null)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString("pref_ringtone_uri", ringtoneUri.toString()).apply();
        }
        else
        {
            String off = context.getResources().getString(R.string.none);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString("pref_ringtone_uri", "").apply();
        }
    }

    public static void startRingtonePickerActivity(Fragment fragment, int requestCode)
    {
        Uri existingRingtoneUri = ReminderHelper.getRingtoneUri(fragment.getContext());
        Uri defaultRingtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultRingtoneUri);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingRingtoneUri);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static String getRingtoneName(Context context)
    {
        Uri ringtoneUri = getRingtoneUri(context);
        String ringtoneName = context.getResources().getString(R.string.none);

        if(ringtoneUri != null)
        {
            Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
            if(ringtone != null)
            {
                ringtoneName = ringtone.getTitle(context);
                ringtone.stop();
            }
        }

        return ringtoneName;
    }
}
