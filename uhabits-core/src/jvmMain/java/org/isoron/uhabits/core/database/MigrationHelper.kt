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

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Locale

class MigrationHelper(
    private val db: Database,
) {
    fun migrateTo(newVersion: Int) {
        try {
            for (v in db.version + 1..newVersion) {
                val fname = String.format(Locale.US, "/migrations/%02d.sql", v)
                for (command in SQLParser.parse(open(fname))) db.execute(command)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun open(fname: String): InputStream {
        val resource = javaClass.getResourceAsStream(fname)
        if (resource != null) return resource

        // Workaround for bug in Android Studio / IntelliJ. Removing this
        // causes unit tests to fail when run from within the IDE, although
        // everything works fine from the command line.
        val file = File("uhabits-core/src/main/resources/$fname")
        if (file.exists()) return FileInputStream(file)
        throw RuntimeException("resource not found: $fname")
    }
}
