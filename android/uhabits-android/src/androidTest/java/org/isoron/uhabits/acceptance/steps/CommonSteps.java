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

import android.view.View;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.hamcrest.Matcher;
import org.isoron.uhabits.BaseUserInterfaceTest;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.list.ListHabitsActivity;

import static android.os.Build.VERSION.SDK_INT;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.PositionAssertions.isBelow;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.instanceOf;

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

    public static void verifyDisplaysTextInSequence(String... text)
    {
        verifyDisplaysText(text[0]);
        for(int i = 1; i < text.length; i++) {
            verifyDisplaysText(text[i]);
            onView(withText(text[i])).check(isBelow(withText(text[i-1])));
        }
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
        String browser_pkg = "org.chromium.webview_shell";
        if(SDK_INT <= 23) {
            browser_pkg = "com.android.browser";
        }
        assertTrue(device.wait(Until.hasObject(By.pkg(browser_pkg)), 5000));
        device.waitForIdle();
        assertTrue(device.findObject(new UiSelector().textContains(url)).exists());
    }

    public enum Screen
    {
        LIST_HABITS, SHOW_HABIT, EDIT_HABIT
    }

    public static void verifyShowsScreen(Screen screen) {
        verifyShowsScreen(screen, true);
    }

    public static void verifyShowsScreen(Screen screen, boolean notesCardVisibleExpected)
    {
        switch(screen)
        {
            case LIST_HABITS:
                onView(withClassName(endsWith("ListHabitsRootView")))
                    .check(matches(isDisplayed()));
                break;

            case SHOW_HABIT:
                Matcher<View> noteCardViewMatcher = notesCardVisibleExpected ? isDisplayed() :
                        withEffectiveVisibility(Visibility.GONE);
                onView(withId(R.id.subtitleCard)).check(matches(isDisplayed()));
                onView(withId(R.id.notesCard)).check(matches(noteCardViewMatcher));
                break;

            case EDIT_HABIT:
                onView(withId(R.id.tvQuestion)).check(matches(isDisplayed()));
                onView(withId(R.id.tvDescription)).check(matches(isDisplayed()));
                break;
        }
    }
}
