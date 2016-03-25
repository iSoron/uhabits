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

package org.isoron.uhabits.unit.tasks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ProgressBar;

import org.isoron.uhabits.helpers.DatabaseHelper;
import org.isoron.uhabits.tasks.ImportDataTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ImportDataTaskTest
{
    private Context context;
    private File baseDir;

    @Before
    public void setup()
    {
        context = InstrumentationRegistry.getContext();

        baseDir = DatabaseHelper.getFilesDir("Backups");
        if(baseDir == null) fail("baseDir should not be null");
    }

    private void copyAssetToFile(String assetPath, File dst) throws IOException
    {
        InputStream in = context.getAssets().open(assetPath);
        DatabaseHelper.copy(in, dst);
    }

    private void assertTaskResult(final int expectedResult, String assetFilename)
            throws IOException, InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        ImportDataTask task = createTask(assetFilename);

        task.setListener(new ImportDataTask.Listener()
        {
            @Override
            public void onImportFinished(int result)
            {
                assertThat(result, equalTo(expectedResult));
                latch.countDown();
            }
        });

        task.execute();
        latch.await(30, TimeUnit.SECONDS);
    }

    @NonNull
    private ImportDataTask createTask(String assetFilename) throws IOException
    {
        ProgressBar bar = new ProgressBar(context);
        File file = new File(String.format("%s/%s", baseDir.getPath(), assetFilename));
        copyAssetToFile(assetFilename, file);

        return new ImportDataTask(file, bar);
    }

    @Test
    public void importInvalidData() throws Throwable
    {
        assertTaskResult(ImportDataTask.NOT_RECOGNIZED, "icon.png");
    }

    @Test
    public void importValidData() throws Throwable
    {
        assertTaskResult(ImportDataTask.SUCCESS, "loop.db");
    }
}
