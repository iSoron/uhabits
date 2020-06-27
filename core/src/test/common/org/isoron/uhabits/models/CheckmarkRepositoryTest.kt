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
import org.isoron.platform.time.*
import kotlin.test.*

class CheckmarkRepositoryTest() {
    @Test
    fun testCRUD() = asyncTest {
        val db = DependencyResolver.getDatabase()

        val habitA = 10
        var checkmarksA = listOf(Checkmark(LocalDate(2019, 1, 15), 100),
                                 Checkmark(LocalDate(2019, 1, 7), 500),
                                 Checkmark(LocalDate(2019, 1, 1), 900))

        val habitB = 35
        val checkmarksB = listOf(Checkmark(LocalDate(2019, 1, 30), 50),
                                 Checkmark(LocalDate(2019, 1, 29), 30),
                                 Checkmark(LocalDate(2019, 1, 27), 900),
                                 Checkmark(LocalDate(2019, 1, 25), 450),
                                 Checkmark(LocalDate(2019, 1, 20), 1000))

        val repository = CheckmarkRepository(db)

        for (c in checkmarksA) repository.insert(habitA, c)
        for (c in checkmarksB) repository.insert(habitB, c)
        assertEquals(checkmarksA, repository.findAll(habitA))
        assertEquals(checkmarksB, repository.findAll(habitB))
        assertEquals(listOf(), repository.findAll(999))

        checkmarksA = listOf(Checkmark(LocalDate(2019, 1, 15), 100),
                             Checkmark(LocalDate(2019, 1, 1), 900))
        repository.delete(habitA, LocalDate(2019, 1, 7))
        assertEquals(checkmarksA, repository.findAll(habitA))
    }
}