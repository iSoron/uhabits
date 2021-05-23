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
package org.isoron.uhabits.core.tasks

import java.util.LinkedList

class SingleThreadTaskRunner : TaskRunner {
    override val activeTaskCount: Int
        get() = 0

    private val listeners: MutableList<TaskRunner.Listener> = LinkedList()
    override fun addListener(listener: TaskRunner.Listener) {
        listeners.add(listener)
    }

    override fun execute(task: Task) {
        for (l in listeners) l.onTaskStarted(task)
        if (!task.isCanceled()) {
            task.onAttached(this)
            task.onPreExecute()
            task.doInBackground()
            task.onPostExecute()
        }
        for (l in listeners) l.onTaskFinished(task)
    }

    override fun publishProgress(task: Task, progress: Int) {
        task.onProgressUpdate(progress)
    }

    override fun removeListener(listener: TaskRunner.Listener) {
        listeners.remove(listener)
    }
}
