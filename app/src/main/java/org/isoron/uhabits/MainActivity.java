package org.isoron.uhabits;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        listHabitsFragment = (ListHabitsFragment) getFragmentManager().findFragmentById(
                R.id.fragment1);

        ReminderHelper.createReminderAlarms(MainActivity.this);

        showTutorial();
    }

    private void showTutorial()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean firstRun = prefs.getBoolean("pref_first_run", true);

        if(firstRun)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("pref_first_run", false);
            editor.apply();

            Intent intent = new Intent(this, IntroActivity.class);
            this.startActivity(intent);
        }
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
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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
