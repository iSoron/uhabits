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

import android.os.*;

import java.util.*;
import java.util.concurrent.*;

import javax.inject.*;

@Singleton
public class TaskRunner
{
    private final LinkedList<CustomAsyncTask> activeTasks;

    @Inject
    public TaskRunner()
    {
        activeTasks = new LinkedList<>();
    }

    public void execute(Task task)
    {
        task.onAttached(this);
        new CustomAsyncTask(task).execute();
    }

    public void setCurrentProgress(Task task, int progress)
    {
        for (CustomAsyncTask asyncTask : activeTasks)
            if (asyncTask.getTask() == task) asyncTask.publish(progress);
    }

    public void waitForTasks(long timeout)
        throws TimeoutException, InterruptedException
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            throw new UnsupportedOperationException("waitForTasks requires API 16+");

        int poolInterval = 100;

        while(timeout > 0)
        {
            if(activeTasks.isEmpty()) return;

            timeout -= poolInterval;
            Thread.sleep(poolInterval);
        }

        throw new TimeoutException();
    }

    private class CustomAsyncTask extends AsyncTask<Void, Integer, Void>
    {
        private final Task task;

        public CustomAsyncTask(Task task)
        {
            this.task = task;
        }

        public Task getTask()
        {
            return task;
        }

        public void publish(int progress)
        {
            publishProgress(progress);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            task.doInBackground();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            task.onPostExecute();
            activeTasks.remove(this);
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            task.onProgressUpdate(values[0]);
        }

        @Override
        protected void onPreExecute()
        {
            activeTasks.add(this);
            task.onPreExecute();
        }
    }
}
