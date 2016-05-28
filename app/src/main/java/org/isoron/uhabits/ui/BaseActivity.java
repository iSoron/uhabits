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

package org.isoron.uhabits.ui;

import android.app.backup.BackupManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.isoron.uhabits.HabitBroadcastReceiver;
import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.Command;
import org.isoron.uhabits.commands.CommandRunner;
import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.BaseTask;
import org.isoron.uhabits.utils.ColorUtils;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.isoron.uhabits.widgets.WidgetManager;

abstract public class BaseActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler,
        CommandRunner.Listener
{
    private Toast toast;

    Thread.UncaughtExceptionHandler androidExceptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        InterfaceUtils.applyCurrentTheme(this);

        androidExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        CommandRunner.getInstance().addListener(this);
    }

    @Override
    protected void onPause()
    {
        CommandRunner.getInstance().removeListener(this);
        super.onPause();
    }

    public void showMessage(Integer stringId)
    {
        if (stringId == null) return;
        if (toast == null) toast = Toast.makeText(this, stringId, Toast.LENGTH_SHORT);
        else toast.setText(stringId);
        toast.show();
    }

    protected void setupSupportActionBar(boolean homeButtonEnabled)
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.setElevation(InterfaceUtils.dpToPixels(this, 2));

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
            ((HabitsApplication) getApplication()).dumpBugReportToFile();
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

        if (!InterfaceUtils.getStyledBoolean(this, R.attr.useHabitColorAsPrimary)) return;

        ColorDrawable drawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(drawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            int darkerColor = ColorUtils.mixColors(color, Color.BLACK, 0.75f);
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

    private void dismissNotifications(Context context)
    {
        for(Habit h : Habit.getHabitsWithReminder())
        {
            if(h.checkmarks.getTodayValue() != Checkmark.UNCHECKED)
                HabitBroadcastReceiver.dismissNotification(context, h);
        }
    }

    @Override
    public void onCommandExecuted(Command command, Long refreshKey)
    {
        showMessage(command.getExecuteStringId());
        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                dismissNotifications(BaseActivity.this);
                BackupManager.dataChanged("org.isoron.uhabits");
                WidgetManager.updateWidgets(BaseActivity.this);
            }
        }.execute();
    }
}
