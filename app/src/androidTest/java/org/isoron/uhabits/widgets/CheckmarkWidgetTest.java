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

package org.isoron.uhabits.widgets;

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.widgets.*;
import org.junit.*;
import org.junit.runner.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.isoron.uhabits.models.Checkmark.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class CheckmarkWidgetTest extends BaseViewTest
{
    private static final String PATH = "widgets/CheckmarkWidgetView/";

    private Habit habit;

    private CheckmarkList checkmarks;

    private FrameLayout view;

    @Override
    public void setUp()
    {
        super.setUp();
        habit = fixtures.createShortHabit();
        checkmarks = habit.getCheckmarks();
        CheckmarkWidget widget = new CheckmarkWidget(targetContext, 0, habit);
        view = convertToView(widget, 200, 250);

        assertThat(checkmarks.getTodayValue(), equalTo(CHECKED_EXPLICITLY));
    }

    @Test
    public void testClick() throws Exception
    {
        Button button = (Button) view.findViewById(R.id.button);
        assertThat(button, is(not(nullValue())));

        // A better test would be to capture the intent, but it doesn't seem
        // possible to capture intents sent to BroadcastReceivers.
        button.performClick();
        sleep(1000);

        assertThat(checkmarks.getTodayValue(), equalTo(UNCHECKED));
    }

    @Test
    public void testIsInstalled()
    {
        assertWidgetProviderIsInstalled(CheckmarkWidgetProvider.class);
    }

    @Test
    public void testRender() throws Exception
    {
        assertRenders(view, PATH + "checked.png");
    }
}
