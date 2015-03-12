package org.isoron.uhabits;

import java.util.LinkedList;

import org.isoron.helpers.Command;
import org.isoron.uhabits.dialogs.ShowHabitsFragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity
{

	private ShowHabitsFragment showHabitsFragment;

	private LinkedList<Command> undoList;
	private LinkedList<Command> redoList;

	private static int MAX_UNDO_LEVEL = 15;

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                   Creation                                                  *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//		Habit.rebuildOrder();
		//		Habit.roundTimestamps();
		setContentView(R.layout.main_activity);
		showHabitsFragment = (ShowHabitsFragment) getFragmentManager().findFragmentById(
				R.id.fragment1);

		undoList = new LinkedList<Command>();
		redoList = new LinkedList<Command>();
		
//		startAlarm("http://hello-world.com/", 5);
//		startAlarm("http://ola-mundo.com.br/", 10);
	}
	
	private void startAlarm(String data, int interval)
	{
		Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
		alarmIntent.setData(Uri.parse(data));
		
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                interval * 1000, pendingIntent);
        
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                 Action Bar Menu                                             *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if(id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                               Commands, Undo, Redo                                          *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public void executeCommand(Command command, boolean datasetChanged)
	{
		executeCommand(command, datasetChanged, true);
	}

	public void executeCommand(Command command, boolean datasetChanged, boolean clearRedoStack)
	{
		undoList.push(command);
		if(undoList.size() > MAX_UNDO_LEVEL)
			undoList.removeLast();
		if(clearRedoStack)
			redoList.clear();
		command.execute();

		showToast(command.getExecuteStringId());
		if(datasetChanged)
		{
			showHabitsFragment.notifyDataSetChanged();
		}
	}

	public void undo()
	{
		if(undoList.isEmpty())
		{
			showToast(R.string.toast_nothing_to_undo);
			return;
		}

		Command last = undoList.pop();
		redoList.push(last);
		last.undo();
		showToast(last.getUndoStringId());

		showHabitsFragment.notifyDataSetChanged();
	}

	public void redo()
	{
		if(redoList.isEmpty())
		{
			showToast(R.string.toast_nothing_to_redo);
			return;
		}
		Command last = redoList.pop();
		executeCommand(last, true, false);
	}

	private Toast toast;

	private void showToast(Integer stringId)
	{
		if(stringId == null)
			return;
		if(toast == null)
			toast = Toast.makeText(this, stringId, Toast.LENGTH_SHORT);
		else
			toast.setText(stringId);
		toast.show();
	}
}
