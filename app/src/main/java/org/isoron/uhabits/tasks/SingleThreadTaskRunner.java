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

package org.isoron.uhabits.tasks;

import org.isoron.uhabits.*;

import dagger.*;

@Module
public class SingleThreadTaskRunner implements TaskRunner
{
    @Provides
    @AppScope
    public static TaskRunner provideTaskRunner()
    {
        return new SingleThreadTaskRunner();
    }

    @Override
    public void addListener(Listener listener)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Task task)
    {
        task.onAttached(this);
        task.onPreExecute();
        task.doInBackground();
        task.onPostExecute();
    }

    @Override
    public int getActiveTaskCount()
    {
        return 0;
    }

    @Override
    public void publishProgress(Task task, int progress)
    {
        task.onProgressUpdate(progress);
    }

    @Override
    public void removeListener(Listener listener)
    {
        throw new UnsupportedOperationException();
    }
}
