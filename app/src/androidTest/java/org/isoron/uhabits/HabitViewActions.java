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

package org.isoron.uhabits;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hamcrest.Matcher;

import java.security.InvalidParameterException;

public class HabitViewActions
{
    public static ViewAction toggleAllCheckmarks()
    {
        final GeneralClickAction clickAction =
                new GeneralClickAction(Tap.LONG, GeneralLocation.CENTER, Press.FINGER);

        return new ViewAction()
        {
            @Override
            public Matcher<View> getConstraints()
            {
                return ViewMatchers.isDisplayed();
            }

            @Override
            public String getDescription()
            {
                return "toggleAllCheckmarks";
            }

            @Override
            public void perform(UiController uiController, View view)
            {
                if (view.getId() != R.id.llButtons)
                    throw new InvalidParameterException("View must have id llButtons");

                LinearLayout llButtons = (LinearLayout) view;
                int count = llButtons.getChildCount();

                for (int i = 0; i < count; i++)
                {
                    TextView tvButton = (TextView) llButtons.getChildAt(i);
                    clickAction.perform(uiController, tvButton);
                }
            }
        };
    }
}
