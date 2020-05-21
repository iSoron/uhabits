/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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
package org.isoron.androidbase

import org.isoron.androidbase.activities.BaseActivity

class BaseExceptionHandler(private val activity: BaseActivity) : Thread.UncaughtExceptionHandler {

    private val originalHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread?, ex: Throwable?) {
        ex?.let { ex ->
            try {
                ex.printStackTrace()
                AndroidBugReporter(activity).dumpBugReportToFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }

//        if (ex.cause is InconsistentDatabaseException) {
//            val app = activity.application as HabitsApplication
//            val habits = app.getComponent().getHabitList()
//            habits.repair()
//            System.exit(0)
//        }

            originalHandler?.uncaughtException(thread, ex)
        }
    }
}