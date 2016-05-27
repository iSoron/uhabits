/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.BaseTask;
import org.isoron.uhabits.tasks.ProgressBar;
import org.isoron.uhabits.ui.about.AboutActivity;
import org.isoron.uhabits.ui.AndroidProgressBar;
import org.isoron.uhabits.ui.BaseActivity;
import org.isoron.uhabits.ui.intro.IntroActivity;
import org.isoron.uhabits.ui.habits.list.ListHabitsFragment;
import org.isoron.uhabits.ui.settings.FilePickerDialog;
import org.isoron.uhabits.ui.settings.SettingsActivity;
import org.isoron.uhabits.ui.habits.show.ShowHabitActivity;
import org.isoron.uhabits.utils.FileUtils;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.isoron.uhabits.widgets.WidgetManager;

import java.io.File;

public class MainActivity extends BaseActivity
        implements ListHabitsFragment.OnHabitClickListener, MainController.Screen
{
    private MainController controller;
    private ListHabitsFragment listHabitsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_habits_activity);
        setupSupportActionBar(false);

        FragmentManager fragmentManager = getSupportFragmentManager();
        listHabitsFragment = (ListHabitsFragment) fragmentManager.findFragmentById(R.id.fragment1);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            onPreLollipopCreate();

        controller = new MainController();
        controller.setScreen(this);
        controller.setSystem((HabitsApplication) getApplication());
        controller.onStartup();
    }

    private void onPreLollipopCreate()
    {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) return;
        if(InterfaceUtils.isNightMode()) return;

        int color = getResources().getColor(R.color.grey_900);
        actionBar.setBackgroundDrawable(new ColorDrawable(color));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.clear();
        getMenuInflater().inflate(R.menu.list_habits_menu, menu);
        MenuItem nightModeItem = menu.findItem(R.id.action_night_mode);
        nightModeItem.setChecked(InterfaceUtils.isNightMode());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_night_mode:
                toggleNightMode();
                return true;

            case R.id.action_settings:
                showSettingsScreen();
                return true;

            case R.id.action_about:
                showAboutScreen();
                return true;

            case R.id.action_faq:
                showFAQScreen();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (resultCode)
        {
            case HabitsApplication.RESULT_IMPORT_DATA:
                showImportScreen();
                break;

            case HabitsApplication.RESULT_EXPORT_CSV:
                controller.exportCSV();
                break;

            case HabitsApplication.RESULT_EXPORT_DB:
                controller.exportDB();
                break;

            case HabitsApplication.RESULT_BUG_REPORT:
                controller.sendBugReport();
                break;
        }
    }

    private void showFAQScreen()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(R.string.helpURL)));
        startActivity(intent);
    }

    private void showAboutScreen()
    {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void showSettingsScreen()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 0);
    }

    private void toggleNightMode()
    {
        if(InterfaceUtils.isNightMode())
            InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_LIGHT);
        else
            InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_DARK);

        refreshTheme();
    }

    private void refreshTheme()
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);

                MainActivity.this.finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                startActivity(intent);

            }
        }, 500); // Let the menu disappear first
    }

    @Override
    public void onHabitClicked(Habit habit)
    {
        showHabitScreen(habit);
    }

    @Override
    public void onPostExecuteCommand(Long refreshKey)
    {
        listHabitsFragment.onPostExecuteCommand(refreshKey);

        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                dismissNotifications(MainActivity.this);
                WidgetManager.updateWidgets(MainActivity.this);
            }
        }.execute();
    }

    private void dismissNotifications(Context context)
    {
        for(Habit h : Habit.getHabitsWithReminder())
        {
            if(h.checkmarks.getTodayValue() != Checkmark.UNCHECKED)
                HabitBroadcastReceiver.dismissNotification(context, h);
        }
    }

    private void showHabitScreen(Habit habit)
    {
        Intent intent = new Intent(this, ShowHabitActivity.class);
        intent.setData(Uri.parse("content://org.isoron.uhabits/habit/" + habit.getId()));
        startActivity(intent);
    }

    public void showIntroScreen()
    {
        Intent intent = new Intent(this, IntroActivity.class);
        this.startActivity(intent);
    }

    public void showImportScreen()
    {
        File dir = FileUtils.getFilesDir(null);
        if(dir == null)
        {
            showMessage(R.string.could_not_import);
            return;
        }

        FilePickerDialog picker = new FilePickerDialog(this, dir);
        picker.setListener(new FilePickerDialog.OnFileSelectedListener()
        {
            @Override
            public void onFileSelected(File file)
            {
                controller.importData(file);
            }
        });
        picker.show();
    }

    @Override
    public void refresh(Long refreshKey)
    {
        listHabitsFragment.loader.updateAllHabits(true);
    }

    @Override
    public ProgressBar getProgressBar()
    {
        return new AndroidProgressBar(listHabitsFragment.progressBar);
    }
}
