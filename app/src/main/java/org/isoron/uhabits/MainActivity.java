package org.isoron.uhabits;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.isoron.helpers.Command;
import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.dialogs.ListHabitsFragment;
import org.isoron.uhabits.models.Habit;

public class MainActivity extends ReplayableActivity
        implements ListHabitsFragment.OnHabitClickListener
{
    private ListHabitsFragment listHabitsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_habits_activity);

        listHabitsFragment = (ListHabitsFragment) getFragmentManager().findFragmentById(
                R.id.fragment1);

        ReminderHelper.createReminderAlarms(MainActivity.this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        listHabitsFragment.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.list_habits_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_settings:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onHabitClicked(Habit habit)
    {
        Intent intent = new Intent(this, ShowHabitActivity.class);
        intent.setData(Uri.parse("content://org.isoron.uhabits/habit/" + habit.getId()));
        startActivity(intent);
    }

    @Override
    public void executeCommand(Command command)
    {
        super.executeCommand(command);
        listHabitsFragment.notifyDataSetChanged();
    }
}
