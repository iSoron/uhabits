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

package org.isoron.uhabits.backend

import junit.framework.TestCase.*
import org.isoron.platform.gui.*
import org.isoron.uhabits.*
import org.junit.*
import java.util.*
import java.util.concurrent.*

class BackendTest : BaseTest() {
    lateinit var backend: Backend
    private val latch = CountDownLatch(1)
    val dbFilename = "uhabits${Random().nextInt()}.db"
    val dbFile = fileOpener.openUserFile(dbFilename)

    @Before
    override fun setUp() {
        super.setUp()
        if (dbFile.exists()) dbFile.delete()
        backend = Backend(dbFilename,
                          databaseOpener,
                          fileOpener,
                          log,
                          taskRunner)
    }

    @After
    fun tearDown() {
        dbFile.delete()
    }

    @Test
    fun testMainScreenDataSource() {
        val listener = object : MainScreenDataSource.Listener {
            override fun onDataChanged(newData: MainScreenDataSource.Data) {
                val expected = MainScreenDataSource.Data(
                        ids = listOf(0, 10, 9, 2, 3, 4, 5, 11, 6, 7, 8),
                        scores = listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                                        0.0, 0.0, 0.0),
                        names = listOf("Wake up early", "Eat healthy", "Floss",
                                       "Journal", "Track time", "Meditate",
                                       "Work out", "Take a walk", "Read books",
                                       "Learn French", "Play chess"),
                        colors = listOf(PaletteColor(8), PaletteColor(8),
                                        PaletteColor(8), PaletteColor(11),
                                        PaletteColor(11), PaletteColor(15),
                                        PaletteColor(15), PaletteColor(15),
                                        PaletteColor(2), PaletteColor(2),
                                        PaletteColor(13)),
                        checkmarks = listOf(
                                listOf(2, 0, 0, 0, 0, 2, 0),
                                listOf(0, 2, 2, 2, 2, 2, 0),
                                listOf(0, 0, 0, 0, 2, 0, 0),
                                listOf(0, 2, 0, 2, 0, 0, 0),
                                listOf(2, 2, 2, 0, 2, 2, 2),
                                listOf(2, 1, 1, 2, 1, 2, 2),
                                listOf(2, 0, 2, 0, 2, 1, 2),
                                listOf(0, 2, 2, 2, 2, 0, 0),
                                listOf(0, 2, 2, 2, 2, 2, 0),
                                listOf(0, 0, 2, 0, 2, 0, 2),
                                listOf(0, 2, 0, 0, 2, 2, 0)))
                assertEquals(newData, expected)
                latch.countDown()
            }
        }
        backend.mainScreenDataSource.observable.addListener(listener)
        backend.mainScreenDataSource.requestData()
        assertTrue(latch.await(3, TimeUnit.SECONDS))
    }
}
