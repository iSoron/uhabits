/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.platform.concurrency

/**
 * A TaskRunner provides the ability of running tasks in different queues. The
 * class is also observable, and notifies listeners when new tasks are started
 * or finished.
 *
 * Two queues are available: a foreground queue and a background queue. These
 * two queues may run in parallel, depending on the hardware. Multiple tasks
 * submitted to the same queue, however, always run sequentially, in the order
 * they were enqueued.
 */
interface TaskRunner {

    val listeners: MutableList<Listener>

    val activeTaskCount: Int

    fun runInBackground(task: () -> Unit)

    fun runInForeground(task: () -> Unit)

    interface Listener {
        fun onTaskStarted()
        fun onTaskFinished()
    }
}

/**
 * Sequential implementation of TaskRunner. Both background and foreground
 * queues run in the same thread, so they block each other.
 */
class SequentialTaskRunner : TaskRunner {

    override val listeners = mutableListOf<TaskRunner.Listener>()

    override var activeTaskCount = 0

    override fun runInBackground(task: () -> Unit) {
        activeTaskCount += 1
        for (l in listeners) l.onTaskStarted()
        task()
        activeTaskCount -= 1
        for (l in listeners) l.onTaskFinished()
    }

    override fun runInForeground(task: () -> Unit) = runInBackground(task)
}