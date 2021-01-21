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

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.isoron.uhabits.core.database.Database
import java.io.File

class AndroidDatabase(
    private val db: SQLiteDatabase,
    override val file: File?,
) : Database {

    override fun beginTransaction() = db.beginTransaction()
    override fun setTransactionSuccessful() = db.setTransactionSuccessful()
    override fun endTransaction() = db.endTransaction()
    override fun close() = db.close()

    override val version: Int
        get() = db.version

    override fun query(q: String, vararg params: String) = AndroidCursor(db.rawQuery(q, params))

    override fun execute(query: String, vararg params: Any) = db.execSQL(query, params)

    override fun update(
        tableName: String,
        values: Map<String, Any?>,
        where: String,
        vararg params: String,
    ): Int {
        val contValues = mapToContentValues(values)
        return db.update(tableName, contValues, where, params)
    }

    override fun insert(tableName: String, values: Map<String, Any?>): Long {
        val contValues = mapToContentValues(values)
        return db.insert(tableName, null, contValues)
    }

    override fun delete(
        tableName: String,
        where: String,
        vararg params: String,
    ) {
        db.delete(tableName, where, params)
    }

    private fun mapToContentValues(map: Map<String, Any?>): ContentValues {
        val values = ContentValues()
        for ((key, value) in map) {
            when (value) {
                null -> values.putNull(key)
                is Int -> values.put(key, value)
                is Long -> values.put(key, value)
                is Double -> values.put(key, value)
                is String -> values.put(key, value)
                else -> throw IllegalStateException("unsupported type: $value")
            }
        }
        return values
    }
}
