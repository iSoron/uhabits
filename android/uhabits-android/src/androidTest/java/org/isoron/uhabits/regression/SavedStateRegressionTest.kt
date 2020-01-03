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

import android.support.test.filters.*

import org.isoron.uhabits.*
import org.junit.*

import org.isoron.uhabits.acceptance.steps.CommonSteps.*
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.*
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.*
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.*
import org.isoron.uhabits.acceptance.steps.WidgetSteps.clickText

@LargeTest
class SavedStateRegressionTest : BaseUserInterfaceTest() {

    @Test
    @Throws(Exception::class)
    fun shouldNotCrashWhenRotatingWeekdayPickedDialog() {
        // https://github.com/iSoron/uhabits/issues/534
        launchApp()
        clickMenu(ADD)
        setReminder()
        clickReminderDays()
        unselectAllDays()
        rotateDevice()
        clickText("Monday")
    }
}
