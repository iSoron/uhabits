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

import android.support.test.espresso.*;
import android.view.*;

import org.hamcrest.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.list.views.*;

import java.util.*;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.*;
import static org.isoron.uhabits.BaseUserInterfaceTest.device;
import static org.isoron.uhabits.acceptance.steps.CommonSteps.clickText;

public abstract class ListHabitsSteps
{
    public static void clickMenu(MenuItem item)
    {
        switch (item)
        {
            case ABOUT:
                clickTextInsideOverflowMenu(R.string.about);
                break;

            case HELP:
                clickTextInsideOverflowMenu(R.string.help);
                break;

            case SETTINGS:
                clickTextInsideOverflowMenu(R.string.settings);
                break;

            case ADD:
                clickViewWithId(R.id.actionAdd);
                break;

            case EDIT:
                clickViewWithId(R.id.action_edit_habit);
                break;

            case DELETE:
                clickTextInsideOverflowMenu(R.string.delete);
                break;

            case ARCHIVE:
                clickTextInsideOverflowMenu(R.string.archive);
                break;

            case UNARCHIVE:
                clickTextInsideOverflowMenu(R.string.unarchive);
                break;

            case TOGGLE_ARCHIVED:
                clickViewWithId(R.id.action_filter);
                clickText(R.string.hide_archived);
                break;

            case TOGGLE_COMPLETED:
                clickViewWithId(R.id.action_filter);
                clickText(R.string.hide_completed);
                break;
        }
    }

    private static void clickTextInsideOverflowMenu(int id)
    {
        onView(allOf(withContentDescription("More options"), withParent(
            withParent(withClassName(endsWith("Toolbar")))))).perform(click());

        onView(withText(id)).perform(click());
    }

    private static void clickViewWithId(int id)
    {
        onView(withId(id)).perform(click());
    }

    private static ViewAction longClickDescendantWithClass(Class cls, int count)
    {
        return new ViewAction()
        {

            @Override
            public Matcher<View> getConstraints()
            {
                return isEnabled();
            }

            @Override
            public String getDescription()
            {
                return "perform on children";
            }

            @Override
            public void perform(UiController uiController, View view)
            {
                LinkedList<ViewGroup> stack = new LinkedList<>();
                if (view instanceof ViewGroup) stack.push((ViewGroup) view);
                int countRemaining = count;

                while (!stack.isEmpty())
                {
                    ViewGroup vg = stack.pop();
                    for (int i = 0; i < vg.getChildCount(); i++)
                    {
                        View v = vg.getChildAt(i);
                        if (v instanceof ViewGroup) stack.push((ViewGroup) v);
                        if (cls.isInstance(v) && countRemaining > 0)
                        {
                            v.performLongClick();
                            uiController.loopMainThreadUntilIdle();
                            countRemaining--;
                        }
                    }
                }
            }
        };
    }

    public static void longPressCheckmarks(String habit, int count)
    {
        CommonSteps.scrollToText(habit);
        onView(allOf(hasDescendant(withText(habit)),
            withClassName(endsWith("HabitCardView")))).perform(
            longClickDescendantWithClass(CheckmarkButtonView.class, count));
        device.waitForIdle();
    }

    public enum MenuItem
    {
        ABOUT, HELP, SETTINGS, EDIT, DELETE, ARCHIVE, TOGGLE_ARCHIVED,
        UNARCHIVE, TOGGLE_COMPLETED, ADD
    }
}
