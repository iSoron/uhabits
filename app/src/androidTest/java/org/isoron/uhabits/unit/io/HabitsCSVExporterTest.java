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

package org.isoron.uhabits.unit.io;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.isoron.uhabits.BaseTest;
import org.isoron.uhabits.helpers.DatabaseHelper;
import org.isoron.uhabits.io.HabitsCSVExporter;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.unit.HabitFixtures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class HabitsCSVExporterTest extends BaseTest
{
    private File baseDir;

    @Before
    public void setup()
    {
        super.setup();

        HabitFixtures.purgeHabits();
        HabitFixtures.createNonDailyHabit();
        HabitFixtures.createEmptyHabit();

        Context targetContext = InstrumentationRegistry.getTargetContext();
        baseDir = targetContext.getCacheDir();
    }

    private void unzip(File file) throws IOException
    {
        ZipFile zip = new ZipFile(file);
        Enumeration<? extends ZipEntry> e = zip.entries();

        while(e.hasMoreElements())
        {
            ZipEntry entry = e.nextElement();
            InputStream stream = zip.getInputStream(entry);

            String outputFilename = String.format("%s/%s", baseDir.getAbsolutePath(),
                    entry.getName());
            File outputFile = new File(outputFilename);

            File parent = outputFile.getParentFile();
            if(parent != null) parent.mkdirs();

            DatabaseHelper.copy(stream, outputFile);
        }

        zip.close();
    }

    @Test
    public void exportCSV() throws IOException
    {
        List<Habit> habits = Habit.getAll(true);

        HabitsCSVExporter exporter = new HabitsCSVExporter(habits, baseDir);
        String filename = exporter.writeArchive();
        assertAbsolutePathExists(filename);

        File archive = new File(filename);
        unzip(archive);

        assertPathExists("Habits.csv");
        assertPathExists("001 Wake up early");
        assertPathExists("001 Wake up early/Checkmarks.csv");
        assertPathExists("001 Wake up early/Scores.csv");
        assertPathExists("002 Meditate/Checkmarks.csv");
        assertPathExists("002 Meditate/Scores.csv");
    }

    private void assertPathExists(String s)
    {
        assertAbsolutePathExists(String.format("%s/%s", baseDir.getAbsolutePath(), s));
    }

    private void assertAbsolutePathExists(String s)
    {
        File file = new File(s);
        assertTrue(String.format("File %s should exist", file.getAbsolutePath()), file.exists());
    }
}
