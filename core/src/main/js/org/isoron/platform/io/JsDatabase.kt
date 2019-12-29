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

external fun require(module: String): dynamic

class JsPreparedStatement(val stmt: dynamic) : PreparedStatement {

    override fun step(): StepResult {
        val isRowAvailable = stmt.step() as Boolean
        return if(isRowAvailable) StepResult.ROW else StepResult.DONE
    }

    override fun finalize() {
        stmt.free()
    }

    override fun getInt(index: Int): Int {
        return (stmt.getNumber(index) as Double).toInt()
    }

    override fun getLong(index: Int): Long {
        return (stmt.getNumber(index) as Double).toLong()
    }

    override fun getText(index: Int): String {
        return stmt.getString(index) as String
    }

    override fun getReal(index: Int): Double {
        return stmt.getNumber(index) as Double
    }

    override fun bindInt(index: Int, value: Int) {
        stmt.bindNumber(value, index + 1)
    }

    override fun bindLong(index: Int, value: Long) {
        stmt.bindNumber(value, index + 1)
    }

    override fun bindText(index: Int, value: String) {
        stmt.bindString(value, index + 1)
    }

    override fun bindReal(index: Int, value: Double) {
        stmt.bindNumber(value, index + 1)
    }

    override fun reset() {
        stmt.reset()
    }

}

class JsDatabase(val db: dynamic) : Database {
    override fun prepareStatement(sql: String): PreparedStatement {
        return JsPreparedStatement(db.prepare(sql))
    }

    override fun close() {
    }
}