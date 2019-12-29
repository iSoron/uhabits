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

package org.isoron.uhabits.models

import org.isoron.platform.io.*

class PreferencesRepository(private val db: Database) {

    private val insertStatement = db.prepareStatement("insert into Preferences(key, value) values (?, ?)")
    private val deleteStatement = db.prepareStatement("delete from Preferences where key=?")
    private val selectStatement = db.prepareStatement("select value from Preferences where key=?")

    fun putBoolean(key: String, value: Boolean) {
        putString(key, value.toString())
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        val value = getString(key, "NULL")
        return if (value == "NULL") default else value.toBoolean()
    }

    fun putLong(key: String, value: Long) {
        putString(key, value.toString())
    }

    fun getLong(key: String, default: Long): Long {
        val value = getString(key, "NULL")
        return if (value == "NULL") default else value.toLong()
    }

    fun putString(key: String, value: String) {
        deleteStatement.bindText(0, key)
        deleteStatement.step()
        deleteStatement.reset()
        insertStatement.bindText(0, key)
        insertStatement.bindText(1, value)
        insertStatement.step()
        insertStatement.reset()
    }

    fun getString(key: String, default: String): String {
        selectStatement.bindText(0, key)
        if (selectStatement.step() == StepResult.DONE) {
            selectStatement.reset()
            return default
        } else {
            val value = selectStatement.getText(0)
            selectStatement.reset()
            return value
        }
    }
}