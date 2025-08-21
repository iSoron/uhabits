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

package org.isoron.uhabits.utils

import android.content.Context
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.utils.DateUtils.Companion.setFixedLocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertTrue

class DatabaseUtilsTest {
    private lateinit var baseDir: File
    private lateinit var filesDir: File
    private lateinit var backupDir: File
    private lateinit var context: Context

    @Before
    fun setUp() {
        baseDir = Files.createTempDirectory("dbutils").toFile()
        filesDir = File(baseDir, "files").apply { mkdirs() }
        File(baseDir, "databases").apply {
            mkdirs()
            File(this, "uhabits.db").writeText("data")
        }
        backupDir = File(baseDir, "backup").apply { mkdirs() }

        context = mock()
        whenever(context.filesDir).thenReturn(filesDir)
        setFixedLocalTime(1422172800000L)
    }

    @After
    fun tearDown() {
        setFixedLocalTime(null)
    }

    @Test
    fun testSaveDatabaseCopyWithDate() {
        val path = DatabaseUtils.saveDatabaseCopy(context, backupDir, true)
        val expected = File(backupDir, "Loop Habits Backup 2015-01-25 080000.db").absolutePath
        assertThat(path, equalTo(expected))
        assertTrue(File(path).exists())
    }

    @Test
    fun testSaveDatabaseCopyWithoutDate() {
        val path = DatabaseUtils.saveDatabaseCopy(context, backupDir, false)
        val expected = File(backupDir, "Loop Habits Backup.db").absolutePath
        assertThat(path, equalTo(expected))
        assertTrue(File(path).exists())
    }
}
