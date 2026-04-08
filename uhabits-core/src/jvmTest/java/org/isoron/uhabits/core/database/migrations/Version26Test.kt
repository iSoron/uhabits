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

import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.database.Database
import org.isoron.uhabits.core.database.MigrationHelper
import org.isoron.uhabits.core.models.sqlite.SQLModelFactory
import org.isoron.uhabits.core.preferences.WidgetPreferences
import org.isoron.uhabits.core.test.HabitFixtures
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class Version26Test : BaseUnitTest() {

    private lateinit var db: Database

    private lateinit var helper: MigrationHelper

    private val widgetPreferences: WidgetPreferences = mock()

    override fun setUp() {
        super.setUp()
        db = openDatabaseResource("/databases/022.db")
        helper = MigrationHelper(db)
        modelFactory = SQLModelFactory(db, widgetPreferences)
        habitList = (modelFactory as SQLModelFactory).buildHabitList()
        fixtures = HabitFixtures(modelFactory, habitList)
    }

    private fun migrateTo26() = helper.migrateTo(26)

    @Test
    fun `test migrate to 26 keeps all habits`() {
        var cursor = db.query("select name from Habits")

        val namesBefore = mutableListOf<String?>()
        while (cursor.moveToNext()) {
            namesBefore.add(cursor.getString(0))
        }

        migrateTo26()
        cursor = db.query("select name from Habits")

        val namesAfter = mutableListOf<String?>()
        while (cursor.moveToNext()) {
            namesAfter.add(cursor.getString(0))
        }

        assertEquals(namesAfter, namesBefore)
    }

    @Test
    fun `test migrate to 26 populates shared ids`() {
        var cursor = db.query("select id from Habits")

        val ids = mutableListOf<Int>()
        while (cursor.moveToNext()) {
            ids.add(cursor.getInt(0) ?: 0)
        }
        val maxId = ids.maxOrNull() ?: 0

        migrateTo26()

        cursor = db.query("select next_id from SharedIds where name = 'habitandgroup'")
        var nextId = 0
        if (cursor.moveToNext()) {
            nextId = cursor.getInt(0) ?: 0
        }

        assertEquals(nextId, maxId + 1)
    }
}
