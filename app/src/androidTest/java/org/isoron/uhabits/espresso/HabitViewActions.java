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

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.isoron.uhabits.R;

import java.security.InvalidParameterException;
import java.util.Random;

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
                if (view.getId() != R.id.checkmarkPanel)
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

    public static ViewAction clickAt(final int x, final int y)
    {
        return new GeneralClickAction(Tap.SINGLE, new CoordinatesProvider()
        {
            @Override
            public float[] calculateCoordinates(View view)
            {
                int[] locations = new int[2];
                view.getLocationOnScreen(locations);

                final float locationX = locations[0] + x;
                final float locationY = locations[1] + y;

                return new float[]{locationX, locationY};
            }
        }, Press.FINGER);
    }

    public static ViewAction clickAtRandomLocations(final int count)
    {
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
                return "clickAtRandomLocations";
            }

            @Override
            public void perform(UiController uiController, View view)
            {
                int width = view.getWidth();
                int height = view.getHeight();
                Random random = new Random();

                for(int i = 0; i < count; i++)
                {
                    int x = random.nextInt(width);
                    int y = random.nextInt(height);

                    ViewAction action = clickAt(x, y);
                    action.perform(uiController, view);
                }
            }
        };
    }
}
