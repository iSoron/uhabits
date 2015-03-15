package org.isoron.uhabits;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.isoron.helpers.Command;
import org.isoron.uhabits.dialogs.ListHabitsFragment;
import org.isoron.uhabits.models.Habit;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class MainActivity extends Activity
{
    private static int MAX_UNDO_LEVEL = 15;
    private ListHabitsFragment listHabitsFragment;
    private LinkedList<Command> undoList;
    private LinkedList<Command> redoList;

    private Toast toast;

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
            calendar.set(Calendar.HOUR_OF_DAY, habit.reminder_hour);
            calendar.set(Calendar.MINUTE, habit.reminder_min);
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

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                ((int) (habit.getId() % Integer.MAX_VALUE)) + 1,
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 19)
        {
            manager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        } else
        {
            manager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }

        Log.d("Alarm", String.format("Setting alarm (%s): %s", DateFormat.getDateTimeInstance()
                .format(new Date(reminderTime)), habit.name));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getActionBar().setElevation(5);

        setContentView(R.layout.list_habits_activity);

        listHabitsFragment = (ListHabitsFragment) getFragmentManager().findFragmentById(
                R.id.fragment1);

        Log.d("MainActivity", "Creating activity");

        undoList = new LinkedList<>();
        redoList = new LinkedList<>();

        createReminderAlarms(MainActivity.this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        listHabitsFragment.notifyDataSetChanged();
        Log.d("MainActivity", "Starting activity");
    }

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
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void executeCommand(Command command, boolean datasetChanged)
    {
        executeCommand(command, datasetChanged, true);
    }

    public void executeCommand(Command command, boolean datasetChanged, boolean clearRedoStack)
    {
        undoList.push(command);
        if (undoList.size() > MAX_UNDO_LEVEL)
            undoList.removeLast();
        if (clearRedoStack)
            redoList.clear();
        command.execute();

        showToast(command.getExecuteStringId());
        if (datasetChanged)
        {
            listHabitsFragment.notifyDataSetChanged();
        }
    }

    public void undo()
    {
        if (undoList.isEmpty())
        {
            showToast(R.string.toast_nothing_to_undo);
            return;
        }

        Command last = undoList.pop();
        redoList.push(last);
        last.undo();
        showToast(last.getUndoStringId());

        listHabitsFragment.notifyDataSetChanged();
    }

    public void redo()
    {
        if (redoList.isEmpty())
        {
            showToast(R.string.toast_nothing_to_redo);
            return;
        }
        Command last = redoList.pop();
        executeCommand(last, true, false);
    }

    private void showToast(Integer stringId)
    {
        if (stringId == null)
            return;
        if (toast == null)
            toast = Toast.makeText(this, stringId, Toast.LENGTH_SHORT);
        else
            toast.setText(stringId);
        toast.show();
    }
}
