/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

import org.isoron.platform.time.DateUtils
import org.isoron.uhabits.AndroidDirFinder
import org.isoron.uhabits.BaseAndroidTest
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.util.TimeZone

class AutoBackupTest : BaseAndroidTest() {
    @Test
    fun testRun() {
        val basedir = AndroidDirFinder(targetContext).getFilesDir("Backups")!!
        createTestFiles(basedir, 30)

        val autoBackup = AutoBackup(targetContext)
        autoBackup.run(keep = 5)

        for (k in 1..25) assertDoesNotExist("${basedir.path}/test-$k.txt")
        for (k in 26..30) assertExists("${basedir.path}/test-$k.txt")
        assertTrue(basedir.list()!!.any { it.startsWith("Loop Habits Backup ") })
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

    @Test
    fun testRunWhenNewestBackupIsOverADayOld() {
        val basedir = createBackupHoursAgo(25)
        withTimeZone("America/Los_Angeles") { AutoBackup(targetContext).run() }
        assertEquals(2, basedir.list()!!.size)
    }

    @Test
    fun testRunWhenNewestBackupIsUnderADayOld() {
        val basedir = createBackupHoursAgo(23)
        withTimeZone("Asia/Tokyo") { AutoBackup(targetContext).run() }
        assertEquals(1, basedir.list()!!.size)
    }

    private fun createBackupHoursAgo(hours: Long): File {
        val basedir = AndroidDirFinder(targetContext).getFilesDir("Backups")!!
        removeAllFiles(basedir)
        val time = System.currentTimeMillis() - hours * DateUtils.HOUR_LENGTH
        touch("${basedir.path}/Loop Habits Backup old.db", time)
        return basedir
    }

    // File times are UTC, so the backup age must not depend on the time zone
    private fun withTimeZone(id: String, block: () -> Unit) {
        DateUtils.setFixedTimeZone(TimeZone.getTimeZone(id))
        try {
            block()
        } finally {
            DateUtils.setFixedTimeZone(null)
        }
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
