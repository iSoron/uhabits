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
package org.isoron.uhabits.core.database.migrations

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.database.Cursor
import org.isoron.uhabits.core.database.Database
import org.isoron.uhabits.core.database.MigrationHelper
import org.isoron.uhabits.core.models.sqlite.SQLModelFactory
import org.isoron.uhabits.core.test.HabitFixtures
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class Version22Test : BaseUnitTest() {
    @get:Rule
    var exception = ExpectedException.none()!!
    private lateinit var db: Database
    private lateinit var helper: MigrationHelper

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        db = openDatabaseResource("/databases/021.db")
        helper = MigrationHelper(db)
        modelFactory = SQLModelFactory(db)
        habitList = (modelFactory as SQLModelFactory).buildHabitList()
        fixtures = HabitFixtures(modelFactory, habitList)
    }

    @Test
    @Throws(Exception::class)
    fun testKeepValidReps() {
        db.query("select count(*) from repetitions") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(3))
        }
        helper.migrateTo(22)
        db.query("select count(*) from repetitions") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(3))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveRepsWithInvalidId() {
        db.execute("insert into Repetitions(habit, timestamp, value) values (99999, 100, 2)")
        db.query("select count(*) from repetitions where habit = 99999") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(1))
        }
        helper.migrateTo(22)
        db.query("select count(*) from repetitions where habit = 99999") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(0))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDisallowNewRepsWithInvalidRef() {
        helper.migrateTo(22)
        exception.expectMessage(Matchers.containsString("SQLITE_CONSTRAINT"))
        db.execute("insert into Repetitions(habit, timestamp, value) values (99999, 100, 2)")
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveRepetitionsWithNullTimestamp() {
        db.execute("insert into repetitions(habit, value) values (0, 2)")
        db.query("select count(*) from repetitions where timestamp is null") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(1))
        }
        helper.migrateTo(22)
        db.query("select count(*) from repetitions where timestamp is null") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(0))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDisallowNullTimestamp() {
        helper.migrateTo(22)
        exception.expectMessage(Matchers.containsString("SQLITE_CONSTRAINT"))
        db.execute("insert into Repetitions(habit, value) " + "values (0, 2)")
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveRepetitionsWithNullHabit() {
        db.execute("insert into repetitions(timestamp, value) values (0, 2)")
        db.query("select count(*) from repetitions where habit is null") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(1))
        }
        helper.migrateTo(22)
        db.query("select count(*) from repetitions where habit is null") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(0))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDisallowNullHabit() {
        helper.migrateTo(22)
        exception.expectMessage(Matchers.containsString("SQLITE_CONSTRAINT"))
        db.execute("insert into Repetitions(timestamp, value) " + "values (5, 2)")
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveDuplicateRepetitions() {
        db.execute("insert into repetitions(habit, timestamp, value)values (0, 100, 2)")
        db.execute("insert into repetitions(habit, timestamp, value)values (0, 100, 5)")
        db.execute("insert into repetitions(habit, timestamp, value)values (0, 100, 10)")
        db.query("select count(*) from repetitions where timestamp=100 and habit=0") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(3))
        }
        helper.migrateTo(22)
        db.query("select count(*) from repetitions where timestamp=100 and habit=0") { c: Cursor ->
            assertThat(c.getInt(0), equalTo(1))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDisallowNewDuplicateTimestamps() {
        helper.migrateTo(22)
        db.execute("insert into repetitions(habit, timestamp, value)values (0, 100, 2)")
        exception.expectMessage(Matchers.containsString("SQLITE_CONSTRAINT"))
        db.execute("insert into repetitions(habit, timestamp, value)values (0, 100, 5)")
    }
}
