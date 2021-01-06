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
package org.isoron.uhabits

import android.app.Activity

class BaseExceptionHandler(private val activity: Activity) : Thread.UncaughtExceptionHandler {

    private val originalHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread?, ex: Throwable?) {
        if (ex == null) return
        if (thread == null) return
        try {
            ex.printStackTrace()
            AndroidBugReporter(activity).dumpBugReportToFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        originalHandler?.uncaughtException(thread, ex)
    }
}
