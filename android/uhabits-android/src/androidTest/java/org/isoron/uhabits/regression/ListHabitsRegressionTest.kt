/*
 * Copyright (C) 2020 Álinson Santos Xavier <isoron@gmail.com>
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

import androidx.test.filters.*
import org.isoron.uhabits.*
import org.isoron.uhabits.acceptance.steps.CommonSteps.*
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.*
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.*
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.*
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.*
import org.junit.*


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
        clickYes()

        clickMenu(ADD)
        verifyShowsScreen(SELECT_HABIT_TYPE)
        clickText("Yes or No")

        verifyShowsScreen(EDIT_HABIT)
        typeName("Hello world")
        clickSave()

        verifyDisplaysText("Hello world")
        longPressCheckmarks("Hello world", 3)
    }
}