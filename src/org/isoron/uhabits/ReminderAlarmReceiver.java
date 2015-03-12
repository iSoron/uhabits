package org.isoron.uhabits;

import java.util.List;

import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

import android.app.Activity;
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
	@Override
	public void onReceive(Context context, Intent intent)
	{
		createNotification(context, intent.getData(), intent.getDataString());
	}
	
	
	private void createNotification(Context context, Uri data, String text)
	{
		Log.d("Alarm", "Alarm received!");
		
		Habit habit = Habit.get(ContentUris.parseId(data));
		
		// Check if user already did the habit repetition
		if(habit.hasRep(DateHelper.getStartOfDay(DateHelper.getLocalTime())))
			return;

		Log.d("Alarm", String.format("Applying highlight: %s", habit.name));
		habit.highlight = 1;
		habit.save();
		
		// Check if reminder has been turned off after alarm was scheduled 
		if(habit.reminder_hour == null)
			return;
		
		Intent contentIntent = new Intent(context, MainActivity.class);
		contentIntent.setData(data);
		PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, 0);
		
		Intent deleteIntent = new Intent(context, ReminderAlarmDismissReceiver.class);
		PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
		
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		inboxStyle.setBigContentTitle("Habit Reminder:");
		List<Habit> pendingHabits = Habit.getHighlightedHabits();
		for(Habit h : pendingHabits)
		{
			if(h.hasRep(DateHelper.getStartOfDay(DateHelper.getLocalTime())))
				continue;
			inboxStyle.addLine(h.name);
			Log.d("Alarm", String.format("Found highlighted: %s", h.name));
		}
		
		String contentText = habit.name;
		if(pendingHabits.size() > 1) {
			contentText = String.format("%d pending habits.", pendingHabits.size());
		}
		
		Notification notification =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_notification)
						.setContentTitle("Habit Reminder")
						.setContentText(contentText)
						.setContentIntent(contentPendingIntent)
						.setDeleteIntent(deletePendingIntent)
						.setSound(soundUri)
						.setStyle(inboxStyle)
						.build();
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Activity.NOTIFICATION_SERVICE);
		
		notificationManager.notify(1, notification);
	}

}
