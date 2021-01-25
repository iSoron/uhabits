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
package org.isoron.uhabits.core.preferences

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties

class PropertiesStorage(file: File) : Preferences.Storage {
    private var props: Properties
    private val file: File
    override fun clear() {
        for (key in props.stringPropertyNames()) props.remove(key)
        flush()
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val value = props.getProperty(key, java.lang.Boolean.toString(defValue))
        return java.lang.Boolean.parseBoolean(value)
    }

    override fun getInt(key: String, defValue: Int): Int {
        val value = props.getProperty(key, defValue.toString())
        return value.toInt()
    }

    override fun getLong(key: String, defValue: Long): Long {
        val value = props.getProperty(key, defValue.toString())
        return value.toLong()
    }

    override fun getString(key: String, defValue: String): String {
        return props.getProperty(key, defValue)
    }

    override fun onAttached(preferences: Preferences) {
        // nop
    }

    override fun putBoolean(key: String, value: Boolean) {
        props.setProperty(key, java.lang.Boolean.toString(value))
    }

    override fun putInt(key: String, value: Int) {
        props.setProperty(key, value.toString())
        flush()
    }

    private fun flush() {
        try {
            props.store(FileOutputStream(file), "")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun putLong(key: String, value: Long) {
        props.setProperty(key, value.toString())
        flush()
    }

    override fun putString(key: String, value: String) {
        props.setProperty(key, value)
        flush()
    }

    override fun remove(key: String) {
        props.remove(key)
        flush()
    }

    init {
        try {
            this.file = file
            props = Properties()
            props.load(FileInputStream(file))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
