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

package org.isoron.uhabits.activities.habits.show

import org.isoron.androidbase.activities.*
import org.isoron.uhabits.core.models.*
import javax.inject.*

@ActivityScope
class ShowHabitPresenter
@Inject constructor(
        val habit: Habit,
) {
    private val listeners = mutableListOf<Listener>()

    private fun build() = ShowHabitViewModel(
            title = habit.name,
            isNumerical = habit.isNumerical,
    )

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun requestData(listener: Listener) {
        listener.onData(build())
    }

    interface Listener {
        fun onData(data: ShowHabitViewModel)
    }
}