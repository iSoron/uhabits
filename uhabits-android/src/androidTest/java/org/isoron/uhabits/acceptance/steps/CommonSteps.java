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

package org.isoron.uhabits.acceptance.steps;

import android.support.annotation.*;
import android.support.test.espresso.*;
import android.support.test.espresso.contrib.*;
import android.support.test.uiautomator.*;
import android.support.v7.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.list.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class CommonSteps extends BaseUserInterfaceTest
{
    public static void clickOK()
    {
        clickText("OK");
    }

    public static void pressBack()
    {
        device.pressBack();
    }

    public static void clickText(String text)
    {
        scrollToText(text);
        onView(withText(text)).perform(click());
    }

    public static void clickText(@StringRes int id)
    {
        onView(withText(id)).perform(click());
    }

    public static void launchApp()
    {
        startActivity(ListHabitsActivity.class);
        assertTrue(
            device.wait(Until.hasObject(By.pkg("org.isoron.uhabits")), 5000));
        device.waitForIdle();
    }

    public static void longClickText(String text)
    {
        scrollToText(text);
        onView(withText(text)).perform(longClick());
    }

    public static void pressHome()
    {
        device.pressHome();
        device.waitForIdle();
    }

    public static void scrollToText(String text)
    {
        try
        {
            if (device
                .findObject(new UiSelector().className(RecyclerView.class))
                .exists())
            {
                onView(instanceOf(RecyclerView.class)).perform(
                    RecyclerViewActions.scrollTo(
                        hasDescendant(withText(text))));
            }
            else
            {
                onView(withText(text)).perform(scrollTo());
            }
        }
        catch (PerformException e)
        {
            //ignored
        }
    }

    public static void verifyDisplayGraphs()
    {
        verifyDisplaysView("HistoryCard");
        verifyDisplaysView("ScoreCard");
    }

    public static void verifyDisplaysText(String text)
    {
        scrollToText(text);
        onView(withText(text)).check(matches(isEnabled()));
    }

    private static void verifyDisplaysView(String className)
    {
        onView(withClassName(endsWith(className))).check(matches(isEnabled()));
    }

    public static void verifyDoesNotDisplayText(String text)
    {
        onView(withText(text)).check(doesNotExist());
    }

    public static void verifyOpensWebsite(String url) throws Exception
    {
        assertTrue(
            device.wait(Until.hasObject(By.pkg("com.android.chrome")), 5000));
        device.waitForIdle();
        assertTrue(device.findObject(new UiSelector().text(url)).exists());
    }

    public enum Screen
    {
        LIST_HABITS, SHOW_HABIT, EDIT_HABIT
    }

    public static void verifyShowsScreen(Screen screen)
    {
        switch(screen)
        {
            case LIST_HABITS:
                onView(withClassName(endsWith("ListHabitsRootView")))
                    .check(matches(isDisplayed()));
                break;

            case SHOW_HABIT:
                onView(withId(R.id.subtitleCard)).check(matches(isDisplayed()));
                break;

            case EDIT_HABIT:
                onView(withId(R.id.tvDescription)).check(matches(isDisplayed()));
                break;
        }
    }
}
