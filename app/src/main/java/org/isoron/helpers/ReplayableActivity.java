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

package org.isoron.helpers;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import org.isoron.uhabits.R;

import java.util.LinkedList;

abstract public class ReplayableActivity extends Activity
{
    private static int MAX_UNDO_LEVEL = 15;

    private LinkedList<Command> undoList;
    private LinkedList<Command> redoList;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
                ReplayableActivity.this.onPostExecuteCommand(refreshKey);
                BackupManager.dataChanged("org.isoron.uhabits");
            }
        }.execute();


        showToast(command.getExecuteStringId());
    }

    public void onPostExecuteCommand(Long refreshKey)
    {
    }
}
