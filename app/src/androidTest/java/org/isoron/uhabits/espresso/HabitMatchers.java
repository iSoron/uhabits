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

import android.preference.Preference;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.isoron.uhabits.models.Habit;

public class HabitMatchers
{
    public static Matcher<Habit> withName(final String name)
    {
        return new TypeSafeMatcher<Habit>()
        {
            @Override
            public boolean matchesSafely(Habit habit)
            {
                return habit.getName().equals(name);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("name should be ").appendText(name);
            }

            @Override
            public void describeMismatchSafely(Habit habit, Description description)
            {
                description.appendText("was ").appendText(habit.getName());
            }
        };
    }

    public static Matcher<View> containsHabit(final Matcher<Habit> matcher)
    {
        return new TypeSafeMatcher<View>()
        {
            @Override
            protected boolean matchesSafely(View view)
            {
                Adapter adapter = ((AdapterView) view).getAdapter();
                for (int i = 0; i < adapter.getCount(); i++)
                    if (matcher.matches(adapter.getItem(i))) return true;

                return false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("with class name: ");
                matcher.describeTo(description);
            }
        };
    }

    public static Matcher<?> isPreferenceWithText(final String text)
    {
        return (Matcher<?>) new BaseMatcher()
        {
            @Override
            public boolean matches(Object o)
            {
                if(!(o instanceof Preference)) return false;
                return o.toString().contains(text);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText(String.format("is preference with text '%s'", text));
            }
        };
    }
}
