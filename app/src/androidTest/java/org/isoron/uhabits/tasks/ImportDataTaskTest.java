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

package org.isoron.uhabits.tasks;

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ImportDataTaskTest extends BaseAndroidTest
{
    private File baseDir;

    @Before
    public void setUp()
    {
        super.setUp();

        baseDir = FileUtils.getFilesDir("Backups");
        if (baseDir == null) fail("baseDir should not be null");
    }

    @Test
    public void testImportInvalidData() throws Throwable
    {
        assertTaskResult(ImportDataTask.NOT_RECOGNIZED, "icon.png");
    }

    @Test
    public void testImportValidData() throws Throwable
    {
        assertTaskResult(ImportDataTask.SUCCESS, "loop.db");
    }

    private void assertTaskResult(final int expectedResult,
                                  String assetFilename) throws Throwable
    {
        File file = new File(baseDir.getPath() + "/" + assetFilename);
        copyAssetToFile(assetFilename, file);

        taskRunner.execute(new ImportDataTask(habitList, file,
            (result) -> assertThat(result, equalTo(expectedResult))));
    }

    private void copyAssetToFile(String assetPath, File dst) throws IOException
    {
        InputStream in = testContext.getAssets().open(assetPath);
        FileUtils.copy(in, dst);
    }
}
