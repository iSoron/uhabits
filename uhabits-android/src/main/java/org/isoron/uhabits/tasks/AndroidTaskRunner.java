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

import org.isoron.uhabits.*;

import java.util.*;

import dagger.*;

@Module
public class AndroidTaskRunner implements TaskRunner
{
    private final LinkedList<CustomAsyncTask> activeTasks;

    private final HashMap<Task, CustomAsyncTask> taskToAsyncTask;

    private LinkedList<Listener> listeners;

    public AndroidTaskRunner()
    {
        activeTasks = new LinkedList<>();
        taskToAsyncTask = new HashMap<>();
        listeners = new LinkedList<>();
    }

    @Provides
    @AppScope
    public static TaskRunner provideTaskRunner()
    {
        return new AndroidTaskRunner();
    }

    @Override
    public void addListener(Listener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void execute(Task task)
    {
        task.onAttached(this);
        new CustomAsyncTask(task).execute();
    }

    @Override
    public int getActiveTaskCount()
    {
        return activeTasks.size();
    }

    @Override
    public void publishProgress(Task task, int progress)
    {
        CustomAsyncTask asyncTask = taskToAsyncTask.get(task);
        if (asyncTask == null) return;
        asyncTask.publish(progress);
    }

    @Override
    public void removeListener(Listener listener)
    {
        listeners.remove(listener);
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
            taskToAsyncTask.remove(task);
            for (Listener l : listeners) l.onTaskFinished(task);
        }

        @Override
        protected void onPreExecute()
        {
            for (Listener l : listeners) l.onTaskStarted(task);
            activeTasks.add(this);
            taskToAsyncTask.put(task, this);
            task.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            task.onProgressUpdate(values[0]);
        }
    }
}
