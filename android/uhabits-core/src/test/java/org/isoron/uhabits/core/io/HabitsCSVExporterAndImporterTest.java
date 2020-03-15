/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.isoron.uhabits.core.BaseUnitTest;
import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.core.models.HabitList;
import org.isoron.uhabits.core.models.memory.MemoryModelFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class HabitsCSVExporterAndImporterTest extends BaseUnitTest
{
    private File baseDir;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        habitList.add(fixtures.createShortHabit());
        habitList.add(fixtures.createEmptyHabit());
        baseDir = Files.createTempDirectory("csv").toFile();
        assertNotNull(baseDir);
    }

    @Override
    public void tearDown() throws Exception
    {
        FileUtils.deleteDirectory(baseDir);
        super.tearDown();
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

    @Test
    public void testImportCSV() throws IOException {
        List<Habit> selected = new LinkedList<>();
        for (Habit h : habitList) selected.add(h);

        HabitsCSVExporter exporter =
                new HabitsCSVExporter(habitList, selected, baseDir);
        File file = new File(exporter.writeArchive());

        HabitList oldHabits = habitList;

        // reset model before importing
        modelFactory = new MemoryModelFactory();
        habitList = spy(modelFactory.buildHabitList());

        HabitsCSVImporter importer = new HabitsCSVImporter(habitList, modelFactory);
        assertTrue(importer.canHandle(file));

        importer.importHabitsFromFile(file);

        String fixed = stringRepresentation(oldHabits);
        String updated = stringRepresentation(habitList);

        // equals methods are too strict for this purpose
        assertEquals(fixed, updated);
    }

    @NotNull
    private String stringRepresentation(HabitList oldHabits) {
        assertEquals(2, oldHabits.size());
        return oldHabits.getByPosition(0).toString() + oldHabits.getByPosition(1).toString();
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
            File out = new File(outputFilename);
            File parent = out.getParentFile();
            if (parent != null) parent.mkdirs();

            IOUtils.copy(stream, new FileOutputStream(out));
        }

        zip.close();
    }

    private void assertPathExists(String s)
    {
        assertAbsolutePathExists(
            String.format("%s/%s", baseDir.getAbsolutePath(), s));
    }

    private void assertAbsolutePathExists(String s)
    {
        File file = new File(s);
        assertTrue(
            String.format("File %s should exist", file.getAbsolutePath()),
            file.exists());
    }
}
