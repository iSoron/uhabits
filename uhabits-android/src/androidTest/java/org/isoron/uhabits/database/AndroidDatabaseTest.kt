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
package org.isoron.uhabits.database

import android.database.sqlite.SQLiteDatabase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.isoron.uhabits.BaseAndroidTest
import org.isoron.uhabits.core.database.Cursor
import org.junit.Test

class AndroidDatabaseTest : BaseAndroidTest() {
    private lateinit var db: AndroidDatabase
    override fun setUp() {
        super.setUp()
        db = AndroidDatabase(SQLiteDatabase.create(null), null)
        db.execute("create table test(color int, name string)")
    }

    @Test
    @Throws(Exception::class)
    fun testInsert() {
        val map = mapOf(Pair("name", "asd"), Pair("color", null))
        db.insert("test", map)
        val c: Cursor = db.query("select * from test")
        c.moveToNext()
        c.getInt(0)!!
        assertThat(c.getString(1), equalTo("asd"))
    }
}
