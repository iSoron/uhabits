package org.isoron.uhabits;

import org.isoron.uhabits.models.Habit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class ReminderAlarmDismissReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		createNotification(context, intent.getData(), intent.getDataString());
	}
	
	
	private void createNotification(Context context, Uri data, String text)
	{
		for(Habit h : Habit.getHighlightedHabits())
		{
			Log.d("Alarm", String.format("Removing highlight from: %s", h.name)); 
			h.highlight = 0;
			h.save();
		}
	}

}
