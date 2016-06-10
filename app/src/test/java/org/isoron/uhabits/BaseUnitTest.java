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

import org.isoron.uhabits.models.HabitFixtures;
import org.isoron.uhabits.models.HabitList;
import org.isoron.uhabits.models.ModelFactory;
import org.isoron.uhabits.ui.habits.list.model.HabitCardListCache;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.Preferences;
import org.junit.After;
import org.junit.Before;

import javax.inject.Inject;

public class BaseUnitTest
{
    // 8:00am, January 25th, 2015 (UTC)
    public static final long FIXED_LOCAL_TIME = 1422172800000L;

    @Inject
    protected Preferences prefs;

    @Inject
    protected HabitCardListCache listCache;

    @Inject
    protected ModelFactory modelFactory;

    protected TestComponent testComponent;

    @Inject
    protected HabitList habitList;

    protected HabitFixtures fixtures;

    @Before
    public void setUp()
    {
        DateUtils.setFixedLocalTime(FIXED_LOCAL_TIME);
        testComponent = DaggerTestComponent.builder().build();
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

    public void log(String format, Object... args)
    {
        System.out.println(String.format(format, args));
    }
}
