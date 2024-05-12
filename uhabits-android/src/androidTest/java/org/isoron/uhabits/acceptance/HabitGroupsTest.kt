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
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.EDIT_HABIT
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.EDIT_HABIT_GROUP
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.LIST_HABITS
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.SELECT_HABIT_TYPE
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.SHOW_HABIT_GROUP
import org.isoron.uhabits.acceptance.steps.CommonSteps.clickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.launchApp
import org.isoron.uhabits.acceptance.steps.CommonSteps.longClickAndDragTo
import org.isoron.uhabits.acceptance.steps.CommonSteps.longClickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.pressBack
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplayAddButton
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysMeasurableGraphs
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysTextInSequence
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDoesNotDisplayText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyHiddenAddButton
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyShowsScreen
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.clickSave
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.pickColor
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.pickFrequency
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeDescription
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeName
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeQuestion
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeQuestionHgr
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.ADD
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.ARCHIVE
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.DELETE
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.EDIT
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.EDIT_GROUP
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.TOGGLE_ARCHIVED
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.TOGGLE_COMPLETED
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.UNARCHIVE
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.clickMenu
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.longPressCheckmarks
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.pressAddButton
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.pressCollapseButton
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HabitGroupsTest : BaseUserInterfaceTest() {

    @Test
    @Throws(Exception::class)
    fun shouldCreateHabitGroup() {
        shouldCreateHabitGroup("Habit group 1")
    }

    @Test
    @Throws(Exception::class)
    fun shouldCreateSubHabit() {
        shouldCreateSubHabit("History", "Study")
    }

    @Test
    @Throws(Exception::class)
    fun shouldCollapseAndUnCollapse() {
        shouldCollapseAndUnCollapse("Study", arrayOf("Physics", "Economics"))
    }

    @Test
    @Throws(Exception::class)
    fun habitGroupConsecutiveTasks() {
        shouldCreateHabitGroup("Habit group 1")
        shouldCreateSubHabit("Sub habit 1", "Habit group 1")
        shouldCreateSubHabit("Sub habit 2", "Habit group 1")
        shouldCollapseAndUnCollapse("Habit group 1", arrayOf("Sub habit 1", "Sub habit 2"))
        shouldMoveHabits("Habit group 1", "Read books")
        shouldMoveHabits("Economics", "Sub habit 2")
        verifyDisplaysTextInSequence(
            "Read books",
            "Habit group 1",
            "Sub habit 1",
            "Sub habit 2",
            "Economics",
            "Physics"
        )
    }

    @Throws(Exception::class)
    private fun shouldCreateHabitGroup(name: String) {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickMenu(ADD)
        verifyShowsScreen(SELECT_HABIT_TYPE)
        clickText("Habit Group")
        verifyShowsScreen(EDIT_HABIT_GROUP)
        typeName(name)
        typeQuestionHgr("Did you say hello to the world today?")
        typeDescription("This is a test description")
        pickColor(5)
        clickSave()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText(name)
    }

    @Throws(Exception::class)
    private fun shouldCreateSubHabit(name: String, habitGroup: String) {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        pressAddButton(habitGroup)
        verifyShowsScreen(SELECT_HABIT_TYPE)
        clickText("Yes or No")
        verifyShowsScreen(EDIT_HABIT)
        typeName(name)
        typeQuestion("Did you say hello to the world today?")
        typeDescription("this is a sub habit")
        pickFrequency()
        pickColor(5)
        clickSave()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText(name)
    }

    @Throws(Exception::class)
    private fun shouldCollapseAndUnCollapse(habitGroup: String, subHabits: Array<String>) {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        subHabits.forEach { verifyDisplaysText(it) }
        verifyDisplayAddButton(habitGroup)
        pressCollapseButton(habitGroup)
        subHabits.forEach { verifyDoesNotDisplayText(it) }
        verifyHiddenAddButton(habitGroup)
        pressCollapseButton(habitGroup)
        subHabits.forEach { verifyDisplaysText(it) }
        verifyDisplayAddButton(habitGroup)
    }

    private fun shouldMoveHabits(habit1: String, habit2: String) {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickAndDragTo(habit1, habit2)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowHabitStatistics() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickText("Study")
        verifyShowsScreen(SHOW_HABIT_GROUP)
        verifyDisplaysMeasurableGraphs()
    }

    @Test
    @Throws(Exception::class)
    fun shouldDeleteHabitGroup() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Study")
        clickMenu(DELETE)
        clickText("Yes")
        verifyDoesNotDisplayText("Study")
        verifyDoesNotDisplayText("Physics")
        verifyDoesNotDisplayText("Economics")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabitGroup() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Study")
        clickMenu(EDIT)
        verifyShowsScreen(EDIT_HABIT_GROUP)
        typeName("Learn")
        typeQuestionHgr("Did you learn something today?")
        typeDescription("this is a test description")
        clickSave()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Learn")
        verifyDoesNotDisplayText("Study")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabitGroup_fromStatisticsScreen() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickText("Study")
        verifyShowsScreen(SHOW_HABIT_GROUP)
        clickMenu(EDIT_GROUP)
        verifyShowsScreen(EDIT_HABIT_GROUP)
        typeName("Learn")
        typeQuestion("Did you take a walk today?")
        pickColor(10)
        clickSave()
        verifyShowsScreen(SHOW_HABIT_GROUP)
        verifyDisplaysText("Learn")
        pressBack()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Learn")
        verifyDoesNotDisplayText("Study")
    }

    @Test
    @Throws(Exception::class)
    fun shouldArchiveAndUnarchiveHabitGroups() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Study")
        clickMenu(ARCHIVE)
        verifyDoesNotDisplayText("Study")
        verifyDoesNotDisplayText("Physics")
        verifyDoesNotDisplayText("Economics")
        clickMenu(TOGGLE_ARCHIVED)
        verifyDisplaysTextInSequence("Study", "Physics", "Economics")
        longClickText("Study")
        clickMenu(UNARCHIVE)
        clickMenu(TOGGLE_ARCHIVED)
        verifyDisplaysTextInSequence("Study", "Physics", "Economics")
    }

    @Test
    @Throws(Exception::class)
    fun shouldToggleCheckmarksAndUpdateScore() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickText("Study")
        Thread.sleep(2001L)
        verifyDisplaysText("0%")
        pressBack()
        longPressCheckmarks("Physics", count = 3)
        longPressCheckmarks("Economics", count = 3)
        clickText("Study")
        verifyShowsScreen(SHOW_HABIT_GROUP)
        // TODO: find a better way than sleeping in tests
        Thread.sleep(2001L)
        verifyDisplaysText("15%")
    }

    @Test
    @Throws(Exception::class)
    fun shouldHideCompleted() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Study")
        verifyDisplaysText("Physics")
        verifyDisplaysText("Economics")
        longPressCheckmarks("Physics", count = 1)
        Thread.sleep(2001L)
        clickMenu(TOGGLE_COMPLETED)
        verifyDoesNotDisplayText("Physics")
        longPressCheckmarks("Economics", count = 1)
        Thread.sleep(2001L)
        verifyDoesNotDisplayText("Economics")
        verifyDoesNotDisplayText("Study")
        clickMenu(TOGGLE_COMPLETED)
        verifyDisplaysText("Study")
        verifyDisplaysText("Physics")
        verifyDisplaysText("Economics")
    }

    @Test
    @Throws(Exception::class)
    fun shouldAllowMultipleSelection() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Physics")
        longClickText("Economics")
        longClickText("Track time")
        verifyDisplaysText("2")
    }
}
