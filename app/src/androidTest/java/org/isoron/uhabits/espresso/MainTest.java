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

package org.isoron.uhabits.espresso;

import android.app.*;
import android.content.*;
import android.support.test.*;
import android.support.test.espresso.*;
import android.support.test.espresso.intent.rule.*;
import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import org.hamcrest.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.list.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import java.util.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.isoron.uhabits.espresso.HabitViewActions.*;
import static org.isoron.uhabits.espresso.MainActivityActions.*;
import static org.isoron.uhabits.espresso.ShowHabitActivityActions.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainTest
{
    private SystemHelper sys;

    @Rule
    public IntentsTestRule<ListHabitsActivity> activityRule =
        new IntentsTestRule<>(ListHabitsActivity.class);

    @Before
    public void setup()
    {
        Context context =
            InstrumentationRegistry.getInstrumentation().getContext();
        sys = new SystemHelper(context);
        sys.disableAllAnimations();
        sys.acquireWakeLock();
        sys.unlockScreen();

        Instrumentation.ActivityResult okResult =
            new Instrumentation.ActivityResult(Activity.RESULT_OK,
                new Intent());

        intending(hasAction(equalTo(Intent.ACTION_SEND))).respondWith(okResult);
        intending(hasAction(equalTo(Intent.ACTION_SENDTO))).respondWith(
            okResult);
        intending(hasAction(equalTo(Intent.ACTION_VIEW))).respondWith(okResult);

        skipTutorial();
    }

    public void skipTutorial()
    {
        try
        {
            for (int i = 0; i < 10; i++)
                onView(allOf(withClassName(endsWith("AppCompatImageButton")),
                    isDisplayed())).perform(click());
        }
        catch (NoMatchingViewException e)
        {
            // ignored
        }
    }

    @After
    public void tearDown()
    {
        sys.releaseWakeLock();
    }

    /**
     * User opens menu, clicks about, sees about screen.
     */
    @Test
    public void testAbout()
    {
        clickMenuItem(R.string.about);
        onView(isRoot()).perform(swipeUp());
    }

    /**
     * User creates a habit, toggles a bunch of checkmarks, clicks the habit to
     * open the statistics screen, scrolls down to some views, then scrolls the
     * views backwards and forwards in time.
     */
    @Test
    public void testAddHabitAndViewStats() throws InterruptedException
    {
        String name = addHabit(true);

        onData(Matchers.allOf(is(instanceOf(Habit.class)),
            HabitMatchers.withName(name)))
            .onChildView(withId(R.id.checkmarkPanel))
            .perform(toggleAllCheckmarks());

        Thread.sleep(1200);

        onData(Matchers.allOf(is(instanceOf(Habit.class)),
            HabitMatchers.withName(name)))
            .onChildView(withId(R.id.label))
            .perform(click());

        onView(withId(R.id.scoreView)).perform(scrollTo(), swipeRight());

        onView(withId(R.id.frequencyChart)).perform(scrollTo(), swipeRight());
    }

    /**
     * User opens the app, clicks the add button, types some bogus information,
     * tries to save, dialog displays an error.
     */
    @Test
    public void testAddInvalidHabit()
    {
        onView(withId(R.id.actionAdd)).perform(click());

        typeHabitData("", "", "15", "7");

        onView(withId(R.id.buttonSave)).perform(click());
        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
    }

    /**
     * User opens the app, creates some habits, selects them, archives them,
     * select 'show archived' on the menu, selects the previously archived
     * habits and then deletes them.
     */
    @Test
    public void testArchiveHabits()
    {
        List<String> names = new LinkedList<>();

        for (int i = 0; i < 3; i++)
            names.add(addHabit());

        selectHabits(names);

        clickMenuItem(R.string.archive);
        assertHabitsDontExist(names);

        clickMenuItem(R.string.show_archived);

        assertHabitsExist(names);
        selectHabits(names);
        clickMenuItem(R.string.unarchive);

        clickMenuItem(R.string.show_archived);

        assertHabitsExist(names);
        deleteHabits(names);
    }

    /**
     * User creates a habit, selects the habit, clicks edit button, changes some
     * information about the habit, click save button, sees changes on the main
     * window, selects habit again, changes color, then deletes the habit.
     */
    @Test
    public void testEditHabit()
    {
        String name = addHabit();

        onData(Matchers.allOf(is(instanceOf(Habit.class)),
            HabitMatchers.withName(name)))
            .onChildView(withId(R.id.label))
            .perform(longClick());

        clickMenuItem(R.string.edit);

        String modifiedName = "Modified " + new Random().nextInt(10000);
        typeHabitData(modifiedName, "", "1", "1");

        onView(withId(R.id.buttonSave)).perform(click());

        assertHabitExists(modifiedName);

        selectHabit(modifiedName);
        clickMenuItem(R.string.color_picker_default_title);
        pressBack();

        deleteHabit(modifiedName);
    }

    /**
     * User creates a habit, opens statistics page, clicks button to edit
     * history, adds some checkmarks, closes dialog, sees the modified history
     * calendar.
     */
    @Test
    public void testEditHistory()
    {
        String name = addHabit();

        onData(Matchers.allOf(is(instanceOf(Habit.class)),
            HabitMatchers.withName(name)))
            .onChildView(withId(R.id.label))
            .perform(click());

        openHistoryEditor();
        onView(withClassName(endsWith("HabitHistoryView"))).perform(
            clickAtRandomLocations(20));

        pressBack();
        onView(withId(R.id.historyChart)).perform(scrollTo(), swipeRight(),
            swipeLeft());
    }

    /**
     * User creates a habit, opens settings, clicks export as CSV, is asked what
     * activity should handle the file.
     */
    @Test
    public void testExportCSV()
    {
        addHabit();
        clickMenuItem(R.string.settings);
        clickSettingsItem("Export as CSV");
        intended(hasAction(Intent.ACTION_SEND));
    }

    /**
     * User creates a habit, exports full backup, deletes the habit, restores
     * backup, sees that the previously created habit has appeared back.
     */
    @Test
    public void testExportImportDB()
    {
        String name = addHabit();

        clickMenuItem(R.string.settings);

        String date =
            DateFormats.getBackupDateFormat().format(DateUtils.getLocalTime());
        date = date.substring(0, date.length() - 2);

        clickSettingsItem("Export full backup");
        intended(hasAction(Intent.ACTION_SEND));

        deleteHabit(name);

        clickMenuItem(R.string.settings);
        clickSettingsItem("Import data");

        onData(
            allOf(is(instanceOf(String.class)), startsWith("Backups"))).perform(
            click());

        onData(
            allOf(is(instanceOf(String.class)), containsString(date))).perform(
            click());

        selectHabit(name);
    }

    /**
     * User opens the settings and generates a bug report.
     */
    @Test
    public void testGenerateBugReport()
    {
        clickMenuItem(R.string.settings);
        clickSettingsItem("Generate bug report");
        intended(hasAction(Intent.ACTION_SEND));
    }

    /**
     * User opens menu, clicks Help, sees website.
     */
    @Test
    public void testHelp()
    {
        clickMenuItem(R.string.help);
        intended(hasAction(Intent.ACTION_VIEW));
    }

    /**
     * User opens menu, clicks settings, sees settings screen.
     */
    @Test
    public void testSettings()
    {
        clickMenuItem(R.string.settings);
    }
}
