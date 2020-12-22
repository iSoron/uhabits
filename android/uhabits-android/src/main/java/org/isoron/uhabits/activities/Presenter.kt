/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.activities

import org.isoron.uhabits.core.commands.*

abstract class Presenter<M>(
        val commandRunner: CommandRunner,
) : CommandRunner.Listener {

    private val listeners = mutableListOf<Listener<M>>()
    private var data: M? = null

    fun onResume() {
        commandRunner.addListener(this)
        data = refresh()
        notifyListeners()
    }

    abstract fun refresh(): M

    fun onPause() {
        commandRunner.removeListener(this)
    }

    fun addListener(listener: Listener<M>) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener<M>) {
        listeners.remove(listener)
    }

    fun requestData(listener: Listener<M>) {
        if (data == null) data = refresh()
        listener.onData(data!!)
    }

    override fun onCommandExecuted(command: Command?, refreshKey: Long?) {
        data = refresh()
        notifyListeners()
    }

    private fun notifyListeners() {
        for (l in listeners) l.onData(data!!)
    }

    interface Listener<T> {
        fun onData(data: T)
    }
}