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

import android.app.backup.BackupManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.isoron.uhabits.commands.Command;
import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.UIHelper;
import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.BaseTask;
import org.isoron.uhabits.widgets.CheckmarkWidgetProvider;
import org.isoron.uhabits.widgets.FrequencyWidgetProvider;
import org.isoron.uhabits.widgets.HistoryWidgetProvider;
import org.isoron.uhabits.widgets.ScoreWidgetProvider;
import org.isoron.uhabits.widgets.StreakWidgetProvider;

abstract public class BaseActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler
{
    private Toast toast;
    private SyncManager sync;

    Thread.UncaughtExceptionHandler androidExceptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        UIHelper.applyCurrentTheme(this);

        androidExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        sync = new SyncManager(this);
    }

    public void showToast(Integer stringId)
    {
        if (stringId == null) return;
        if (toast == null) toast = Toast.makeText(this, stringId, Toast.LENGTH_SHORT);
        else toast.setText(stringId);
        toast.show();
    }

    public void executeCommand(final Command command, final Long refreshKey)
    {
        executeCommand(command, refreshKey, true);
    }

    public void executeCommand(final Command command, final Long refreshKey,
                               final boolean shouldBroadcast)
    {
        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                Log.d("BaseActivity", "Executing command");
                command.execute();
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                BaseActivity.this.onPostExecuteCommand(command, refreshKey);
                BackupManager.dataChanged("org.isoron.uhabits");
                if(shouldBroadcast) sync.postCommand(command);
            }
        }.execute();


        showToast(command.getExecuteStringId());
    }

    public void onPostExecuteCommand(Command command, Long refreshKey)
    {
        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                dismissNotifications(BaseActivity.this);
                updateWidgets(BaseActivity.this);
            }
        }.execute();
    }

    protected void setupSupportActionBar(boolean homeButtonEnabled)
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.setElevation(UIHelper.dpToPixels(this, 2));

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) return;

        if(homeButtonEnabled)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
        try
        {
            ex.printStackTrace();
            HabitsApplication.generateLogFile();
        }
        catch(Exception e)
        {
            // ignored
        }

        if(androidExceptionHandler != null)
            androidExceptionHandler.uncaughtException(thread, ex);
        else
            System.exit(1);
    }

    protected void setupActionBarColor(int color)
    {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) return;

        if (!UIHelper.getStyledBoolean(this, R.attr.useHabitColorAsPrimary)) return;

        ColorDrawable drawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(drawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            int darkerColor = ColorHelper.mixColors(color, Color.BLACK, 0.75f);
            getWindow().setStatusBarColor(darkerColor);
        }
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            hideFakeToolbarShadow();
    }

    protected void hideFakeToolbarShadow()
    {
        View view = findViewById(R.id.toolbarShadow);
        if(view != null) view.setVisibility(View.GONE);

        view = findViewById(R.id.headerShadow);
        if(view != null) view.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy()
    {
        sync.close();
        super.onDestroy();
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
}
