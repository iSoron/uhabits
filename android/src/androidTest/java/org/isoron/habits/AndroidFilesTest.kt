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

package org.isoron.habits

import org.junit.Test

import org.junit.Assert.*
import java.io.*

class AndroidFilesTest : BaseTest() {

    @Test
    fun testUserFiles() {
        val file = File(context.filesDir, "test.txt")
        file.writeText("Hello world!")

        val af = fileOpener.openUserFile("test.txt")
        assertTrue(af.exists())
        af.delete()
        assertFalse(af.exists())
    }

    @Test
    fun testResourceFiles() {
        val file = fileOpener.openResourceFile("migrations/010.sql")
        val lines = file.readLines()
        assertEquals("delete from Score", lines[0])
    }
}