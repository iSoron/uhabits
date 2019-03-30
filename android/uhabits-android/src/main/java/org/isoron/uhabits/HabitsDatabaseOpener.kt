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
 *
 *
 */

package org.isoron.uhabits

import android.content.*
import android.database.sqlite.*

import org.isoron.uhabits.core.database.*
import org.isoron.uhabits.database.*

class HabitsDatabaseOpener(
        context: Context,
        databaseFilename: String,
        private val version: Int
) : SQLiteOpenHelper(context, databaseFilename, null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.version = 8
        onUpgrade(db, -1, version)
    }

    override fun onUpgrade(db: SQLiteDatabase,
                           oldVersion: Int,
                           newVersion: Int) {
        if (db.version < 8) throw UnsupportedDatabaseVersionException()
        val helper = MigrationHelper(AndroidDatabase(db))
        helper.migrateTo(newVersion)
    }

    override fun onDowngrade(db: SQLiteDatabase,
                             oldVersion: Int,
                             newVersion: Int) {
        throw UnsupportedDatabaseVersionException()
    }
}
