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

import android.content.*;
import android.support.test.*;
import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import static junit.framework.Assert.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class HabitsCSVExporterTest extends BaseAndroidTest
{
    private File baseDir;

    @Before
    public void setUp()
    {
        super.setUp();

        fixtures.purgeHabits(habitList);
        fixtures.createShortHabit();
        fixtures.createEmptyHabit();

        Context targetContext = InstrumentationRegistry.getTargetContext();
        baseDir = targetContext.getCacheDir();
    }

    @Test
    public void testExportCSV() throws IOException
    {
        List<Habit> selected = new LinkedList<>();
        for (Habit h : habitList) selected.add(h);

        HabitsCSVExporter exporter =
            new HabitsCSVExporter(habitList, selected, baseDir);
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
        assertPathExists("Checkmarks.csv");
        assertPathExists("Scores.csv");
    }

    private void assertAbsolutePathExists(String s)
    {
        File file = new File(s);
        assertTrue(
            String.format("File %s should exist", file.getAbsolutePath()),
            file.exists());
    }

    private void assertPathExists(String s)
    {
        assertAbsolutePathExists(
            String.format("%s/%s", baseDir.getAbsolutePath(), s));
    }

    private void unzip(File file) throws IOException
    {
        ZipFile zip = new ZipFile(file);
        Enumeration<? extends ZipEntry> e = zip.entries();

        while (e.hasMoreElements())
        {
            ZipEntry entry = e.nextElement();
            InputStream stream = zip.getInputStream(entry);

            String outputFilename =
                String.format("%s/%s", baseDir.getAbsolutePath(),
                    entry.getName());
            File outputFile = new File(outputFilename);

            File parent = outputFile.getParentFile();
            if (parent != null) parent.mkdirs();

            FileUtils.copy(stream, outputFile);
        }

        zip.close();
    }
}
