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

import androidx.test.filters.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.isoron.uhabits.*;
import org.junit.*;
import org.junit.runner.*;

import static org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.*;
import static org.isoron.uhabits.acceptance.steps.CommonSteps.*;
import static org.isoron.uhabits.acceptance.steps.EditHabitSteps.*;
import static org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.*;
import static org.isoron.uhabits.acceptance.steps.ListHabitsSteps.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HabitsTest extends BaseUserInterfaceTest
{
    @Test
    public void shouldCreateHabit() throws Exception {
        shouldCreateHabit("this is a test description");
    }

    @Test
    public void shouldCreateHabitBlankDescription() throws Exception {
        shouldCreateHabit("");
    }

    private void shouldCreateHabit(String description) throws Exception
    {
        launchApp();

        verifyShowsScreen(LIST_HABITS);
        clickMenu(ADD);

        verifyShowsScreen(SELECT_HABIT_TYPE);
        clickText("Yes or No");

        verifyShowsScreen(EDIT_HABIT);
        String testName = "Hello world";
        typeName(testName);
        typeQuestion("Did you say hello to the world today?");
        typeDescription(description);
        pickFrequency();
        pickColor(5);
        clickSave();

        verifyShowsScreen(LIST_HABITS);
        verifyDisplaysText(testName);
    }

    @Test
    public void shouldShowHabitStatistics() throws Exception
    {
        launchApp();
        verifyShowsScreen(LIST_HABITS);
        clickText("Track time");

        verifyShowsScreen(SHOW_HABIT);
        verifyDisplayGraphs();
    }

    @Test
    public void shouldDeleteHabit() throws Exception
    {
        launchApp();

        verifyShowsScreen(LIST_HABITS);
        longClickText("Track time");
        clickMenu(DELETE);
        clickYes();
        verifyDoesNotDisplayText("Track time");
    }

    @Test
    public void shouldEditHabit() throws Exception {
        shouldEditHabit("this is a test description");
    }

    @Test
    public void shouldEditHabitBlankDescription() throws Exception {
        shouldEditHabit("");
    }

    private void shouldEditHabit(String description) throws Exception
    {
        launchApp();

        verifyShowsScreen(LIST_HABITS);
        longClickText("Track time");
        clickMenu(EDIT);

        verifyShowsScreen(EDIT_HABIT);
        typeName("Take a walk");
        typeQuestion("Did you take a walk today?");
        typeDescription(description);
        clickSave();

        verifyShowsScreen(LIST_HABITS);
        verifyDisplaysTextInSequence("Wake up early", "Take a walk", "Meditate");
        verifyDoesNotDisplayText("Track time");
    }

    @Test
    public void shouldEditHabit_fromStatisticsScreen() throws Exception
    {
        launchApp();

        verifyShowsScreen(LIST_HABITS);
        clickText("Track time");

        verifyShowsScreen(SHOW_HABIT);
        clickMenu(EDIT);

        verifyShowsScreen(EDIT_HABIT);
        typeName("Take a walk");
        typeQuestion("Did you take a walk today?");
        pickColor(10);
        clickSave();

        verifyShowsScreen(SHOW_HABIT);
        verifyDisplaysText("Take a walk");
        pressBack();

        verifyShowsScreen(LIST_HABITS);
        verifyDisplaysText("Take a walk");
        verifyDoesNotDisplayText("Track time");
    }

    @Test
    public void shouldArchiveAndUnarchiveHabits() throws Exception
    {
        launchApp();

        verifyShowsScreen(LIST_HABITS);
        longClickText("Track time");
        clickMenu(ARCHIVE);
        verifyDoesNotDisplayText("Track time");
        clickMenu(TOGGLE_ARCHIVED);
        verifyDisplaysText("Track time");

        longClickText("Track time");
        clickMenu(UNARCHIVE);
        clickMenu(TOGGLE_ARCHIVED);
        verifyDisplaysText("Track time");
    }

    @Test
    public void shouldToggleCheckmarksAndUpdateScore() throws Exception
    {
        launchApp();
        verifyShowsScreen(LIST_HABITS);
        longPressCheckmarks("Wake up early", 2);
        clickText("Wake up early");

        verifyShowsScreen(SHOW_HABIT);
        verifyDisplaysText("10%");
    }

    @Test
    public void shouldHideCompleted() throws Exception
    {
        launchApp();
        verifyShowsScreen(LIST_HABITS);
        verifyDisplaysText("Track time");
        verifyDisplaysText("Wake up early");

        clickMenu(TOGGLE_COMPLETED);
        verifyDoesNotDisplayText("Track time");
        verifyDisplaysText("Wake up early");

        longPressCheckmarks("Wake up early", 1);
        verifyDoesNotDisplayText("Wake up early");

        clickMenu(TOGGLE_COMPLETED);
        verifyDisplaysText("Track time");
        verifyDisplaysText("Wake up early");
    }

    @Test
    public void shouldHideNotesCard() throws Exception
    {
        launchApp();
        clickText(EMPTY_DESCRIPTION_HABIT_NAME);
        verifyShowsScreen(SHOW_HABIT, false);
    }
}
