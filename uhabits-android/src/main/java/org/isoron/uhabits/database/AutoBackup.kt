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

package org.isoron.uhabits.database

import android.content.Context
import android.util.Log
import org.isoron.uhabits.AndroidDirFinder
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.utils.DatabaseUtils
import java.io.File

class AutoBackup(private val context: Context) {

    private val basedir = AndroidDirFinder(context).getFilesDir("Backups")!!

    fun run(keep: Int = 5) {
        Log.i("AutoBackup", "Starting automatic backups...")
        val files = listBackupFiles()
        var newestTimestamp = 0L
        if (files.isNotEmpty()) {
            newestTimestamp = files.last().lastModified()
        }
        val now = DateUtils.getLocalTime()
        removeOldest(files, keep)
        if (now - newestTimestamp > DateUtils.DAY_LENGTH) {
            DatabaseUtils.saveDatabaseCopy(context, basedir)
        } else {
            Log.i("AutoBackup", "Fresh backup found (timestamp=$newestTimestamp)")
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
