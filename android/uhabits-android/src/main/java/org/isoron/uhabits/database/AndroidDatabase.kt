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

package org.isoron.uhabits.database

import android.content.*
import android.database.sqlite.*
import org.isoron.uhabits.core.database.*

class AndroidDatabase(private val db: SQLiteDatabase) : Database {

    override fun beginTransaction() = db.beginTransaction()
    override fun setTransactionSuccessful() = db.setTransactionSuccessful()
    override fun endTransaction() = db.endTransaction()
    override fun close() = db.close()
    override fun getVersion() = db.version

    override fun query(query: String, vararg params: String)
            = AndroidCursor(db.rawQuery(query, params))

    override fun execute(query: String, vararg params: Any)
            = db.execSQL(query, params)

    override fun update(tableName: String,
                        map: Map<String, Any?>,
                        where: String,
                        vararg params: String): Int {
        val values = mapToContentValues(map)
        return db.update(tableName, values, where, params)
    }

    override fun insert(tableName: String, map: Map<String, Any?>): Long? {
        val values = mapToContentValues(map)
        return db.insert(tableName, null, values)
    }

    override fun delete(tableName: String,
                        where: String,
                        vararg params: String) {
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
                else -> throw IllegalStateException(
                        "unsupported type: " + value)
            }
        }
        return values
    }
}
