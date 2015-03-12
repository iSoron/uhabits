package org.isoron.uhabits;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver
{
	static int k = 1;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		createNotification(context, intent.getData(), intent.getDataString());
	}
	
	
	private void createNotification(Context context, Uri data, String text)
	{
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.setData(data);
		
		PendingIntent notificationIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
		
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		Notification notification =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_notification)
						.setContentTitle("Habit Reminder")
						.setContentText(text)
						.setContentIntent(notificationIntent)
						.setSound(soundUri)
						.build();
		
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
		notificationManager.notify(k++, notification);
	}

}
