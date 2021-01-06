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

import org.isoron.uhabits.AndroidDirFinder
import org.isoron.uhabits.BaseAndroidTest
import org.isoron.uhabits.core.utils.DateUtils
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class AutoBackupTest : BaseAndroidTest() {
    @Test
    fun testRun() {
        DateUtils.setFixedLocalTime(40 * DateUtils.DAY_LENGTH)
        val basedir = AndroidDirFinder(targetContext).getFilesDir("Backups")!!
        createTestFiles(basedir, 30)

        val autoBackup = AutoBackup(targetContext)
        autoBackup.run(keep = 5)

        for (k in 1..25) assertDoesNotExist("${basedir.path}/test-$k.txt")
        for (k in 26..30) assertExists("${basedir.path}/test-$k.txt")
        assertExists("${basedir.path}/Loop Habits Backup 1970-02-10 000000.db")
    }

    @Test
    fun testRunWithEmptyDir() {
        val basedir = AndroidDirFinder(targetContext).getFilesDir("Backups")!!
        removeAllFiles(basedir)
        basedir.delete()

        // Should not crash
        val autoBackup = AutoBackup(targetContext)
        autoBackup.run()
    }

    private fun assertExists(path: String) {
        assertTrue("File $path should exist", File(path).exists())
    }

    private fun assertDoesNotExist(path: String) {
        assertFalse("File $path should not exist", File(path).exists())
    }

    private fun createTestFiles(basedir: File, nfiles: Int) {
        removeAllFiles(basedir)
        for (k in 1..nfiles) {
            touch("${basedir.path}/test-$k.txt", DateUtils.DAY_LENGTH * k)
        }
    }

    private fun touch(path: String, time: Long) {
        val file = File(path)
        FileOutputStream(file).close()
        file.setLastModified(time)
    }

    private fun removeAllFiles(dir: File) {
        dir.list().forEach { path ->
            val file = File("${dir.path}/$path")
            assertTrue(file.delete())
        }
    }
}
