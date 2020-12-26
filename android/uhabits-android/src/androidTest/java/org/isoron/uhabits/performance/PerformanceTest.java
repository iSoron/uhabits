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

package org.isoron.uhabits.performance;

import androidx.test.ext.junit.runners.*;
import androidx.test.filters.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.sqlite.*;
import org.junit.*;
import org.junit.runner.*;

import static org.isoron.uhabits.core.models.Timestamp.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class PerformanceTest extends BaseAndroidTest
{
    private Habit habit;

    @Override
    public void setUp()
    {
        super.setUp();
        habit = fixtures.createLongHabit();
    }

    @Ignore
    @Test(timeout = 5000)
    public void benchmarkCreateHabitCommand()
    {
        Database db = ((SQLModelFactory) modelFactory).getDatabase();
        db.beginTransaction();
        for (int i = 0; i < 1_000; i++)
        {
            Habit model = modelFactory.buildHabit();
            new CreateHabitCommand(modelFactory, habitList, model).execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Ignore
    @Test(timeout = 5000)
    public void benchmarkCreateRepetitionCommand()
    {
        Database db = ((SQLModelFactory) modelFactory).getDatabase();
        db.beginTransaction();
        Habit habit = fixtures.createEmptyHabit();
        for (int i = 0; i < 5_000; i++)
        {
            Timestamp timestamp = new Timestamp(i * DAY_LENGTH);
            new CreateRepetitionCommand(habitList, habit, timestamp, 1).execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
