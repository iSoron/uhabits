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

package org.isoron.platform.io

import kotlinx.cinterop.*
import sqlite3.*

fun sqlite3_errstr(db: CPointer<sqlite3>): String {
    return "SQLite3 error: " + sqlite3_errmsg(db).toString()
}

class IosDatabaseOpener() : DatabaseOpener {
    override fun open(file: UserFile): Database = memScoped {
        val db = alloc<CPointerVar<sqlite3>>()
        val path = (file as IosFile).path
        if (sqlite3_open(path, db.ptr) != SQLITE_OK) {
            throw Exception(sqlite3_errstr(db.value!!))
        }
        return IosDatabase(db.value!!)
    }
}

class IosDatabase(val db: CPointer<sqlite3>) : Database {
    override fun prepareStatement(sql: String): PreparedStatement = memScoped {
        if (sql.isEmpty()) throw Exception("empty SQL query")
        val stmt = alloc<CPointerVar<sqlite3_stmt>>()
        if (sqlite3_prepare_v2(db, sql.cstr, -1, stmt.ptr, null) != SQLITE_OK) {
            throw Exception(sqlite3_errstr(db))
        }
        return IosPreparedStatement(db, stmt.value!!)
    }
    override fun close() {
        sqlite3_close(db)
    }
}

class IosPreparedStatement(val db: CPointer<sqlite3>,
                           val stmt: CPointer<sqlite3_stmt>) : PreparedStatement {
    override fun step(): StepResult {
        val result = sqlite3_step(stmt)
        when (result) {
            SQLITE_ROW -> return StepResult.ROW
            SQLITE_DONE -> return StepResult.DONE
            else -> throw Exception(sqlite3_errstr(db))
        }
    }

    override fun finalize() {
        sqlite3_finalize(stmt)
    }

    override fun getInt(index: Int): Int {
        return sqlite3_column_int(stmt, index)
    }

    override fun getLong(index: Int): Long {
        return sqlite3_column_int64(stmt, index)
    }

    override fun getText(index: Int): String {
        return sqlite3_column_text(stmt, index)!!
                .reinterpret<ByteVar>()
                .toKString()
    }

    override fun getReal(index: Int): Double {
        return sqlite3_column_double(stmt, index)
    }

    override fun bindInt(index: Int, value: Int) {
        sqlite3_bind_int(stmt, index + 1, value)
    }

    override fun bindLong(index: Int, value: Long) {
        sqlite3_bind_int64(stmt, index + 1, value)
    }

    override fun bindText(index: Int, value: String) {
        sqlite3_bind_text(stmt, index + 1, value, -1, SQLITE_TRANSIENT)
    }

    override fun bindReal(index: Int, value: Double) {
        sqlite3_bind_double(stmt, index + 1, value)
    }

    override fun reset() {
        sqlite3_reset(stmt)
    }
}