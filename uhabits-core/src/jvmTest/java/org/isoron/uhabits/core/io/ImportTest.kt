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
import org.hamcrest.core.IsEqual.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitType
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
        assertThat(habitList.size(), equalTo(4))
        val habit = habitList.getByPosition(0)
        assertThat(habit.name, equalTo("Breed dragons"))
        assertThat(habit.description, equalTo("with love and fire"))
        assertThat(habit.frequency, equalTo(Frequency.DAILY))
        assertTrue(isChecked(habit, 2016, 3, 18))
        assertTrue(isChecked(habit, 2016, 3, 19))
        assertFalse(isChecked(habit, 2016, 3, 20))
        assertTrue(isNotesEqual(habit, 2016, 3, 18, "text"))
    }

    @Test
    @Throws(IOException::class)
    fun testHabitBullCSV2() {
        importFromFile("habitbull2.csv")
        assertThat(habitList.size(), equalTo(6))
        val habit = habitList.getByPosition(2)
        assertThat(habit.name, equalTo("H3"))
        assertThat(habit.description, equalTo("Habit 3"))
        assertThat(habit.frequency, equalTo(Frequency.DAILY))
        assertTrue(isChecked(habit, 2019, 4, 11))
        assertTrue(isChecked(habit, 2019, 5, 7))
        assertFalse(isChecked(habit, 2019, 6, 14))
        assertTrue(isNotesEqual(habit, 2019, 4, 11, "text"))
        assertTrue(isNotesEqual(habit, 2019, 6, 14, "Habit 3 notes"))
    }

    @Test
    @Throws(IOException::class)
    fun testHabitBullCSV3() {
        importFromFile("habitbull3.csv")
        assertThat(habitList.size(), equalTo(2))

        val habit = habitList.getByPosition(0)
        assertThat(habit.name, equalTo("Pushups"))
        assertThat(habit.type, equalTo(HabitType.NUMERICAL))
        assertThat(habit.description, equalTo(""))
        assertThat(habit.frequency, equalTo(Frequency.DAILY))
        assertThat(getValue(habit, 2021, 9, 1), equalTo(30))
        assertThat(getValue(habit, 2022, 1, 8), equalTo(100))

        val habit2 = habitList.getByPosition(1)
        assertThat(habit2.name, equalTo("run"))
        assertThat(habit2.type, equalTo(HabitType.YES_NO))
        assertThat(habit2.description, equalTo(""))
        assertThat(habit2.frequency, equalTo(Frequency.DAILY))
        assertTrue(isChecked(habit2, 2022, 1, 3))
        assertTrue(isChecked(habit2, 2022, 1, 18))
        assertTrue(isChecked(habit2, 2022, 1, 19))
    }

    @Test
    @Throws(IOException::class)
    fun testLoopDB() {
        importFromFile("loop.db")
        assertThat(habitList.size(), equalTo(9))
        val habit = habitList.getByPosition(0)
        assertThat(habit.name, equalTo("Wake up early"))
        assertThat(habit.frequency, equalTo(Frequency.THREE_TIMES_PER_WEEK))
        assertTrue(isChecked(habit, 2016, 3, 14))
        assertTrue(isChecked(habit, 2016, 3, 16))
        assertFalse(isChecked(habit, 2016, 3, 17))
    }

    @Test
    @Throws(IOException::class)
    fun testRewireDB() {
        importFromFile("rewire.db")
        assertThat(habitList.size(), equalTo(3))
        var habit = habitList.getByPosition(1)
        assertThat(habit.name, equalTo("Wake up early"))
        assertThat(habit.frequency, equalTo(Frequency.THREE_TIMES_PER_WEEK))
        assertFalse(habit.hasReminder())
        assertFalse(isChecked(habit, 2015, 12, 31))
        assertTrue(isChecked(habit, 2016, 1, 18))
        assertTrue(isChecked(habit, 2016, 1, 28))
        assertFalse(isChecked(habit, 2016, 3, 10))
        habit = habitList.getByPosition(2)
        assertThat(habit.name, equalTo("brush teeth"))
        assertThat(habit.frequency, equalTo(Frequency.THREE_TIMES_PER_WEEK))
        assertThat(habit.hasReminder(), equalTo(true))
        val reminder = habit.reminder
        assertThat(reminder!!.hour, equalTo(8))
        assertThat(reminder.minute, equalTo(0))
        val reminderDays = booleanArrayOf(false, true, true, true, true, true, false)
        assertThat(reminder.days.toArray(), equalTo(reminderDays))
    }

    @Test
    @Throws(IOException::class)
    fun testTickmateDB() {
        importFromFile("tickmate.db")
        assertThat(habitList.size(), equalTo(3))
        val h = habitList.getByPosition(2)
        assertThat(h.name, equalTo("Vegan"))
        assertTrue(isChecked(h, 2016, 1, 24))
        assertTrue(isChecked(h, 2016, 2, 5))
        assertTrue(isChecked(h, 2016, 3, 18))
        assertFalse(isChecked(h, 2016, 3, 14))
    }

    private fun isChecked(h: Habit, year: Int, month: Int, day: Int): Boolean {
        return getValue(h, year, month, day) == Entry.YES_MANUAL
    }

    private fun getValue(h: Habit, year: Int, month: Int, day: Int): Int {
        val date = getStartOfTodayCalendar()
        date.set(year, month - 1, day)
        val timestamp = Timestamp(date)
        return h.originalEntries.get(timestamp).value
    }

    private fun isNotesEqual(h: Habit, year: Int, month: Int, day: Int, notes: String): Boolean {
        val date = getStartOfTodayCalendar()
        date.set(year, month - 1, day)
        val timestamp = Timestamp(date)
        return h.originalEntries.get(timestamp).notes == notes
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
            HabitBullCSVImporter(habitList, modelFactory, StandardLogging())
        )
        assertTrue(importer.canHandle(file))
        importer.importHabitsFromFile(file)
        file.delete()
    }
}
