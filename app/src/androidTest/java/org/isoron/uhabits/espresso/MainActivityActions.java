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

import android.support.test.espresso.*;
import android.support.test.espresso.contrib.*;

import org.hamcrest.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.*;

import java.util.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.RootMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.Visibility.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

public class MainActivityActions
{
    public static String addHabit()
    {
        return addHabit(false);
    }

    public static String addHabit(boolean openDialogs)
    {
        String name = "New Habit " + new Random().nextInt(1000000);
        String description = "Did you perform your new habit today?";
        String num = "4";
        String den = "8";

        onView(withId(R.id.actionAdd)).perform(click());

        typeHabitData(name, description, num, den);

        if (openDialogs)
        {
            onView(withId(R.id.buttonPickColor)).perform(click());
            pressBack();
            onView(withId(R.id.tvReminderTime)).perform(click());
            onView(withText("Done")).perform(click());
            onView(withId(R.id.tvReminderDays)).perform(click());
            onView(withText("OK")).perform(click());
        }

        onView(withId(R.id.buttonSave)).perform(click());

        onData(Matchers.allOf(is(instanceOf(Habit.class)),
            HabitMatchers.withName(name))).onChildView(withId(R.id.label));

        return name;
    }

    public static void assertHabitExists(String name)
    {
        List<String> names = new LinkedList<>();
        names.add(name);
        assertHabitsExist(names);
    }

    public static void assertHabitsDontExist(List<String> names)
    {
        for (String name : names)
            onView(withId(R.id.listView)).check(matches(Matchers.not(
                HabitMatchers.containsHabit(HabitMatchers.withName(name)))));
    }

    public static void assertHabitsExist(List<String> names)
    {
        for (String name : names)
            onData(Matchers.allOf(is(instanceOf(Habit.class)),
                HabitMatchers.withName(name))).check(matches(isDisplayed()));
    }

    private static void clickHiddenMenuItem(int stringId)
    {
        try
        {
            // Try the ActionMode overflow menu first
            onView(allOf(withContentDescription("More options"), withParent(
                withParent(withClassName(containsString("Action")))))).perform(
                click());
        }
        catch (Exception e1)
        {
            // Try the toolbar overflow menu
            onView(allOf(withContentDescription("More options"), withParent(
                withParent(withClassName(containsString("Toolbar")))))).perform(
                click());
        }

        onView(withText(stringId)).perform(click());
    }

    public static void clickMenuItem(int stringId)
    {
        try
        {
            onView(withText(stringId)).perform(click());
        }
        catch (Exception e1)
        {
            try
            {
                onView(withContentDescription(stringId)).perform(click());
            }
            catch (Exception e2)
            {
                clickHiddenMenuItem(stringId);
            }
        }
    }

    public static void clickSettingsItem(String text)
    {
        onView(withClassName(containsString("RecyclerView"))).perform(
            RecyclerViewActions.actionOnItem(
                hasDescendant(withText(containsString(text))), click()));
    }

    public static void deleteHabit(String name)
    {
        deleteHabits(Collections.singletonList(name));
    }

    public static void deleteHabits(List<String> names)
    {
        selectHabits(names);
        clickMenuItem(R.string.delete);
        onView(withText("OK")).perform(click());
        assertHabitsDontExist(names);
    }

    public static void selectHabit(String name)
    {
        selectHabits(Collections.singletonList(name));
    }

    public static void selectHabits(List<String> names)
    {
        boolean first = true;
        for (String name : names)
        {
            onData(Matchers.allOf(is(instanceOf(Habit.class)),
                HabitMatchers.withName(name)))
                .onChildView(withId(R.id.label))
                .perform(first ? longClick() : click());

            first = false;
        }
    }

    public static void typeHabitData(String name,
                                     String description,
                                     String num,
                                     String den)
    {
        onView(withId(R.id.tvName)).perform(replaceText(name));
        onView(withId(R.id.tvDescription)).perform(replaceText(description));

        try
        {
            onView(allOf(withId(R.id.sFrequency),
                withEffectiveVisibility(VISIBLE))).perform(click());
            onData(allOf(instanceOf(String.class), startsWith("Custom")))
                .inRoot(isPlatformPopup())
                .perform(click());
        }
        catch (NoMatchingViewException e)
        {
            // ignored
        }

        onView(withId(R.id.tvFreqNum)).perform(replaceText(num));
        onView(withId(R.id.tvFreqDen)).perform(replaceText(den));
    }
}
