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


@file:Suppress("BlockingMethodInNonBlockingContext")

package org.isoron.uhabits.sync.repository

import kotlinx.coroutines.*
import org.hamcrest.CoreMatchers.*
import org.isoron.uhabits.sync.*
import org.junit.*
import org.junit.Assert.*
import java.nio.file.*

class FileRepositoryTest {

    @Test
    fun testUsage() = runBlocking {
        val tempdir = Files.createTempDirectory("db")!!
        val repo = FileRepository(tempdir)

        val original = SyncData(10, "Hello world")
        repo.put("abcdefg", original)

        val metaPath = tempdir.resolve("a/b/c/d/abcdefg/version")
        assertTrue("$metaPath should exist", Files.exists(metaPath))
        assertEquals("10", metaPath.toFile().readText())

        val dataPath = tempdir.resolve("a/b/c/d/abcdefg/content")
        assertTrue("$dataPath should exist", Files.exists(dataPath))
        assertEquals("Hello world", dataPath.toFile().readText())

        val retrieved = repo.get("abcdefg")
        assertThat(retrieved, equalTo(original))
    }
}