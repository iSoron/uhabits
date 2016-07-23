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

import org.isoron.uhabits.*;
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

    @NonNull
    private final HabitList allHabits;

    public HabitsCSVExporter(@NonNull HabitList allHabits,
                             @NonNull List<Habit> selectedHabits,
                             @NonNull File dir)
    {
        HabitsApplication.getComponent().inject(this);

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

    private void writeCheckmarks(String habitDirName, CheckmarkList checkmarks)
        throws IOException
    {
        String filename = habitDirName + "Checkmarks.csv";
        FileWriter out = new FileWriter(exportDirName + filename);
        generateFilenames.add(filename);
        checkmarks.writeCSV(out);
        out.close();
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
