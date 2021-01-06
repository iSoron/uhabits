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

import org.apache.commons.lang3.StringUtils
import java.io.File
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Types
import java.util.ArrayList

class JdbcDatabase(private val connection: Connection) : Database {
    private var transactionSuccessful = false
    override fun query(q: String, vararg params: String): Cursor {
        return try {
            val st = buildStatement(q, params)
            JdbcCursor(st.executeQuery())
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun update(
        tableName: String,
        values: Map<String, Any?>,
        where: String,
        vararg params: String,
    ): Int {
        return try {
            val fields = ArrayList<String?>()
            val valuesStr = ArrayList<String>()
            for ((key, value) in values) {
                fields.add("$key=?")
                valuesStr.add(value.toString())
            }
            valuesStr.addAll(listOf(*params))
            val query = String.format(
                "update %s set %s where %s",
                tableName,
                StringUtils.join(fields, ", "),
                where
            )
            val st = buildStatement(query, valuesStr.toTypedArray())
            st.executeUpdate()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun insert(tableName: String, values: Map<String, Any?>): Long? {
        return try {
            val fields = ArrayList<String?>()
            val params = ArrayList<Any?>()
            val questionMarks = ArrayList<String?>()
            for ((key, value) in values) {
                fields.add(key)
                params.add(value)
                questionMarks.add("?")
            }
            val query = String.format(
                "insert into %s(%s) values(%s)",
                tableName,
                StringUtils.join(fields, ", "),
                StringUtils.join(questionMarks, ", ")
            )
            val st = buildStatement(query, params.toTypedArray())
            st.execute()
            var id: Long? = null
            val keys = st.generatedKeys
            if (keys.next()) id = keys.getLong(1)
            id
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun delete(tableName: String, where: String, vararg params: String) {
        val query = String.format("delete from %s where %s", tableName, where)
        execute(query, *params)
    }

    override fun execute(query: String, vararg params: Any) {
        try {
            buildStatement(query, params).execute()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    private fun buildStatement(query: String, params: Array<out Any?>): PreparedStatement {
        val st = connection.prepareStatement(query)
        var index = 1
        for (param in params) {
            when (param) {
                null -> st.setNull(index++, Types.INTEGER)
                is Int -> st.setInt(index++, param)
                is Double -> st.setDouble(index++, param)
                is String -> st.setString(index++, param)
                is Long -> st.setLong(index++, param)
                else -> throw IllegalArgumentException()
            }
        }
        return st
    }

    @Synchronized
    override fun beginTransaction() {
        try {
            connection.autoCommit = false
            transactionSuccessful = false
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    @Synchronized
    override fun setTransactionSuccessful() {
        transactionSuccessful = true
    }

    @Synchronized
    override fun endTransaction() {
        try {
            if (transactionSuccessful) connection.commit() else connection.rollback()
            connection.autoCommit = true
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override fun close() {
        try {
            connection.close()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    override val version: Int
        get() {
            query("PRAGMA user_version").use { c ->
                c.moveToNext()
                return c.getInt(0)!!
            }
        }

    override val file: File?
        get() = null
}
