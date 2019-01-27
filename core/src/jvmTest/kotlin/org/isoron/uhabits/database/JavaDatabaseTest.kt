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

package org.isoron.uhabits.database

import org.isoron.uhabits.BaseTest
import org.isoron.uhabits.utils.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class JavaDatabaseTest : BaseTest() {
    private lateinit var db: Database

    @Before
    fun setup() {
        val dbFile = fileOpener.openUserFile("test.sqlite3")
        if (dbFile.exists()) dbFile.delete()
        db = databaseOpener.open(dbFile)
    }

    @Test
    fun testUsage() {
        db.setVersion(0)
        assertEquals(db.getVersion(), 0)

        db.setVersion(23)
        assertEquals(db.getVersion(), 23)

        var stmt = db.prepareStatement("drop table if exists demo")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement("create table if not exists demo(key int, value text)")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement("insert into demo(key, value) values (?1, ?2)")
        stmt.bindInt(1, 42)
        stmt.bindText(2, "Hello World")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement("select * from demo where key > ?1")
        stmt.bindInt(1, 10)

        var result = stmt.step()
        assertEquals(result, StepResult.ROW)
        assertEquals(stmt.getInt(0), 42)
        assertEquals(stmt.getText(1), "Hello World")

        result = stmt.step()
        assertEquals(result, StepResult.DONE)

        stmt.finalize()
        db.close()
    }

    @Test
    fun testMigrateTo() {
        assertEquals(0, db.getVersion())
        db.migrateTo(22, fileOpener)
        assertEquals(22, db.getVersion())
        db.execute("select * from habits")
    }
}