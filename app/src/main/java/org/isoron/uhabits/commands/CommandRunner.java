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

package org.isoron.uhabits.commands;

import org.isoron.uhabits.tasks.BaseTask;

import java.util.LinkedList;

public class CommandRunner
{
    public interface Listener
    {
        void onCommandExecuted(Command command, Long refreshKey);
    }

    private static CommandRunner singleton;
    private LinkedList<Listener> listeners;

    private CommandRunner()
    {
        listeners = new LinkedList<>();
    }

    public static CommandRunner getInstance()
    {
        if(singleton == null) singleton = new CommandRunner();
        return singleton;
    }

    public void execute(final Command command, final Long refreshKey)
    {
        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                command.execute();
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                for(Listener l : listeners)
                    l.onCommandExecuted(command, refreshKey);
            }
        }.execute();
    }

    public void addListener(Listener l)
    {
        listeners.add(l);
    }

    public void removeListener(Listener l)
    {
        listeners.remove(l);
    }
}
