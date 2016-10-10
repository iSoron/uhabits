/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.io;

import android.support.annotation.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

/**
 * Class that exports the application data to CSV files.
 */
public class HabitsCSVExporter
{
    private List<Habit> selectedHabits;

    private List<String> generateDirs;

    private List<String> generateFilenames;

    private String exportDirName;
    /**
     * Delimiter used in a CSV file.
     */
    private final String DELIMITER = ",";

    @NonNull
    private final HabitList allHabits;

    public HabitsCSVExporter(@NonNull HabitList allHabits,
                             @NonNull List<Habit> selectedHabits,
                             @NonNull File dir)
    {
        this.allHabits = allHabits;
        this.selectedHabits = selectedHabits;
        this.exportDirName = dir.getAbsolutePath() + "/";

        generateDirs = new LinkedList<>();
        generateFilenames = new LinkedList<>();
    }

    public String writeArchive() throws IOException
    {
        String zipFilename;

        writeHabits();
        zipFilename = writeZipFile();
        cleanup();

        return zipFilename;
    }

    private void addFileToZip(ZipOutputStream zos, String filename)
        throws IOException
    {
        FileInputStream fis =
            new FileInputStream(new File(exportDirName + filename));
        ZipEntry ze = new ZipEntry(filename);
        zos.putNextEntry(ze);

        int length;
        byte bytes[] = new byte[1024];
        while ((length = fis.read(bytes)) >= 0) zos.write(bytes, 0, length);

        zos.closeEntry();
        fis.close();
    }

    private void cleanup()
    {
        for (String filename : generateFilenames)
            new File(exportDirName + filename).delete();

        for (String filename : generateDirs)
            new File(exportDirName + filename).delete();

        new File(exportDirName).delete();
    }

    @NonNull
    private String sanitizeFilename(String name)
    {
        String s = name.replaceAll("[^ a-zA-Z0-9\\._-]+", "");
        return s.substring(0, Math.min(s.length(), 100));
    }

    private void writeHabits() throws IOException
    {
        String filename = "Habits.csv";
        new File(exportDirName).mkdirs();
        FileWriter out = new FileWriter(exportDirName + filename);
        generateFilenames.add(filename);
        allHabits.writeCSV(out);
        out.close();

        for (Habit h : selectedHabits)
        {
            String sane = sanitizeFilename(h.getName());
            String habitDirName =
                String.format("%03d %s", allHabits.indexOf(h) + 1, sane);
            habitDirName = habitDirName.trim() + "/";

            new File(exportDirName + habitDirName).mkdirs();
            generateDirs.add(habitDirName);

            writeScores(habitDirName, h.getScores());
            writeCheckmarks(habitDirName, h.getCheckmarks());
        }

        writeMultipleHabits();
    }

    private void writeScores(String habitDirName, ScoreList scores)
        throws IOException
    {
        String path = habitDirName + "Scores.csv";
        FileWriter out = new FileWriter(exportDirName + path);
        generateFilenames.add(path);
        scores.writeCSV(out);
        out.close();
    }

    private void writeCheckmarks(String habitDirName, CheckmarkList checkmarks)
        throws IOException
    {
        String filename = habitDirName + "Checkmarks.csv";
        FileWriter out = new FileWriter(exportDirName + filename);
        generateFilenames.add(filename);
        checkmarks.writeCSV(out);
        out.close();
    }

    /**
     * Writes a scores file and a checkmarks file containing scores and checkmarks of every habit.
     * The first column corresponds to the date. Subsequent columns correspond to a habit.
     * Habits are taken from the list of selected habits.
     * Dates are determined from the oldest repetition date to the newest repetition date found in
     * the list of habits.
     *
     * @throws IOException if there was problem writing the files
     */
    private void writeMultipleHabits() throws IOException
    {
        String scoresFileName = "Scores.csv";
        String checksFileName = "Checkmarks.csv";
        generateFilenames.add(scoresFileName);
        generateFilenames.add(checksFileName);
        FileWriter scoresWriter = new FileWriter(exportDirName + scoresFileName);
        FileWriter checksWriter = new FileWriter(exportDirName + checksFileName);

        writeMultipleHabitsHeader(scoresWriter);
        writeMultipleHabitsHeader(checksWriter);

        long[] timeframe = getTimeframe();
        long oldest = timeframe[0];
        long newest = DateUtils.getStartOfToday();

        List<int[]> checkmarks = new ArrayList<>();
        List<int[]> scores = new ArrayList<>();
        for (Habit h : selectedHabits)
        {
            checkmarks.add(h.getCheckmarks().getValues(oldest, newest));
            scores.add(h.getScores().getValues(oldest, newest));
        }

        int days = DateUtils.getDaysBetween(oldest, newest);
        SimpleDateFormat dateFormat = DateFormats.getCSVDateFormat();
        for (int i = 0; i <= days; i++)
        {
            Date day = new Date(newest - i * DateUtils.millisecondsInOneDay);

            String date = dateFormat.format(day);
            StringBuilder sb = new StringBuilder();
            sb.append(date).append(DELIMITER);
            checksWriter.write(sb.toString());
            scoresWriter.write(sb.toString());

            for(int j = 0; j < selectedHabits.size(); j++)
            {
                checksWriter.write(String.valueOf(checkmarks.get(j)[i]));
                checksWriter.write(DELIMITER);
                String score =
                        String.format("%.4f", ((float) scores.get(j)[i]) / Score.MAX_VALUE);
                scoresWriter.write(score);
                scoresWriter.write(DELIMITER);
            }
            checksWriter.write("\n");
            scoresWriter.write("\n");
        }
        scoresWriter.close();
        checksWriter.close();
    }

    /**
     * Writes the first row, containing header information, using the given writer.
     * This consists of the date title and the names of the selected habits.
     *
     * @param out the writer to use
     * @throws IOException if there was a problem writing
     */
    private void writeMultipleHabitsHeader(Writer out) throws IOException
    {
        out.write("Date" + DELIMITER);
        for (Habit h : selectedHabits) {
            out.write(h.getName());
            out.write(DELIMITER);
        }
        out.write("\n");
    }

    /**
     * Gets the overall timeframe of the selected habits.
     * The timeframe is an array containing the oldest timestamp among the habits and the
     * newest timestamp among the habits.
     * Both timestamps are in milliseconds.
     *
     * @return the timeframe containing the oldest timestamp and the newest timestamp
     */
    private long[] getTimeframe()
    {
        long oldest = Long.MAX_VALUE;
        long newest = -1;
        for (Habit h : selectedHabits)
        {
            if(h.getRepetitions().getOldest() == null || h.getRepetitions().getNewest() == null)
                continue;
            long currOld = h.getRepetitions().getOldest().getTimestamp();
            long currNew = h.getRepetitions().getNewest().getTimestamp();
            oldest = currOld > oldest ? oldest : currOld;
            newest = currNew < newest ? newest : currNew;
        }
        return new long[]{oldest, newest};
    }

    private String writeZipFile() throws IOException
    {
        SimpleDateFormat dateFormat = DateFormats.getCSVDateFormat();
        String date = dateFormat.format(DateUtils.getStartOfToday());
        String zipFilename =
            String.format("%s/Loop Habits CSV %s.zip", exportDirName, date);

        FileOutputStream fos = new FileOutputStream(zipFilename);
        ZipOutputStream zos = new ZipOutputStream(fos);

        for (String filename : generateFilenames)
            addFileToZip(zos, filename);

        zos.close();
        fos.close();

        return zipFilename;
    }
}
