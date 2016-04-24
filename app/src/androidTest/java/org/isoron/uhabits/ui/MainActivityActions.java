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

package org.isoron.uhabits.ui;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.contrib.RecyclerViewActions;

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.isoron.uhabits.ui.HabitMatchers.containsHabit;
import static org.isoron.uhabits.ui.HabitMatchers.withName;

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

        onView(withId(R.id.action_add))
                .perform(click());

        typeHabitData(name, description, num, den);

        if(openDialogs)
        {
            onView(withId(R.id.buttonPickColor))
                    .perform(click());
            pressBack();
            onView(withId(R.id.inputReminderTime))
                    .perform(click());
            onView(withText("Done"))
                    .perform(click());
            onView(withId(R.id.inputReminderDays))
                    .perform(click());
            onView(withText("OK"))
                    .perform(click());
        }

        onView(withId(R.id.buttonSave))
                .perform(click());

        onData(allOf(is(instanceOf(Habit.class)), withName(name)))
                .onChildView(withId(R.id.label));

        return name;
    }

    public static void typeHabitData(String name, String description, String num, String den)
    {
        onView(withId(R.id.input_name))
                .perform(replaceText(name));
        onView(withId(R.id.input_description))
                .perform(replaceText(description));

        try
        {
            onView(allOf(withId(R.id.sFrequency), withEffectiveVisibility(VISIBLE)))
                    .perform(click());
            onData(allOf(instanceOf(String.class), startsWith("Custom")))
                    .inRoot(isPlatformPopup())
                    .perform(click());
        }
        catch(NoMatchingViewException e)
        {
            // ignored
        }

        onView(withId(R.id.input_freq_num))
                .perform(replaceText(num));
        onView(withId(R.id.input_freq_den))
                .perform(replaceText(den));
    }

    public static void selectHabit(String name)
    {
        selectHabits(Collections.singletonList(name));
    }

    public static void selectHabits(List<String> names)
    {
        boolean first = true;
        for(String name : names)
        {
            onData(allOf(is(instanceOf(Habit.class)), withName(name)))
                    .onChildView(withId(R.id.label))
                    .perform(first ? longClick() : click());

            first = false;
        }
    }

    public static void assertHabitsDontExist(List<String> names)
    {
        for(String name : names)
            onView(withId(R.id.listView))
                    .check(matches(not(containsHabit(withName(name)))));
    }

    public static void assertHabitExists(String name)
    {
        List<String> names = new LinkedList<>();
        names.add(name);
        assertHabitsExist(names);
    }

    public static void assertHabitsExist(List<String> names)
    {
        for(String name : names)
            onData(allOf(is(instanceOf(Habit.class)), withName(name)))
                    .check(matches(isDisplayed()));
    }

    public static void deleteHabit(String name)
    {
        deleteHabits(Collections.singletonList(name));
    }

    public static void deleteHabits(List<String> names)
    {
        selectHabits(names);
        clickMenuItem(R.string.delete);
        onView(withText("OK"))
                .perform(click());
        assertHabitsDontExist(names);
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
            catch(Exception e2)
            {
                clickHiddenMenuItem(stringId);
            }
        }
    }

    private static void clickHiddenMenuItem(int stringId)
    {
        try
        {
            // Try the ActionMode overflow menu first
            onView(allOf(withContentDescription("More options"), withParent(withParent(
                    withClassName(containsString("Action")))))).perform(click());
        }
        catch (Exception e1)
        {
            // Try the toolbar overflow menu
            onView(allOf(withContentDescription("More options"), withParent(withParent(
                    withClassName(containsString("Toolbar")))))).perform(click());
        }

        onView(withText(stringId)).perform(click());
    }

    public static void clickSettingsItem(String text)
    {
        onView(withClassName(containsString("RecyclerView")))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(containsString(text))),
                        click()));
    }
}
