package org.isoron.uhabits;

import java.util.Date;
import java.util.List;

import org.isoron.uhabits.models.Habit;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
		
		if(action.equals(ACTION_REMIND))
			createNotification(context, intent.getData());
		
		else if(action.equals(ACTION_DISMISS))
			dismissAllHabits();
		
		else if(action.equals(ACTION_CHECK))
			checkHabit(context, intent.getData());

		else if(action.equals(ACTION_SNOOZE))
			snoozeHabit(context, intent.getData());
	}
	
	private void snoozeHabit(Context context, Uri data)
	{
		int delayMinutes = 15;
		Habit habit = Habit.get(ContentUris.parseId(data));
		MainActivity.createReminderAlarm(context, habit, new Date().getTime() + delayMinutes * 1000);
		dismissNotification(context);
	}
	
	private void checkHabit(Context context, Uri data)
	{
		Habit habit = Habit.get(ContentUris.parseId(data));
		habit.toggleRepetitionToday();
		habit.save();
		dismissNotification(context);
	}
	
	private void dismissAllHabits()
	{
		for(Habit h : Habit.getHighlightedHabits())
		{
			Log.d("Alarm", String.format("Removing highlight from: %s", h.name)); 
			h.highlight = 0;
			h.save();
		}
	}
	
	private void dismissNotification(Context context)
	{
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Activity.NOTIFICATION_SERVICE);
		
		notificationManager.cancel(1);
	}
	
	
	private void createNotification(Context context, Uri data)
	{
		Log.d("Alarm", "Alarm received!");
		
		Habit habit = Habit.get(ContentUris.parseId(data));
		
		if(habit.hasImplicitRepToday())
		{
			Log.d("Alarm", String.format("(%s) has implicit rep today", habit.name));
			return;
		}

		Log.d("Alarm", String.format("Applying highlight: %s", habit.name));
		habit.highlight = 1;
		habit.save();
		
		// Check if reminder has been turned off after alarm was scheduled 
		if(habit.reminder_hour == null)
			return;
		
		Intent contentIntent = new Intent(context, MainActivity.class);
		contentIntent.setData(data);
		PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, 0);
		
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
		
		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		inboxStyle.setBigContentTitle("Habit Reminder:");
		List<Habit> pendingHabits = Habit.getHighlightedHabits();
		StringBuffer contentText = new StringBuffer();
		for(Habit h : pendingHabits)
		{
			if(h.hasImplicitRepToday())
				continue;
			
			inboxStyle.addLine(h.name);
			if(contentText.length() > 0)
				contentText.append(", ");
			contentText.append(h.name);
			Log.d("Alarm", String.format("Found highlighted: %s", h.name));
		}
		
		Notification notification =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_notification)
						.setContentTitle("Habit Reminder")
						.setContentText(contentText)
						.setContentIntent(contentPendingIntent)
						.setDeleteIntent(deletePendingIntent)
						.addAction(R.drawable.ic_action_check, "Check", checkIntentPending)
						.addAction(R.drawable.ic_action_snooze, "Later", snoozeIntentPending)
						.setSound(soundUri)
						.setStyle(inboxStyle)
						.build();
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Activity.NOTIFICATION_SERVICE);
		
		notificationManager.notify(1, notification);
	}

}
