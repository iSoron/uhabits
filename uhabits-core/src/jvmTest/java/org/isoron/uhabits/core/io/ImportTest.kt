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
package org.isoron.uhabits.core.io

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils.Companion.getStartOfTodayCalendar
import org.isoron.uhabits.core.utils.DateUtils.Companion.setFixedLocalTime
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException

class ImportTest : BaseUnitTest() {
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        setFixedLocalTime(null)
    }

    @Test
    @Throws(IOException::class)
    fun testHabitBullCSV() {
        importFromFile("habitbull.csv")
        assertThat(habitList.size(), IsEqual.equalTo(4))
        val habit = habitList.getByPosition(0)
        assertThat(habit.name, IsEqual.equalTo("Breed dragons"))
        assertThat(habit.description, IsEqual.equalTo("with love and fire"))
        assertThat(habit.frequency, IsEqual.equalTo(Frequency.DAILY))
        assertTrue(isChecked(habit, 2016, 3, 18))
        assertTrue(isChecked(habit, 2016, 3, 19))
        assertFalse(isChecked(habit, 2016, 3, 20))
    }

    @Test
    @Throws(IOException::class)
    fun testLoopDB() {
        importFromFile("loop.db")
        assertThat(habitList.size(), IsEqual.equalTo(9))
        val habit = habitList.getByPosition(0)
        assertThat(habit.name, IsEqual.equalTo("Wake up early"))
        assertThat(habit.frequency, IsEqual.equalTo(Frequency.THREE_TIMES_PER_WEEK))
        assertTrue(isChecked(habit, 2016, 3, 14))
        assertTrue(isChecked(habit, 2016, 3, 16))
        assertFalse(isChecked(habit, 2016, 3, 17))
    }

    @Test
    @Throws(IOException::class)
    fun testRewireDB() {
        importFromFile("rewire.db")
        assertThat(habitList.size(), IsEqual.equalTo(3))
        var habit = habitList.getByPosition(1)
        assertThat(habit.name, IsEqual.equalTo("Wake up early"))
        assertThat(habit.frequency, IsEqual.equalTo(Frequency.THREE_TIMES_PER_WEEK))
        assertFalse(habit.hasReminder())
        assertFalse(isChecked(habit, 2015, 12, 31))
        assertTrue(isChecked(habit, 2016, 1, 18))
        assertTrue(isChecked(habit, 2016, 1, 28))
        assertFalse(isChecked(habit, 2016, 3, 10))
        habit = habitList.getByPosition(2)
        assertThat(habit.name, IsEqual.equalTo("brush teeth"))
        assertThat(habit.frequency, IsEqual.equalTo(Frequency.THREE_TIMES_PER_WEEK))
        assertThat(habit.hasReminder(), IsEqual.equalTo(true))
        val reminder = habit.reminder
        assertThat(reminder!!.hour, IsEqual.equalTo(8))
        assertThat(reminder.minute, IsEqual.equalTo(0))
        val reminderDays = booleanArrayOf(false, true, true, true, true, true, false)
        assertThat(reminder.days.toArray(), IsEqual.equalTo(reminderDays))
    }

    @Test
    @Throws(IOException::class)
    fun testTickmateDB() {
        importFromFile("tickmate.db")
        assertThat(habitList.size(), IsEqual.equalTo(3))
        val h = habitList.getByPosition(2)
        assertThat(h.name, IsEqual.equalTo("Vegan"))
        assertTrue(isChecked(h, 2016, 1, 24))
        assertTrue(isChecked(h, 2016, 2, 5))
        assertTrue(isChecked(h, 2016, 3, 18))
        assertFalse(isChecked(h, 2016, 3, 14))
    }

    private fun isChecked(h: Habit, year: Int, month: Int, day: Int): Boolean {
        val date = getStartOfTodayCalendar()
        date.set(year, month - 1, day)
        val timestamp = Timestamp(date)
        return h.originalEntries.get(timestamp).value == Entry.YES_MANUAL
    }

    @Throws(IOException::class)
    private fun importFromFile(assetFilename: String) {
        val file = File.createTempFile("asset", "")
        copyAssetToFile(assetFilename, file)
        assertTrue(file.exists())
        assertTrue(file.canRead())
        val importer = GenericImporter(
            LoopDBImporter(
                habitList,
                modelFactory,
                databaseOpener,
                commandRunner,
                StandardLogging()
            ),
            RewireDBImporter(habitList, modelFactory, databaseOpener),
            TickmateDBImporter(habitList, modelFactory, databaseOpener),
            HabitBullCSVImporter(habitList, modelFactory)
        )
        assertTrue(importer.canHandle(file))
        importer.importHabitsFromFile(file)
        file.delete()
    }
}
