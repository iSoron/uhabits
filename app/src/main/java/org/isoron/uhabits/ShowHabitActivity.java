package org.isoron.uhabits;

import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.models.Habit;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ShowHabitActivity extends ReplayableActivity
{

    public Habit habit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        habit = Habit.get(ContentUris.parseId(data));
        getActionBar().setTitle(habit.name);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            getActionBar().setBackgroundDrawable(new ColorDrawable(habit.color));
        }

        setContentView(R.layout.show_habit_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.show_habit_activity_menu, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
