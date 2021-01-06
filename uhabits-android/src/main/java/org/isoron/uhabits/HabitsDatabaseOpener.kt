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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.isoron.uhabits.core.database.MigrationHelper
import org.isoron.uhabits.core.database.UnsupportedDatabaseVersionException
import org.isoron.uhabits.database.AndroidDatabase
import java.io.File

class HabitsDatabaseOpener(
    context: Context,
    private val databaseFilename: String,
    private val version: Int
) : SQLiteOpenHelper(context, databaseFilename, null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.disableWriteAheadLogging()
        db.version = 8
        onUpgrade(db, -1, version)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.disableWriteAheadLogging()
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.disableWriteAheadLogging()
        if (db.version < 8) throw UnsupportedDatabaseVersionException()
        val helper = MigrationHelper(AndroidDatabase(db, File(databaseFilename)))
        helper.migrateTo(newVersion)
    }

    override fun onDowngrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        throw UnsupportedDatabaseVersionException()
    }
}
