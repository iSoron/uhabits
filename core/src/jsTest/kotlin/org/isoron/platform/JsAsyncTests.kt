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

package org.isoron

import kotlinx.coroutines.*
import org.isoron.platform.io.*
import org.isoron.uhabits.*
import org.isoron.uhabits.models.*
import kotlin.test.*

class JsAsyncTests {
    var fs: JsFileStorage? = null

    suspend fun getFileOpener(): FileOpener {
        if (fs == null) {
            fs = JsFileStorage()
            fs?.init()
        }
        return JsFileOpener(fs!!)
    }

    suspend fun getDatabase(): Database {
        val nativeDB = eval("new SQL.Database()")
        val db = JsDatabase(nativeDB)
        db.migrateTo(LOOP_DATABASE_VERSION, getFileOpener(), StandardLog())
        return db
    }

    @Test
    fun testFiles() = GlobalScope.promise {
        FilesTest(getFileOpener()).testLines()
    }

    @Test
    fun testDatabase() = GlobalScope.promise {
        DatabaseTest(getDatabase()).testUsage()
    }

    @Test
    fun testCheckmarkRepository() = GlobalScope.promise {
        CheckmarkRepositoryTest(getDatabase()).testCRUD()
    }

    @Test
    fun testHabitRepository() = GlobalScope.promise {
        HabitRepositoryTest(getDatabase()).testCRUD()
    }

    @Test
    fun testPreferencesRepository() = GlobalScope.promise {
        PreferencesRepositoryTest(getDatabase()).testUsage()
    }
}