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

package org.isoron.uhabits.server.sync

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.isoron.uhabits.core.sync.SyncData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class RepositoryTest {

    @Test
    fun testUsage() = runBlocking {
        val tempdir = Files.createTempDirectory("db")!!
        val repo = Repository(tempdir)

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
