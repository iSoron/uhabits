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
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HabitsCSVExporterTest : BaseUnitTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        habitList.add(fixtures.createShortHabit())
        habitList.add(fixtures.createEmptyHabit())
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

    @Test
    fun testWideCheckmarksIncludeOldestComputedEntry() = runTest {
        val habit = modelFactory.buildHabit()
        habit.name = "Non daily"
        habit.frequency = Frequency(1, 3)
        habit.type = HabitType.YES_NO
        val today = getToday()
        habit.originalEntries.add(Entry(today.minus(4), Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(9), Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(10), Entry.YES_MANUAL))
        habit.recompute()

        val oldestComputed = habit.computedEntries.getKnown().last().date
        assertEquals(LocalDate(2015, 1, 13), oldestComputed)

        habitList.add(habit)
        val exporter = HabitsCSVExporter(habitList, listOf(habit))
        val bytes = exporter.writeArchive()

        val entries = ZipReader(bytes).entries()

        val perHabit = entries.find { it.name.endsWith("Checkmarks.csv") && it.name != "Checkmarks.csv" }
        assertNotNull(perHabit, "per-habit Checkmarks.csv should exist in zip")
        assertTrue(
            perHabit.content.lines().any { it.startsWith(oldestComputed.toCSVString() + ",") },
            "per-habit Checkmarks.csv should contain $oldestComputed"
        )

        val wide = entries.find { it.name == "Checkmarks.csv" }
        assertNotNull(wide, "wide Checkmarks.csv should exist in zip")
        val wideDates = wide.content.trimEnd().lines().drop(1).map { it.substringBefore(",") }
        assertTrue(
            wideDates.contains(oldestComputed.toCSVString()),
            "wide Checkmarks.csv should contain the oldest computed date $oldestComputed"
        )
    }
}
