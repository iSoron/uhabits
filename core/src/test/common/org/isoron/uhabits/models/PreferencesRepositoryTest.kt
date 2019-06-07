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

import org.isoron.*
import org.isoron.platform.io.*
import kotlin.test.*

class PreferencesRepositoryTest() {
    @Test
    fun testUsage() = asyncTest{
        val db = DependencyResolver.getDatabase()
        val prefs = PreferencesRepository(db)
        assertEquals("default", prefs.getString("non_existing_key", "default"))
        prefs.putString("ringtone_path", "/tmp")
        assertEquals("/tmp", prefs.getString("ringtone_path", "none"))

        assertEquals(42, prefs.getLong("non_existing_key", 42))
        prefs.putLong("times_launched", 130)
        assertEquals(130, prefs.getLong("times_launched", 0))

        assertEquals(true, prefs.getBoolean("non_existing_key", true))
        assertEquals(false, prefs.getBoolean("non_existing_key", false))
        prefs.putBoolean("show_archived", true)
        assertEquals(true, prefs.getBoolean("show_archived", false))
    }
}