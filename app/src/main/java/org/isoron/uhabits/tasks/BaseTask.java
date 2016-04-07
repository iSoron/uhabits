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

import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.TimeoutException;

public abstract class BaseTask extends AsyncTask<Void, Integer, Void>
{
    private static int activeTaskCount;

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        activeTaskCount++;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        activeTaskCount--;
        super.onPostExecute(null);
    }

    @Override
    protected final Void doInBackground(Void... params)
    {
        doInBackground();
        return null;
    }

    protected abstract void doInBackground();

    public static void waitForTasks(long timeout)
            throws TimeoutException, InterruptedException
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            throw new UnsupportedOperationException("waitForTasks requires API 16+");

        int poolInterval = 100;

        while(timeout > 0)
        {
            if(activeTaskCount == 0) return;

            timeout -= poolInterval;
            Thread.sleep(poolInterval);
        }

        throw new TimeoutException();
    }
}
