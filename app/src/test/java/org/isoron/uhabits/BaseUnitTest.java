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

package org.isoron.uhabits;

import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.io.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import javax.inject.*;

public class BaseUnitTest
{
    // 8:00am, January 25th, 2015 (UTC)
    public static final long FIXED_LOCAL_TIME = 1422172800000L;

    @Inject
    protected Preferences prefs;

    @Inject
    protected ModelFactory modelFactory;

    @Inject
    protected DialogFactory dialogFactory;

    @Inject
    protected IntentFactory intentFactory;

    @Inject
    protected HabitList habitList;

    @Inject
    protected HabitLogger logger;

    @Inject
    protected PendingIntentFactory pendingIntentFactory;

    @Inject
    protected IntentScheduler intentScheduler;

    @Inject
    protected DirFinder dirFinder;

    @Inject
    protected CommandRunner commandRunner;

    protected TestComponent testComponent;

    protected HabitFixtures fixtures;

    public void log(String format, Object... args)
    {
        System.out.println(String.format(format, args));
    }

    @Before
    public void setUp()
    {
        DateUtils.setFixedLocalTime(FIXED_LOCAL_TIME);
        testComponent = DaggerTestComponent.create();
        HabitsApplication.setComponent(testComponent);
        testComponent.inject(this);
        fixtures = new HabitFixtures(habitList);
    }

    @After
    public void tearDown()
    {
        DateUtils.setFixedLocalTime(null);
        fixtures.purgeHabits();

    }
}
