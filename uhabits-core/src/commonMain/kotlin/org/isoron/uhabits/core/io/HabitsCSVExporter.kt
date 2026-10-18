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

import org.isoron.platform.io.ZipWriter
import org.isoron.platform.io.csvLine
import org.isoron.platform.io.format
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.models.EntryList
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import kotlin.math.min

/**
 * Class that exports the application data to CSV files inside a ZIP archive.
 */
class HabitsCSVExporter(
    private val allHabits: HabitList,
    private val selectedHabits: List<Habit>
) {
    private val delimiter = ","

    suspend fun writeArchive(): ByteArray {
        val zip = ZipWriter()
        zip.addEntry("Habits.csv", allHabits.writeCSV())
        for (h in selectedHabits) {
            val habitDirName = habitDirName(h)
            zip.addEntry("${habitDirName}Scores.csv", writeScores(h))
            zip.addEntry("${habitDirName}Checkmarks.csv", writeEntries(h.computedEntries))
        }
        zip.addEntry("Scores.csv", writeMultipleHabitsScores())
        zip.addEntry("Checkmarks.csv", writeMultipleHabitsCheckmarks())
        return zip.toBytes()
    }

    private fun habitDirName(h: Habit): String {
        val sane = sanitizeFilename(h.name)
        return format("%03d", allHabits.indexOf(h) + 1) + " " + sane.trim() + "/"
    }

    private fun sanitizeFilename(name: String): String {
        val s = name.replace("[^ a-zA-Z0-9._-]+".toRegex(), "")
        return s.substring(0, min(s.length, 100))
    }

    private fun writeScores(habit: Habit): String {
        val sb = StringBuilder()
        val today = getToday()
        var oldest = today
        val known = habit.computedEntries.getKnown()
        if (known.isNotEmpty()) oldest = known[known.size - 1].date
        sb.append(csvLine(arrayOf("Date", "Score")))
        for (s in habit.scores.getByInterval(oldest, today)) {
            sb.append(csvLine(arrayOf(s.date.toCSVString(), format("%.4f", s.value))))
        }
        return sb.toString()
    }

    private fun writeEntries(entries: EntryList): String {
        val sb = StringBuilder()
        sb.append(csvLine(arrayOf("Date", "Value", "Notes")))
        for (entry in entries.getKnown()) {
            sb.append(csvLine(arrayOf(entry.date.toCSVString(), entry.formattedValue, entry.notes)))
        }
        return sb.toString()
    }

    private fun writeMultipleHabitsScores(): String {
        val sb = StringBuilder()
        writeMultipleHabitsHeader(sb)
        val timeframe = getTimeframe()
        val oldest = timeframe[0]
        val newest = getToday()
        val scores = selectedHabits.map { ArrayList(it.scores.getByInterval(oldest, newest)) }
        val days = oldest.daysUntil(newest)
        for (i in 0..days) {
            val date = newest.minus(i).toCSVString()
            sb.append(date).append(delimiter)
            for (j in selectedHabits.indices) {
                val score = format("%.4f", scores[j][i].value)
                sb.append(score).append(delimiter)
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun writeMultipleHabitsCheckmarks(): String {
        val sb = StringBuilder()
        writeMultipleHabitsHeader(sb)
        val timeframe = getTimeframe()
        val oldest = timeframe[0]
        val newest = getToday()
        val checkmarks = selectedHabits.map { ArrayList(it.computedEntries.getByInterval(oldest, newest)) }
        val days = oldest.daysUntil(newest)
        for (i in 0..days) {
            val date = newest.minus(i).toCSVString()
            sb.append(date).append(delimiter)
            for (j in selectedHabits.indices) {
                sb.append(checkmarks[j][i].formattedValue).append(delimiter)
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun writeMultipleHabitsHeader(sb: StringBuilder) {
        val fields = arrayOf("Date") + selectedHabits.map { it.name } + arrayOf("")
        sb.append(csvLine(fields))
    }

    private fun getTimeframe(): Array<LocalDate> {
        var oldest = LocalDate(1000000)
        var newest = LocalDate(0)
        for (habit in selectedHabits) {
            val entries = habit.computedEntries.getKnown()
            if (entries.isEmpty()) continue
            val currNew = entries[0].date
            val currOld = entries[entries.size - 1].date
            oldest = if (currOld.isOlderThan(oldest)) currOld else oldest
            newest = if (currNew.isNewerThan(newest)) currNew else newest
        }
        return arrayOf(oldest, newest)
    }
}
