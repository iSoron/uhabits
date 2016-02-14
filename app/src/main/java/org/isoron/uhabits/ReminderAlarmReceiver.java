package org.isoron.uhabits;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.isoron.uhabits.models.Habit;

import java.util.Date;

public class ReminderAlarmReceiver extends BroadcastReceiver
{

    public static String ACTION_CHECK = "org.isoron.uhabits.ACTION_CHECK";
    public static String ACTION_DISMISS = "org.isoron.uhabits.ACTION_DISMISS";
    public static String ACTION_REMIND = "org.isoron.uhabits.ACTION_REMIND";
    public static String ACTION_REMOVE_REMINDER = "org.isoron.uhabits.ACTION_REMOVE_REMINDER";
    public static String ACTION_SNOOZE = "org.isoron.uhabits.ACTION_SNOOZE";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if (action.equals(ACTION_REMIND)) createNotification(context, intent.getData());

        else if (action.equals(ACTION_DISMISS)) dismissAllHabits();

        else if (action.equals(ACTION_CHECK)) checkHabit(context, intent.getData());

        else if (action.equals(ACTION_SNOOZE)) snoozeHabit(context, intent.getData());
    }

    private void snoozeHabit(Context context, Uri data)
    {
        int delayMinutes = 60;
        Habit habit = Habit.get(ContentUris.parseId(data));
        ReminderHelper.createReminderAlarm(context, habit,
                new Date().getTime() + delayMinutes * 60 * 1000);
        dismissNotification(context, habit);
    }

    private void checkHabit(Context context, Uri data)
    {
        Habit habit = Habit.get(ContentUris.parseId(data));
        habit.toggleRepetitionToday();
        habit.save();
        dismissNotification(context, habit);
    }

    private void dismissAllHabits()
    {
        for (Habit h : Habit.getHighlightedHabits())
        {
            h.highlight = 0;
            h.save();
        }
    }

    private void dismissNotification(Context context, Habit habit)
    {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        int notificationId = (int) (habit.getId() % Integer.MAX_VALUE);
        notificationManager.cancel(notificationId);
    }


    private void createNotification(Context context, Uri data)
    {

        Habit habit = Habit.get(ContentUris.parseId(data));

        if (habit.hasImplicitRepToday()) return;

        Log.d("Alarm", String.format("Applying highlight: %s", habit.name));
        habit.highlight = 1;
        habit.save();

        // Check if reminder has been turned off after alarm was scheduled
        if (habit.reminder_hour == null) return;

        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.setData(data);
        PendingIntent contentPendingIntent =
                PendingIntent.getActivity(context, 0, contentIntent, 0);

        Intent deleteIntent = new Intent(context, ReminderAlarmReceiver.class);
        deleteIntent.setAction(ACTION_DISMISS);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);

        Intent checkIntent = new Intent(context, ReminderAlarmReceiver.class);
        checkIntent.setData(data);
        checkIntent.setAction(ACTION_CHECK);
        PendingIntent checkIntentPending = PendingIntent.getBroadcast(context, 0, checkIntent, 0);

        Intent snoozeIntent = new Intent(context, ReminderAlarmReceiver.class);
        snoozeIntent.setData(data);
        snoozeIntent.setAction(ACTION_SNOOZE);
        PendingIntent snoozeIntentPending = PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender().setBackground(
                        BitmapFactory.decodeResource(context.getResources(), R.drawable.stripe));

        Notification notification =
                new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(habit.name)
                        .setContentText(habit.description)
                        .setContentIntent(contentPendingIntent)
                        .setDeleteIntent(deletePendingIntent)
                        .addAction(R.drawable.ic_action_check, "Check", checkIntentPending)
                        .addAction(R.drawable.ic_action_snooze, "Later", snoozeIntentPending)
                        .setSound(soundUri)
                        .extend(wearableExtender)
                        .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        int notificationId = (int) (habit.getId() % Integer.MAX_VALUE);
        notificationManager.notify(notificationId, notification);
    }

}
