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

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import org.isoron.uhabits.ui.list.ListHabitsFragment;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.ReminderUtils;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.BaseTask;
import org.isoron.uhabits.ui.AboutActivity;
import org.isoron.uhabits.ui.BaseActivity;
import org.isoron.uhabits.ui.IntroActivity;
import org.isoron.uhabits.ui.settings.SettingsActivity;
import org.isoron.uhabits.ui.show.ShowHabitActivity;
import org.isoron.uhabits.widgets.CheckmarkWidgetProvider;
import org.isoron.uhabits.widgets.FrequencyWidgetProvider;
import org.isoron.uhabits.widgets.HistoryWidgetProvider;
import org.isoron.uhabits.widgets.ScoreWidgetProvider;
import org.isoron.uhabits.widgets.StreakWidgetProvider;

import java.io.IOException;

public class MainActivity extends BaseActivity
        implements ListHabitsFragment.OnHabitClickListener
{
    private ListHabitsFragment listHabitsFragment;
    private SharedPreferences prefs;
    private BroadcastReceiver receiver;
    private LocalBroadcastManager localBroadcastManager;

    public static final String ACTION_REFRESH = "org.isoron.uhabits.ACTION_REFRESH";

    public static final int RESULT_IMPORT_DATA = 1;
    public static final int RESULT_EXPORT_CSV = 2;
    public static final int RESULT_EXPORT_DB = 3;
    public static final int RESULT_BUG_REPORT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_habits_activity);

        setupSupportActionBar(false);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listHabitsFragment =
                (ListHabitsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment1);

        receiver = new Receiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(receiver, new IntentFilter(ACTION_REFRESH));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            onPreLollipopStartup();

        onStartup();
    }

    private void onPreLollipopStartup()
    {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) return;
        if(InterfaceUtils.isNightMode()) return;

        int color = getResources().getColor(R.color.grey_900);
        actionBar.setBackgroundDrawable(new ColorDrawable(color));
    }

    private void onStartup()
    {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        InterfaceUtils.incrementLaunchCount(this);
        InterfaceUtils.updateLastAppVersion(this);
        showTutorial();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params)
            {
                ReminderUtils.createReminderAlarms(MainActivity.this);
                updateWidgets(MainActivity.this);
                return null;
            }
        }.execute();

    }

    private void showTutorial()
    {
        Boolean firstRun = prefs.getBoolean("pref_first_run", true);

        if (firstRun)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("pref_first_run", false);
            editor.putLong("last_hint_timestamp", DateUtils.getStartOfToday()).apply();
            editor.apply();

            Intent intent = new Intent(this, IntroActivity.class);
            this.startActivity(intent);
        }
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
            {
                if(InterfaceUtils.isNightMode())
                    InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_LIGHT);
                else
                    InterfaceUtils.setCurrentTheme(InterfaceUtils.THEME_DARK);

                refreshTheme();
                return true;
            }

            case R.id.action_settings:
            {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 0);
                return true;
            }

            case R.id.action_about:
            {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }

            case R.id.action_faq:
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.helpURL)));
                startActivity(intent);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (resultCode)
        {
            case RESULT_IMPORT_DATA:
                listHabitsFragment.showImportDialog();
                break;

            case RESULT_EXPORT_CSV:
                listHabitsFragment.exportAllHabits();
                break;

            case RESULT_EXPORT_DB:
                listHabitsFragment.exportDB();
                break;

            case RESULT_BUG_REPORT:
                generateBugReport();
                break;
        }
    }

    private void generateBugReport()
    {
        try
        {
            HabitsApplication.dumpBugReportToFile();
        }
        catch (IOException e)
        {
            // ignored
        }

        try
        {
            String log = "---------- BUG REPORT BEGINS ----------\n";
            log += HabitsApplication.generateBugReport();
            log += "---------- BUG REPORT ENDS ------------\n";

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "dev@loophabits.org" });
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report - Loop Habit Tracker");
            intent.putExtra(Intent.EXTRA_TEXT, log);
            startActivity(intent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showToast(R.string.bug_report_failed);
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
    public void onPostExecuteCommand(Long refreshKey)
    {
        listHabitsFragment.onPostExecuteCommand(refreshKey);

        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                dismissNotifications(MainActivity.this);
                updateWidgets(MainActivity.this);
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

    public static void updateWidgets(Context context)
    {
        updateWidgets(context, CheckmarkWidgetProvider.class);
        updateWidgets(context, HistoryWidgetProvider.class);
        updateWidgets(context, ScoreWidgetProvider.class);
        updateWidgets(context, StreakWidgetProvider.class);
        updateWidgets(context, FrequencyWidgetProvider.class);
    }

    private static void updateWidgets(Context context, Class providerClass)
    {
        ComponentName provider = new ComponentName(context, providerClass);
        Intent intent = new Intent(context, providerClass);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(provider);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onDestroy()
    {
        localBroadcastManager.unregisterReceiver(receiver);
        super.onDestroy();
    }

    class Receiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            listHabitsFragment.onPostExecuteCommand(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (grantResults.length <= 0) return;
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) return;

        listHabitsFragment.showImportDialog();
    }
}
