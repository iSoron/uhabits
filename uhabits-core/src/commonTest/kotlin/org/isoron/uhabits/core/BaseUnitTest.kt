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
package org.isoron.uhabits.core

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.isoron.platform.io.Database
import org.isoron.platform.io.DatabaseOpener
import org.isoron.platform.io.FileOpener
import org.isoron.platform.io.TestDatabaseHelper
import org.isoron.platform.io.UserFile
import org.isoron.platform.io.createTestDatabaseOpenerSuspend
import org.isoron.platform.io.createTestFileOpener
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.setFirstWeekdayNumber
import org.isoron.platform.time.setToday
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.memory.MemoryModelFactory
import org.isoron.uhabits.core.tasks.CoroutineTaskRunner
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.test.HabitFixtures
import kotlin.test.BeforeTest

open class BaseUnitTest {
    protected open lateinit var habitList: HabitList
    protected lateinit var fixtures: HabitFixtures
    protected lateinit var modelFactory: ModelFactory
    protected lateinit var taskRunner: TaskRunner
    protected open lateinit var commandRunner: CommandRunner
    protected val fileOpener: FileOpener = createTestFileOpener()
    private var _databaseOpener: DatabaseOpener? = null
    protected suspend fun databaseOpener(): DatabaseOpener {
        if (_databaseOpener == null) {
            _databaseOpener = createTestDatabaseOpenerSuspend()
        }
        return _databaseOpener!!
    }

    @BeforeTest
    open fun setUp() {
        setToday(LocalDate(2015, 1, 25))
        setFirstWeekdayNumber(7)
        val memoryModelFactory = MemoryModelFactory()
        habitList = memoryModelFactory.buildHabitList()
        fixtures = HabitFixtures(memoryModelFactory, habitList)
        modelFactory = memoryModelFactory
        taskRunner = CoroutineTaskRunner(
            mainDispatcher = UnconfinedTestDispatcher(),
            ioDispatcher = UnconfinedTestDispatcher()
        )
        commandRunner = CommandRunner(taskRunner)
    }

    protected suspend fun createTempDir(): UserFile {
        val dir = fileOpener.openUserFile("test-temp-dir-${tempFileCounter++}")
        dir.mkdirs()
        return dir
    }

    protected suspend fun copyResourceToTempFile(resourcePath: String): UserFile {
        val cleanPath = resourcePath.removePrefix("/")
        val tempFile = fileOpener.openUserFile("test-temp-${tempFileCounter++}")
        fileOpener.openResourceFile(cleanPath).copyTo(tempFile)
        return tempFile
    }

    protected suspend fun openDatabaseResource(resourcePath: String): Database {
        val tempFile = copyResourceToTempFile(resourcePath)
        return databaseOpener().open(tempFile.pathString)
    }

    companion object {
        private var tempFileCounter = 0

        suspend fun buildMemoryDatabase(): Database {
            return TestDatabaseHelper.createEmptyDatabase()
        }
    }
}
