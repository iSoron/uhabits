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

package org.isoron.uhabits.regression

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.allOf
import org.isoron.uhabits.BaseUserInterfaceTest
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.EDIT_HABIT
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.LIST_HABITS
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.SELECT_HABIT_TYPE
import org.isoron.uhabits.acceptance.steps.CommonSteps.clickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.launchApp
import org.isoron.uhabits.acceptance.steps.CommonSteps.longClickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.offsetHeaders
import org.isoron.uhabits.acceptance.steps.CommonSteps.scrollToText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyShowsScreen
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.clickSave
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeName
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.ADD
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.DELETE
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.clickMenu
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.longPressCheckmarks
import org.junit.Test

@LargeTest
class ListHabitsRegressionTest : BaseUserInterfaceTest() {
    /**
     * https://github.com/iSoron/uhabits/issues/539
     */
    @Test
    @Throws(Exception::class)
    fun should_not_crash_after_deleting_then_adding_a_habit() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Track time")
        clickMenu(DELETE)
        clickText("Yes")

        clickMenu(ADD)
        verifyShowsScreen(SELECT_HABIT_TYPE)
        clickText("Yes or No")

        verifyShowsScreen(EDIT_HABIT)
        typeName("Hello world")
        clickSave()

        verifyDisplaysText("Hello world")
        longPressCheckmarks("Hello world", 3)
    }

    /**
     * https://github.com/iSoron/uhabits/issues/713
     */
    @Test
    @Throws(Exception::class)
    fun should_update_out_of_screen_checkmarks_when_scrolling_horizontally() {
        launchApp()

        verifyShowsScreen(LIST_HABITS)
        longPressCheckmarks("Wake up early", 1)

        verifyShowsScreen(LIST_HABITS)
        assertCorrectNumberCheckmarks(listOf(2, -1, -1, -1))

        fun createHabit(habitName: String) {
            clickMenu(ADD)
            verifyShowsScreen(SELECT_HABIT_TYPE)
            clickText("Yes or No")

            verifyShowsScreen(EDIT_HABIT)
            typeName(habitName)
            clickSave()
        }

        createHabit("H")
        createHabit("H")
        createHabit("H")
        createHabit("H")
        createHabit("H")
        createHabit("H")
        createHabit("H")
        createHabit("Last Habit")

        scrollToText("Last Habit")
        offsetHeaders()
        assertCorrectNumberCheckmarks(listOf(-1, -1, -1, -1))
    }

    private fun assertCorrectNumberCheckmarks(vals: List<Int>) {
        val habit = "Wake up early"

        scrollToText(habit)

        Espresso.onView(
            allOf(
                ViewMatchers.hasDescendant(ViewMatchers.withText(habit)),
                ViewMatchers.withClassName(CoreMatchers.endsWith("HabitCardView"))
            )
        ).check(HasButtonsViewAssertion(vals))
    }
}
