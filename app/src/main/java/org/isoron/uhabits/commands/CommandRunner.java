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

import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.tasks.*;

import java.util.*;

import javax.inject.*;

/**
 * A CommandRunner executes and undoes commands.
 * <p>
 * CommandRunners also allows objects to subscribe to it, and receive events
 * whenever a command is performed.
 */
@AppScope
public class CommandRunner
{
    private TaskRunner taskRunner;

    private LinkedList<Listener> listeners;

    @Inject
    public CommandRunner(@NonNull TaskRunner taskRunner)
    {
        this.taskRunner = taskRunner;
        listeners = new LinkedList<>();
    }

    public void addListener(Listener l)
    {
        listeners.add(l);
    }

    public void execute(final Command command, final Long refreshKey)
    {
        taskRunner.execute(new Task()
        {
            @Override
            public void doInBackground()
            {
                command.execute();
            }

            @Override
            public void onPostExecute()
            {
                for (Listener l : listeners)
                    l.onCommandExecuted(command, refreshKey);
            }
        });
    }

    public void removeListener(Listener l)
    {
        listeners.remove(l);
    }

    /**
     * Interface implemented by objects that want to receive an event whenever a
     * command is executed.
     */
    public interface Listener
    {
        void onCommandExecuted(@NonNull Command command,
                               @Nullable Long refreshKey);
    }
}
