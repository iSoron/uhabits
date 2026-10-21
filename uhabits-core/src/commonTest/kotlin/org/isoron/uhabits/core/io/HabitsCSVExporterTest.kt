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
package org.isoron.uhabits.core.io

import kotlinx.coroutines.test.runTest
import org.isoron.platform.io.ZipReader
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.PaletteColor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HabitsCSVExporterTest : BaseUnitTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        val today = getToday()

        val meditate = fixtures.createEmptyHabit()
        meditate.position = 1

        val short = fixtures.createShortHabit()
        short.position = 2

        val reading = modelFactory.buildHabit()
        reading.name = "Reading"
        reading.question = "Did you read today?"
        reading.type = HabitType.YES_NO
        reading.frequency = Frequency.DAILY
        reading.color = PaletteColor(6)
        reading.position = 3
        reading.category = "Books"

        val stretch = modelFactory.buildHabit()
        stretch.name = "Stretch"
        stretch.question = "Did you stretch today?"
        stretch.type = HabitType.YES_NO
        stretch.frequency = Frequency.DAILY
        stretch.color = PaletteColor(10)
        stretch.position = 4
        stretch.category = "Health, Wellness"

        val commaName = modelFactory.buildHabit()
        commaName.name = "Foo, Bar"
        commaName.question = "Did you do foo bar?"
        commaName.type = HabitType.YES_NO
        commaName.frequency = Frequency.DAILY
        commaName.color = PaletteColor(12)
        commaName.position = 5
        commaName.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        commaName.recompute()

        val nonDaily = modelFactory.buildHabit()
        nonDaily.name = "Night run"
        nonDaily.question = "Did you run at night?"
        nonDaily.type = HabitType.YES_NO
        nonDaily.frequency = Frequency(1, 3)
        nonDaily.color = PaletteColor(14)
        nonDaily.position = 6
        nonDaily.originalEntries.add(Entry(today.minus(4), Entry.YES_MANUAL))
        nonDaily.originalEntries.add(Entry(today.minus(9), Entry.YES_MANUAL))
        nonDaily.originalEntries.add(Entry(today.minus(10), Entry.YES_MANUAL))
        nonDaily.recompute()

        habitList.add(meditate)
        habitList.add(short)
        habitList.add(reading)
        habitList.add(stretch)
        habitList.add(commaName)
        habitList.add(nonDaily)
    }

    @Test
    fun testExportCSV() = runTest {
        val selected: MutableList<Habit> = mutableListOf()
        for (h in habitList) selected.add(h)
        val exporter = HabitsCSVExporter(habitList, selected)
        val bytes = exporter.writeArchive()
        assertTrue(bytes.isNotEmpty())

        val entries = ZipReader(bytes).entries()

        val filesToCheck = arrayOf(
            "001 Meditate/Checkmarks.csv",
            "001 Meditate/Scores.csv",
            "002 Wake up early/Checkmarks.csv",
            "002 Wake up early/Scores.csv",
            "003 Foo Bar/Checkmarks.csv",
            "003 Foo Bar/Scores.csv",
            "004 Night run/Checkmarks.csv",
            "004 Night run/Scores.csv",
            "005 Reading/Checkmarks.csv",
            "005 Reading/Scores.csv",
            "006 Stretch/Checkmarks.csv",
            "006 Stretch/Scores.csv",
            "Checkmarks.csv",
            "Habits.csv",
            "Scores.csv"
        )

        for (file in filesToCheck) {
            val entry = entries.find { it.name == file }
            assertNotNull(entry, "$file should exist in zip")
            val expected = fileOpener.openResourceFile("csv_export/$file").lines()
            assertEquals(expected, entry.content.trimEnd().lines(), "content mismatch for $file")
        }
    }
}
