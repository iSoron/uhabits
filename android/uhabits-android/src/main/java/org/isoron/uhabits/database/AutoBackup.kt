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

package org.isoron.uhabits.database

import android.content.*
import android.util.*
import org.isoron.androidbase.*
import org.isoron.uhabits.core.utils.*
import org.isoron.uhabits.utils.*
import java.io.*

class AutoBackup(private val context: Context) {

    private val basedir = AndroidDirFinder(context).getFilesDir("Backups")!!

    fun run(keep: Int = 5) {
        val files = listBackupFiles()
        var newestTimestamp = 0L;
        if (files.isNotEmpty()) {
            newestTimestamp = files.last().lastModified()
        }
        val todayTimestamp = DateUtils.getStartOfToday()
        removeOldest(files, keep)
        if (todayTimestamp - newestTimestamp > DateUtils.DAY_LENGTH) {
            DatabaseUtils.saveDatabaseCopy(context, basedir)
        }
    }

    private fun removeOldest(files: ArrayList<File>, keep: Int) {
        files.sortBy { -it.lastModified() }
        for (k in keep until files.size) {
            Log.i("AutoBackup", "Removing ${files[k]}")
            files[k].delete()
        }
    }

    private fun listBackupFiles(): ArrayList<File> {
        val files = ArrayList<File>()
        for (path in basedir.list()!!) {
            files.add(File("${basedir.path}/$path"))
        }
        return files
    }
}