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
package org.isoron.uhabits.core.database

import java.sql.ResultSet
import java.sql.SQLException

class JdbcCursor(private val resultSet: ResultSet) : Cursor {
    override fun close() {
        try {
            resultSet.close()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun moveToNext(): Boolean {
        return try {
            resultSet.next()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun getInt(index: Int): Int? {
        return try {
            val value = resultSet.getInt(index + 1)
            if (resultSet.wasNull()) null else value
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun getLong(index: Int): Long? {
        return try {
            val value = resultSet.getLong(index + 1)
            if (resultSet.wasNull()) null else value
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun getDouble(index: Int): Double? {
        return try {
            val value = resultSet.getDouble(index + 1)
            if (resultSet.wasNull()) null else value
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun getString(index: Int): String? {
        return try {
            val value = resultSet.getString(index + 1)
            if (resultSet.wasNull()) null else value
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }
}
