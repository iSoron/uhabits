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

import androidx.test.uiautomator.*;

import org.isoron.uhabits.*;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.isoron.uhabits.BaseUserInterfaceTest.*;

public class EditHabitSteps
{
    public static void clickSave()
    {
        onView(withId(R.id.buttonSave)).perform(click());
    }

    public static void pickFrequency()
    {
        onView(withId(R.id.boolean_frequency_picker)).perform(click());
        onView(withText("SAVE")).perform(click());
    }

    public static void pickColor(int color)
    {
        onView(withId(R.id.colorButton)).perform(click());
        device.findObject(By.descStartsWith(String.format("Color %d", color))).click();
    }

    public static void typeName(String name)
    {
        typeTextWithId(R.id.nameInput, name);
    }

    public static void typeQuestion(String name)
    {
        typeTextWithId(R.id.questionInput, name);
    }

    public static void typeDescription(String description)
    {
        typeTextWithId(R.id.notesInput, description);
    }

    public static void setReminder()
    {
        onView(withId(R.id.reminderTimePicker)).perform(click());
        onView(withId(R.id.done_button)).perform(click());
    }

    public static void clickActiveDays()
    {
        onView(withId(R.id.activeDaysDatePicker)).perform(click());
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
