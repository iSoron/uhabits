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

class FilesTest() {
    @Test
    fun testLines() = asyncTest {
        val fileOpener = DependencyResolver.getFileOpener()

        assertFalse(fileOpener.openUserFile("non-existing-usr.txt").exists(),
                    "non-existing-usr.txt shouldn't exist")

        assertFalse(fileOpener.openResourceFile("non-existing-res.txt").exists(),
                    "non-existing-res.txt shouldn't exist")

        val hello = fileOpener.openResourceFile("hello.txt")
        assertTrue(hello.exists(), "hello.txt should exist")
        var lines = hello.lines()
        assertEquals(2, lines.size)
        assertEquals("Hello World!", lines[0])
        assertEquals("This is a resource.", lines[1])

        val helloCopy = fileOpener.openUserFile("copies/hello.txt")
        hello.copyTo(helloCopy)
        lines = helloCopy.lines()
        assertEquals("Hello World!", lines[0])
        assertEquals("This is a resource.", lines[1])

        assertTrue(helloCopy.exists(), "helloCopy should exist")
        helloCopy.delete()
        assertFalse(helloCopy.exists(), "helloCopy shouldn't exist")


        val migration = fileOpener.openResourceFile("migrations/012.sql")
        assertTrue(migration.exists(), "migrations/012.sql should exist")
        lines = migration.lines()
        assertEquals("delete from Score", lines[0])
    }
}