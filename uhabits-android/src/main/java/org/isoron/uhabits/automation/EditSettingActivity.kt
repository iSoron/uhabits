/*
 * Copyright (C) 2015-2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.automation

import android.os.*

import org.isoron.androidbase.activities.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*

class EditSettingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = applicationContext as HabitsApplication
        val habits = app.component.habitList.getFiltered(
                HabitMatcherBuilder()
                        .setArchivedAllowed(false)
                        .setCompletedAllowed(true)
                        .build())

        val controller = EditSettingController(this)
        val rootView = EditSettingRootView(this, habits, controller)
        val screen = BaseScreen(this)
        screen.setRootView(rootView)
        setScreen(screen)
    }
}
