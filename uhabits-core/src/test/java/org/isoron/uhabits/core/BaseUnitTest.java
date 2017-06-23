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

package org.isoron.uhabits.core;

import android.support.annotation.*;

import org.apache.commons.io.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.memory.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.test.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.junit.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BaseUnitTest
{

    // 8:00am, January 25th, 2015 (UTC)
    protected static final long FIXED_LOCAL_TIME = 1422172800000L;

    protected HabitList habitList;

    protected HabitFixtures fixtures;

    protected ModelFactory modelFactory;

    protected SingleThreadTaskRunner taskRunner;

    protected CommandRunner commandRunner;

    protected DatabaseOpener databaseOpener = new DatabaseOpener()
    {
        @Override
        public Database open(@NonNull File file)
        {
            try
            {
                return new JdbcDatabase(DriverManager.getConnection(
                    String.format("jdbc:sqlite:%s", file.getAbsolutePath())));
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    };

    @Before
    public void setUp() throws Exception
    {
        DateUtils.setFixedLocalTime(FIXED_LOCAL_TIME);

        modelFactory = new MemoryModelFactory();
        habitList = spy(modelFactory.buildHabitList());
        fixtures = new HabitFixtures(modelFactory, habitList);
        taskRunner = new SingleThreadTaskRunner();
        commandRunner = new CommandRunner(taskRunner);
    }

    @After
    public void tearDown() throws Exception
    {
        validateMockitoUsage();
        DateUtils.setFixedLocalTime(null);
    }

    public long timestamp(int year, int month, int day)
    {
        GregorianCalendar cal = DateUtils.getStartOfTodayCalendar();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }

    @Test
    public void nothing()
    {

    }

    protected Database buildMemoryDatabase()
    {
        try
        {
            Database db = new JdbcDatabase(
                DriverManager.getConnection("jdbc:sqlite::memory:"));
            db.execute("pragma user_version=8;");
            MigrationHelper helper = new MigrationHelper(db);
            helper.migrateTo(21);
            return db;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void copyAssetToFile(String assetPath, File dst)
        throws IOException
    {
        IOUtils.copy(openAsset(assetPath), new FileOutputStream(dst));
    }

    @NonNull
    protected InputStream openAsset(String assetPath) throws IOException
    {
        InputStream in = getClass().getResourceAsStream(assetPath);
        if (in != null) return in;

        String basePath = "uhabits-core/src/test/resources/";
        File file = new File(basePath + assetPath);
        if (file.exists() && file.canRead()) in = new FileInputStream(file);
        if (in != null) return in;

        basePath = "src/test/resources/";
        file = new File(basePath + assetPath);
        if (file.exists() && file.canRead()) in = new FileInputStream(file);
        if (in != null) return in;

        throw new IllegalStateException("asset not found: " + assetPath);
    }

    protected Database openDatabaseResource(String path) throws IOException
    {
        InputStream original = openAsset(path);
        File tmpDbFile = File.createTempFile("database", ".db");
        tmpDbFile.deleteOnExit();
        IOUtils.copy(original, new FileOutputStream(tmpDbFile));
        return databaseOpener.open(tmpDbFile);
    }
}
