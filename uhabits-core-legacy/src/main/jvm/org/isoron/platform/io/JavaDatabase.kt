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

import java.sql.*
import java.sql.PreparedStatement

class JavaPreparedStatement(private var stmt: PreparedStatement) : org.isoron.platform.io.PreparedStatement {
    private var rs: ResultSet? = null

    private var hasExecuted = false

    override fun step(): StepResult {
        if (!hasExecuted) {
            hasExecuted = true
            val hasResult = stmt.execute()
            if (hasResult) rs = stmt.resultSet
        }

        if (rs == null || !rs!!.next()) return StepResult.DONE
        return StepResult.ROW
    }

    override fun finalize() {
        stmt.close()
    }

    override fun getInt(index: Int): Int {
        return rs!!.getInt(index + 1)
    }

    override fun getLong(index: Int): Long {
        return rs!!.getLong(index + 1)
    }

    override fun getText(index: Int): String {
        return rs!!.getString(index + 1)
    }

    override fun getReal(index: Int): Double {
        return rs!!.getDouble(index + 1)
    }

    override fun bindInt(index: Int, value: Int) {
        stmt.setInt(index + 1, value)
    }

    override fun bindLong(index: Int, value: Long) {
        stmt.setLong(index + 1, value)
    }

    override fun bindText(index: Int, value: String) {
        stmt.setString(index + 1, value)
    }

    override fun bindReal(index: Int, value: Double) {
        stmt.setDouble(index + 1, value)
    }

    override fun reset() {
        stmt.clearParameters()
        hasExecuted = false
    }
}

class JavaDatabase(private var conn: Connection,
                   private val log: Log) : Database {

    override fun prepareStatement(sql: String): org.isoron.platform.io.PreparedStatement {
        return JavaPreparedStatement(conn.prepareStatement(sql))
    }

    override fun close() {
        conn.close()
    }
}

class JavaDatabaseOpener(val log: Log) : DatabaseOpener {
    override fun open(file: UserFile): Database {
        val platformFile = file as JavaUserFile
        val conn = DriverManager.getConnection("jdbc:sqlite:${platformFile.path}")
        return JavaDatabase(conn, log)
    }
}