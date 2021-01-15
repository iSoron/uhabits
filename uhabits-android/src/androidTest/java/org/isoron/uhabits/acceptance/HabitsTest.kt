/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.acceptance

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.isoron.uhabits.BaseUserInterfaceTest
import org.isoron.uhabits.acceptance.steps.CommonSteps
import org.isoron.uhabits.acceptance.steps.CommonSteps.clickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.launchApp
import org.isoron.uhabits.acceptance.steps.CommonSteps.longClickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.pressBack
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplayGraphs
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysTextInSequence
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDoesNotDisplayText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyShowsScreen
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.clickSave
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.pickColor
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.pickFrequency
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeDescription
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeName
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeQuestion
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.clickMenu
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.longPressCheckmarks
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HabitsTest : BaseUserInterfaceTest() {
    @Test
    @Throws(Exception::class)
    fun shouldCreateHabit() {
        shouldCreateHabit("this is a test description")
    }

    @Test
    @Throws(Exception::class)
    fun shouldCreateHabitBlankDescription() {
        shouldCreateHabit("")
    }

    @Throws(Exception::class)
    private fun shouldCreateHabit(description: String) {
        launchApp()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        clickMenu(ListHabitsSteps.MenuItem.ADD)
        verifyShowsScreen(CommonSteps.Screen.SELECT_HABIT_TYPE)
        clickText("Yes or No")
        verifyShowsScreen(CommonSteps.Screen.EDIT_HABIT)
        val testName = "Hello world"
        typeName(testName)
        typeQuestion("Did you say hello to the world today?")
        typeDescription(description)
        pickFrequency()
        pickColor(5)
        clickSave()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        verifyDisplaysText(testName)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowHabitStatistics() {
        launchApp()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        clickText("Track time")
        verifyShowsScreen(CommonSteps.Screen.SHOW_HABIT)
        verifyDisplayGraphs()
    }

    @Test
    @Throws(Exception::class)
    fun shouldDeleteHabit() {
        launchApp()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        longClickText("Track time")
        clickMenu(ListHabitsSteps.MenuItem.DELETE)
        clickText("Yes")
        verifyDoesNotDisplayText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabit() {
        shouldEditHabit("this is a test description")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabitBlankDescription() {
        shouldEditHabit("")
    }

    @Throws(Exception::class)
    private fun shouldEditHabit(description: String) {
        launchApp()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        longClickText("Track time")
        clickMenu(ListHabitsSteps.MenuItem.EDIT)
        verifyShowsScreen(CommonSteps.Screen.EDIT_HABIT)
        typeName("Take a walk")
        typeQuestion("Did you take a walk today?")
        typeDescription(description)
        clickSave()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        verifyDisplaysTextInSequence("Wake up early", "Take a walk", "Meditate")
        verifyDoesNotDisplayText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabit_fromStatisticsScreen() {
        launchApp()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        clickText("Track time")
        verifyShowsScreen(CommonSteps.Screen.SHOW_HABIT)
        clickMenu(ListHabitsSteps.MenuItem.EDIT)
        verifyShowsScreen(CommonSteps.Screen.EDIT_HABIT)
        typeName("Take a walk")
        typeQuestion("Did you take a walk today?")
        pickColor(10)
        clickSave()
        verifyShowsScreen(CommonSteps.Screen.SHOW_HABIT)
        verifyDisplaysText("Take a walk")
        pressBack()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        verifyDisplaysText("Take a walk")
        verifyDoesNotDisplayText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldArchiveAndUnarchiveHabits() {
        launchApp()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        longClickText("Track time")
        clickMenu(ListHabitsSteps.MenuItem.ARCHIVE)
        verifyDoesNotDisplayText("Track time")
        clickMenu(ListHabitsSteps.MenuItem.TOGGLE_ARCHIVED)
        verifyDisplaysText("Track time")
        longClickText("Track time")
        clickMenu(ListHabitsSteps.MenuItem.UNARCHIVE)
        clickMenu(ListHabitsSteps.MenuItem.TOGGLE_ARCHIVED)
        verifyDisplaysText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldToggleCheckmarksAndUpdateScore() {
        launchApp()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        longPressCheckmarks("Wake up early", 2)
        clickText("Wake up early")
        verifyShowsScreen(CommonSteps.Screen.SHOW_HABIT)
        verifyDisplaysText("10%")
    }

    @Test
    @Throws(Exception::class)
    fun shouldHideCompleted() {
        launchApp()
        verifyShowsScreen(CommonSteps.Screen.LIST_HABITS)
        verifyDisplaysText("Track time")
        verifyDisplaysText("Wake up early")
        clickMenu(ListHabitsSteps.MenuItem.TOGGLE_COMPLETED)
        verifyDoesNotDisplayText("Track time")
        verifyDisplaysText("Wake up early")
        longPressCheckmarks("Wake up early", 1)
        verifyDoesNotDisplayText("Wake up early")
        clickMenu(ListHabitsSteps.MenuItem.TOGGLE_COMPLETED)
        verifyDisplaysText("Track time")
        verifyDisplaysText("Wake up early")
    }
}
