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

import android.support.test.uiautomator.*;

import org.isoron.uhabits.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.isoron.uhabits.BaseUserInterfaceTest.*;

public class EditHabitSteps
{
    public static void clickSave()
    {
        onView(withId(R.id.buttonSave)).perform(click());
    }

    public static void pickFrequency(String freq)
    {
        onView(withId(R.id.spinner)).perform(click());
        device.findObject(By.text(freq)).click();
    }

    public static void pickColor(int color)
    {
        onView(withId(R.id.buttonPickColor)).perform(click());
        device.findObject(By.descStartsWith(String.format("Color %d", color))).click();
    }

    public static void typeName(String name)
    {
        typeTextWithId(R.id.tvName, name);
    }

    public static void typeQuestion(String name)
    {
        typeTextWithId(R.id.tvDescription, name);
    }

    public static void setReminder()
    {
        onView(withId(R.id.tvReminderTime)).perform(click());
        onView(withId(R.id.done_button)).perform(click());
    }

    public static void clickReminderDays()
    {
        onView(withId(R.id.tvReminderDays)).perform(click());
    }

    public static void unselectAllDays()
    {
        onView(withText("Saturday")).perform(click());
        onView(withText("Sunday")).perform(click());
        onView(withText("Monday")).perform(click());
        onView(withText("Tuesday")).perform(click());
        onView(withText("Wednesday")).perform(click());
        onView(withText("Thursday")).perform(click());
        onView(withText("Friday")).perform(click());
    }

    private static void typeTextWithId(int id, String name)
    {
        onView(withId(id)).perform(clearText(), typeText(name), closeSoftKeyboard());
    }
}
