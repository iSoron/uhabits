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

import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.EntryList
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateFormats
import org.isoron.uhabits.core.utils.DateUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import java.util.ArrayList
import java.util.LinkedList
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.min

/**
 * Class that exports the application data to CSV files.
 */
class HabitsCSVExporter(
    private val allHabits: HabitList,
    private val selectedHabits: List<Habit>,
    dir: File
) {
    private val generatedDirs = LinkedList<String>()
    private val generatedFilenames = LinkedList<String>()
    private val exportDirName: String = dir.absolutePath + "/"
    private val delimiter = ","

    fun writeArchive(): String {
        writeHabits()
        val zipFilename = writeZipFile()
        cleanup()
        return zipFilename
    }

    private fun addFileToZip(zos: ZipOutputStream, filename: String) {
        val fis = FileInputStream(File(exportDirName + filename))
        val ze = ZipEntry(filename)
        zos.putNextEntry(ze)
        var length: Int
        val bytes = ByteArray(1024)
        while (fis.read(bytes).also { length = it } >= 0) zos.write(bytes, 0, length)
        zos.closeEntry()
        fis.close()
    }

    private fun cleanup() {
        for (filename in generatedFilenames) File(exportDirName + filename).delete()
        for (filename in generatedDirs) File(exportDirName + filename).delete()
        File(exportDirName).delete()
    }

    private fun sanitizeFilename(name: String): String {
        val s = name.replace("[^ a-zA-Z0-9._-]+".toRegex(), "")
        return s.substring(0, min(s.length, 100))
    }

    private fun writeHabits() {
        val filename = "Habits.csv"
        File(exportDirName).mkdirs()
        val out = FileWriter(exportDirName + filename)
        generatedFilenames.add(filename)
        allHabits.writeCSV(out)
        out.close()
        for (h in selectedHabits) {
            val sane = sanitizeFilename(h.name)
            var habitDirName = String.format(Locale.US, "%03d %s", allHabits.indexOf(h) + 1, sane)
            habitDirName = habitDirName.trim() + "/"
            File(exportDirName + habitDirName).mkdirs()
            generatedDirs.add(habitDirName)
            writeScores(habitDirName, h)
            writeEntries(habitDirName, h.computedEntries)
        }
        writeMultipleHabits()
    }

    private fun writeScores(habitDirName: String, habit: Habit) {
        val path = habitDirName + "Scores.csv"
        val out = FileWriter(exportDirName + path)
        generatedFilenames.add(path)
        val dateFormat = DateFormats.getCSVDateFormat()
        val today = DateUtils.getTodayWithOffset()
        var oldest = today
        val known = habit.computedEntries.getKnown()
        if (known.isNotEmpty()) oldest = known[known.size - 1].timestamp
        for ((timestamp1, value) in habit.scores.getByInterval(oldest, today)) {
            val timestamp = dateFormat.format(timestamp1.unixTime)
            val score = String.format(Locale.US, "%.4f", value)
            out.write(String.format("%s,%s\n", timestamp, score))
        }
        out.close()
    }

    private fun writeEntries(habitDirName: String, entries: EntryList) {
        val filename = habitDirName + "Checkmarks.csv"
        val out = FileWriter(exportDirName + filename)
        generatedFilenames.add(filename)
        val dateFormat = DateFormats.getCSVDateFormat()
        for ((timestamp, value) in entries.getKnown()) {
            val date = dateFormat.format(timestamp.toJavaDate())
            out.write(String.format(Locale.US, "%s,%d\n", date, value))
        }
        out.close()
    }

    /**
     * Writes a scores file and a checkmarks file containing scores and checkmarks of every habit.
     * The first column corresponds to the date. Subsequent columns correspond to a habit.
     * Habits are taken from the list of selected habits.
     * Dates are determined from the oldest repetition date to the newest repetition date found in
     * the list of habits.
     */
    private fun writeMultipleHabits() {
        val scoresFileName = "Scores.csv"
        val checksFileName = "Checkmarks.csv"
        generatedFilenames.add(scoresFileName)
        generatedFilenames.add(checksFileName)

        val scoresWriter = FileWriter(exportDirName + scoresFileName)
        val checksWriter = FileWriter(exportDirName + checksFileName)
        writeMultipleHabitsHeader(scoresWriter)
        writeMultipleHabitsHeader(checksWriter)

        val timeframe = getTimeframe()
        val oldest = timeframe[0]
        val newest = DateUtils.getToday()
        val checkmarks: MutableList<ArrayList<Entry>> = ArrayList()
        val scores: MutableList<ArrayList<Score>> = ArrayList()
        for (habit in selectedHabits) {
            checkmarks.add(ArrayList(habit.computedEntries.getByInterval(oldest, newest)))
            scores.add(ArrayList(habit.scores.getByInterval(oldest, newest)))
        }

        val days = oldest.daysUntil(newest)
        val dateFormat = DateFormats.getCSVDateFormat()
        for (i in 0..days) {
            val day = newest.minus(i).toJavaDate()
            val date = dateFormat.format(day)
            val sb = StringBuilder()
            sb.append(date).append(delimiter)
            checksWriter.write(sb.toString())
            scoresWriter.write(sb.toString())
            for (j in selectedHabits.indices) {
                checksWriter.write(checkmarks[j][i].value.toString())
                checksWriter.write(delimiter)
                val score = String.format(Locale.US, "%.4f", scores[j][i].value)
                scoresWriter.write(score)
                scoresWriter.write(delimiter)
            }
            checksWriter.write("\n")
            scoresWriter.write("\n")
        }
        scoresWriter.close()
        checksWriter.close()
    }

    /**
     * Writes the first row, containing header information, using the given writer.
     * This consists of the date title and the names of the selected habits.
     *
     * @param out the writer to use
     * @throws IOException if there was a problem writing
     */
    @Throws(IOException::class)
    private fun writeMultipleHabitsHeader(out: Writer) {
        out.write("Date$delimiter")
        for (habit in selectedHabits) {
            out.write(habit.name)
            out.write(delimiter)
        }
        out.write("\n")
    }

    /**
     * Gets the overall timeframe of the selected habits.
     * The timeframe is an array containing the oldest timestamp among the habits and the
     * newest timestamp among the habits.
     * Both timestamps are in milliseconds.
     *
     * @return the timeframe containing the oldest timestamp and the newest timestamp
     */
    private fun getTimeframe(): Array<Timestamp> {
        var oldest = Timestamp.ZERO.plus(1000000)
        var newest = Timestamp.ZERO
        for (habit in selectedHabits) {
            val entries = habit.originalEntries.getKnown()
            if (entries.isEmpty()) continue
            val currNew = entries[0].timestamp
            val currOld = entries[entries.size - 1].timestamp
            oldest = if (currOld.isOlderThan(oldest)) currOld else oldest
            newest = if (currNew.isNewerThan(newest)) currNew else newest
        }
        return arrayOf(oldest, newest)
    }

    private fun writeZipFile(): String {
        val dateFormat = DateFormats.getCSVDateFormat()
        val date = dateFormat.format(DateUtils.getStartOfToday())
        val zipFilename = String.format("%s/Loop Habits CSV %s.zip", exportDirName, date)
        val fos = FileOutputStream(zipFilename)
        val zos = ZipOutputStream(fos)
        for (filename in generatedFilenames) addFileToZip(zos, filename)
        zos.close()
        fos.close()
        return zipFilename
    }
}
