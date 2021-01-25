/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.tasks

import android.os.AsyncTask
import dagger.Module
import dagger.Provides
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.tasks.Task
import org.isoron.uhabits.core.tasks.TaskRunner
import java.util.HashMap
import java.util.LinkedList

// TODO: @Module not needed?
@Module
class AndroidTaskRunner : TaskRunner {
    private val activeTasks: LinkedList<CustomAsyncTask> = LinkedList()
    private val taskToAsyncTask: HashMap<Task, CustomAsyncTask> = HashMap()
    private val listeners: LinkedList<TaskRunner.Listener> = LinkedList<TaskRunner.Listener>()
    override fun addListener(listener: TaskRunner.Listener) {
        listeners.add(listener)
    }

    override fun execute(task: Task) {
        task.onAttached(this)
        CustomAsyncTask(task).execute()
    }

    override val activeTaskCount: Int
        get() = activeTasks.size

    override fun publishProgress(task: Task, progress: Int) {
        val asyncTask = taskToAsyncTask[task] ?: return
        asyncTask.publish(progress)
    }

    override fun removeListener(listener: TaskRunner.Listener) {
        listeners.remove(listener)
    }

    private inner class CustomAsyncTask(val task: Task) : AsyncTask<Void?, Int?, Void?>() {

        fun publish(progress: Int) {
            publishProgress(progress)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            if (isCancelled) return null
            task.doInBackground()
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            if (isCancelled) return
            task.onPostExecute()
            activeTasks.remove(this)
            taskToAsyncTask.remove(task)
            for (l in listeners) l.onTaskFinished(task)
        }

        override fun onPreExecute() {
            if (isCancelled) return
            for (l in listeners) l.onTaskStarted(task)
            activeTasks.add(this)
            taskToAsyncTask[task] = this
            task.onPreExecute()
        }

        override fun onProgressUpdate(vararg values: Int?) {
            values[0]?.let { task.onProgressUpdate(it) }
        }
    }

    @Module
    companion object {
        @JvmStatic
        @Provides
        @AppScope
        fun provideTaskRunner(): TaskRunner {
            return AndroidTaskRunner()
        }
    }
}
