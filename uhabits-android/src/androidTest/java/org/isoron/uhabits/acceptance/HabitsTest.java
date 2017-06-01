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

package org.isoron.uhabits.acceptance;

import android.support.test.filters.*;
import android.support.test.runner.*;

import org.isoron.uhabits.*;
import org.junit.*;
import org.junit.runner.*;

import static org.isoron.uhabits.acceptance.steps.CommonSteps.*;
import static org.isoron.uhabits.acceptance.steps.EditHabitSteps.*;
import static org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.*;
import static org.isoron.uhabits.acceptance.steps.ListHabitsSteps.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HabitsTest extends BaseUIAutomatorTest
{
    @Test
    public void shouldCreateHabit() throws Exception
    {
        launchApp();
        clickMenu(CREATE_HABIT);
        typeName("Hello world");
        typeQuestion("Did you say hello to the world today?");
        pickFrequency("Every week");
        pickColor(5);
        clickSave();
        verifyDisplaysText("Hello world");
    }

    @Test
    public void shouldShowHabitStatistics() throws Exception
    {
        launchApp();
        clickText("Track time");
        verifyDisplayGraphs();
    }

    @Test
    public void shouldDeleteHabit() throws Exception
    {
        launchApp();
        longClickText("Track time");
        clickMenu(DELETE);
        clickOK();
        verifyDoesNotDisplayText("Track time");
    }

    @Test
    public void shouldEditHabit() throws Exception
    {
        launchApp();
        longClickText("Track time");
        clickMenu(EDIT_HABIT);
        typeName("Take a walk");
        typeQuestion("Did you take a walk today?");
        clickSave();
        verifyDisplaysText("Take a walk");
        verifyDoesNotDisplayText("Track time");
    }

    @Test
    public void shouldEditHabit_fromStatisticsScreen() throws Exception
    {
        launchApp();
        clickText("Track time");
        clickMenu(EDIT_HABIT);
        typeName("Take a walk");
        typeQuestion("Did you take a walk today?");
        pickColor(10);
        clickSave();
        verifyDisplaysText("Take a walk");
        pressBack();
        verifyDisplaysText("Take a walk");
        verifyDoesNotDisplayText("Track time");
    }

    @Test
    public void shouldArchiveAndUnarchiveHabits() throws Exception
    {
        launchApp();
        longClickText("Track time");
        clickMenu(ARCHIVE);
        verifyDoesNotDisplayText("Track time");
        clickMenu(HIDE_ARCHIVED);
        verifyDisplaysText("Track time");

        longClickText("Track time");
        clickMenu(UNARCHIVE);
        clickMenu(HIDE_ARCHIVED);
        verifyDisplaysText("Track time");
    }
}
