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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import org.isoron.uhabits.commands.Command;
import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.UIHelper;

import java.util.LinkedList;

abstract public class BaseActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler
{
    private static int MAX_UNDO_LEVEL = 15;

    private LinkedList<Command> undoList;
    private LinkedList<Command> redoList;
    private Toast toast;

    Thread.UncaughtExceptionHandler androidExceptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        UIHelper.applyCurrentTheme(this);

        androidExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        undoList = new LinkedList<>();
        redoList = new LinkedList<>();
    }

    public void executeCommand(Command command, Long refreshKey)
    {
        executeCommand(command, false, refreshKey);
    }

    protected void undo()
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
    }

    protected void redo()
    {
        if (redoList.isEmpty())
        {
            showToast(R.string.toast_nothing_to_redo);
            return;
        }
        Command last = redoList.pop();
        executeCommand(last, false, null);
    }

    public void showToast(Integer stringId)
    {
        if (stringId == null) return;
        if (toast == null) toast = Toast.makeText(this, stringId, Toast.LENGTH_SHORT);
        else toast.setText(stringId);
        toast.show();
    }

    public void executeCommand(final Command command, Boolean clearRedoStack, final Long refreshKey)
    {
        undoList.push(command);

        if (undoList.size() > MAX_UNDO_LEVEL) undoList.removeLast();
        if (clearRedoStack) redoList.clear();

        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                command.execute();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                BaseActivity.this.onPostExecuteCommand(refreshKey);
                BackupManager.dataChanged("org.isoron.uhabits");
            }
        }.execute();


        showToast(command.getExecuteStringId());
    }

    protected void setupSupportActionBar(boolean homeButtonEnabled)
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.setElevation(UIHelper.dpToPixels(this, 6));

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) return;

        if(homeButtonEnabled)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void onPostExecuteCommand(Long refreshKey)
    {
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
}
