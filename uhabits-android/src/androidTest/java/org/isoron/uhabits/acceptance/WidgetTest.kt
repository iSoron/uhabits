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

import androidx.test.filters.LargeTest
import org.isoron.uhabits.BaseUserInterfaceTest
import org.isoron.uhabits.acceptance.steps.CommonSteps.clickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.launchApp
import org.isoron.uhabits.acceptance.steps.CommonSteps.pressHome
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysText
import org.isoron.uhabits.acceptance.steps.WidgetSteps.clickCheckmarkWidget
import org.isoron.uhabits.acceptance.steps.WidgetSteps.dragCheckmarkWidgetToHomeScreen
import org.isoron.uhabits.acceptance.steps.WidgetSteps.verifyCheckmarkWidgetIsShown
import org.junit.Ignore
import org.junit.Test

@LargeTest
@Ignore("Flaky")
class WidgetTest : BaseUserInterfaceTest() {
    @Test
    @Throws(Exception::class)
    fun shouldCreateAndToggleCheckmarkWidget() {
        dragCheckmarkWidgetToHomeScreen()
        Thread.sleep(3000)
        clickText("Wake up early")
        clickText("Save")
        verifyCheckmarkWidgetIsShown()
        clickCheckmarkWidget()
        launchApp()
        clickText("Wake up early")
        verifyDisplaysText("5%")
        pressHome()
        clickCheckmarkWidget()
        launchApp()
        clickText("Wake up early")
        verifyDisplaysText("0%")
    }
}
