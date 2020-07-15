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

package org.isoron.uhabits;

import android.content.*;

import androidx.test.uiautomator.*;

import com.linkedin.android.testbutler.*;

import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.uiautomator.UiDevice.*;

public class BaseUserInterfaceTest
{
    private static final String PKG = "org.isoron.uhabits";
    public static final String EMPTY_DESCRIPTION_HABIT_NAME = "Read books";

    public static UiDevice device;

    private HabitsApplicationComponent component;

    private HabitList habitList;

    private Preferences prefs;

    private HabitFixtures fixtures;

    private HabitCardListCache cache;

    public static void startActivity(Class cls)
    {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PKG, cls.getCanonicalName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    @Before
    public void setUp() throws Exception
    {
        device = getInstance(getInstrumentation());
        TestButler.setup(getApplicationContext());
        TestButler.verifyAnimationsDisabled(getApplicationContext());

        HabitsApplication app =
            (HabitsApplication) getApplicationContext().getApplicationContext();
        component = app.getComponent();
        habitList = component.getHabitList();
        prefs = component.getPreferences();
        cache = component.getHabitCardListCache();
        fixtures = new HabitFixtures(component.getModelFactory(), habitList);
        resetState();
    }

    @After
    public void tearDown() throws Exception
    {
        for (int i = 0; i < 10; i++) device.pressBack();
        TestButler.teardown(getApplicationContext());
    }

    private void resetState() throws Exception
    {
        prefs.clear();
        prefs.setFirstRun(false);
        prefs.updateLastHint(100, DateUtils.getToday());
        habitList.removeAll();
        cache.refreshAllHabits();
        Thread.sleep(1000);

        Habit h1 = fixtures.createEmptyHabit();
        h1.setName("Wake up early");
        h1.setQuestion("Did you wake up early today?");
        h1.setDescription("test description 1");
        h1.setColor(5);
        habitList.update(h1);

        Habit h2 = fixtures.createShortHabit();
        h2.setName("Track time");
        h2.setQuestion("Did you track your time?");
        h2.setDescription("test description 2");
        h2.setColor(5);
        habitList.update(h2);

        Habit h3 = fixtures.createLongHabit();
        h3.setName("Meditate");
        h3.setQuestion("Did meditate today?");
        h3.setDescription("test description 3");
        h3.setColor(10);
        habitList.update(h3);

        Habit h4 = fixtures.createEmptyHabit();
        h4.setName(EMPTY_DESCRIPTION_HABIT_NAME);
        h4.setQuestion("Did you read books today?");
        h4.setDescription("");
        h4.setColor(2);
        habitList.update(h4);
    }

    protected void rotateDevice() throws Exception
    {
        device.setOrientationLeft();
        device.setOrientationNatural();
    }
}
