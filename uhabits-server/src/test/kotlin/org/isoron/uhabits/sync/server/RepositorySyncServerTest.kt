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

package org.isoron.uhabits.sync.server

import kotlinx.coroutines.*
import org.isoron.uhabits.sync.*
import org.isoron.uhabits.sync.repository.*
import org.junit.Test
import java.nio.file.*
import kotlin.test.*

class RepositorySyncServerTest {

    private val tempdir = Files.createTempDirectory("db")
    private val server = RepositorySyncServer(FileRepository(tempdir))
    private val key = runBlocking { server.register() }

    @Test
    fun testUsage(): Unit = runBlocking {
        val data0 = SyncData(0, "")
        assertEquals(server.getData(key), data0)

        val data1 = SyncData(1, "Hello world")
        server.put(key, data1)
        assertEquals(server.getData(key), data1)

        val data2 = SyncData(2, "Hello new world")
        server.put(key, data2)
        assertEquals(server.getData(key), data2)

        assertFailsWith<EditConflictException> {
            server.put(key, data2)
        }

        assertFailsWith<KeyNotFoundException> {
            server.getData("INVALID")
        }

        assertFailsWith<KeyNotFoundException> {
            server.put("INVALID", data0)
        }
    }
}