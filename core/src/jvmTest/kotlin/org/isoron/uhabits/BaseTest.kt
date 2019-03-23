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

package org.isoron.uhabits

import org.isoron.uhabits.models.HabitRepository
import org.isoron.uhabits.utils.*
import org.junit.Before

open class BaseTest {
    val fileOpener = JavaFileOpener()
    val log = StandardLog()
    val databaseOpener = JavaDatabaseOpener(log)
    lateinit var db: Database

    @Before
    open fun setUp() {
        val dbFile = fileOpener.openUserFile("test.sqlite3")
        if (dbFile.exists()) dbFile.delete()
        db = databaseOpener.open(dbFile)
        db.migrateTo(LOOP_DATABASE_VERSION, fileOpener, log)
    }
}