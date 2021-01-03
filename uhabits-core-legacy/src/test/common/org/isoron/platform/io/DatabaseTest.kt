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

import org.isoron.*
import kotlin.test.*

class DatabaseTest  {
    @Test
    fun testUsage() = asyncTest{
        val db = DependencyResolver.getDatabase()

        db.setVersion(0)
        assertEquals(0, db.getVersion())

        db.setVersion(23)
        assertEquals(23, db.getVersion())

        var stmt = db.prepareStatement("drop table if exists demo")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement("create table if not exists demo(key int, value text)")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement("insert into demo(key, value) values (?, ?)")
        stmt.bindInt(0, 42)
        stmt.bindText(1, "Hello World")
        stmt.step()
        stmt.finalize()

        stmt = db.prepareStatement("select * from demo where key > ?")
        stmt.bindInt(0, 10)

        var result = stmt.step()
        assertEquals(StepResult.ROW, result)
        assertEquals(42, stmt.getInt(0))
        assertEquals("Hello World", stmt.getText(1))

        result = stmt.step()
        assertEquals(StepResult.DONE, result)

        stmt.finalize()
        db.close()
    }
}