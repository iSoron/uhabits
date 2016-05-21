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

import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.CheckmarkList;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.ScoreList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HabitsCSVExporter
{
    private List<Habit> habits;

    private List<String> generateDirs;
    private List<String> generateFilenames;

    private String exportDirName;

    public HabitsCSVExporter(List<Habit> habits, File dir)
    {
        this.habits = habits;
        this.exportDirName = dir.getAbsolutePath() + "/";

        generateDirs = new LinkedList<>();
        generateFilenames = new LinkedList<>();
    }

    private void writeHabits() throws IOException
    {
        String filename = "Habits.csv";
        new File(exportDirName).mkdirs();
        FileWriter out = new FileWriter(exportDirName + filename);
        generateFilenames.add(filename);
        Habit.writeCSV(habits, out);
        out.close();

        //my contribution
		String filename2 = "AllCheckmarks.csv";
		new File(exportDirName).mkdirs();
		FileWriter out2 = new FileWriter(exportDirName + filename2);
		generateFilenames.add(filename2);
        CheckmarkList check = new CheckmarkList();
        check.writeCSVMultipleHabits (habits, out2);
		out2.close();
		//until here



        for(Habit h : habits)
        {
            String habitDirName = String.format("%03d %s/", h.position + 1, h.name);
            new File(exportDirName + habitDirName).mkdirs();
            generateDirs.add(habitDirName);

            writeScores(habitDirName, h.scores);
            writeCheckmarks(habitDirName, h.checkmarks);
        }
    }

    private void writeScores(String habitDirName, ScoreList scores) throws IOException
    {
        String path = habitDirName + "Scores.csv";
        FileWriter out = new FileWriter(exportDirName + path);
        generateFilenames.add(path);
        scores.writeCSV(out);
        out.close();
    }

    private void writeCheckmarks(String habitDirName, CheckmarkList checkmarks) throws IOException
    {
        String filename = habitDirName + "Checkmarks.csv";
        FileWriter out = new FileWriter(exportDirName + filename);
        generateFilenames.add(filename);
        checkmarks.writeCSV(out);
        out.close();
    }

    private String writeZipFile() throws IOException
    {
        SimpleDateFormat dateFormat = DateHelper.getCSVDateFormat();
        String date = dateFormat.format(DateHelper.getStartOfToday());
        String zipFilename = String.format("%s/Loop Habits CSV %s.zip", exportDirName, date);

        FileOutputStream fos = new FileOutputStream(zipFilename);
        ZipOutputStream zos = new ZipOutputStream(fos);

        for(String filename : generateFilenames)
            addFileToZip(zos, filename);

        zos.close();
        fos.close();

        return zipFilename;
    }

    private void addFileToZip(ZipOutputStream zos, String filename) throws IOException
    {
        FileInputStream fis = new FileInputStream(new File(exportDirName + filename));
        ZipEntry ze = new ZipEntry(filename);
        zos.putNextEntry(ze);

        int length;
        byte bytes[] = new byte[1024];
        while((length = fis.read(bytes)) >= 0)
            zos.write(bytes, 0, length);

        zos.closeEntry();
        fis.close();
    }

    public String writeArchive() throws IOException
    {
        String zipFilename;

        writeHabits();
        zipFilename = writeZipFile();
        cleanup();

        return zipFilename;
    }

    private void cleanup()
    {
        for(String filename : generateFilenames)
            new File(exportDirName + filename).delete();

        for(String filename : generateDirs)
            new File(exportDirName + filename).delete();

        new File(exportDirName).delete();
    }
}
