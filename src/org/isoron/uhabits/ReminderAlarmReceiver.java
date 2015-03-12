package org.isoron.uhabits;

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
		
		// Check if reminder has been turned off after alarm was scheduled 
		if(habit.reminder_hour == null)
			return;
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.setData(data);
		
		PendingIntent notificationIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
		
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		Notification notification =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_notification)
						.setContentTitle("Habit Reminder")
						.setContentText(habit.name)
						.setContentIntent(notificationIntent)
						.setSound(soundUri)
						.build();
		
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Activity.NOTIFICATION_SERVICE);
		
		notificationManager.notify(0, notification);
	}

}
