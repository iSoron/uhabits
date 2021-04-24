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
import org.isoron.uhabits.acceptance.steps.CommonSteps.clickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.launchApp
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyOpensWebsite
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.ABOUT
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.HELP
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.SETTINGS
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.clickMenu
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@Ignore("Fails on GitHub Actions")
class LinksTest : BaseUserInterfaceTest() {
    @Test
    @Throws(Exception::class)
    fun shouldLinkToSourceCode() {
        launchApp()
        clickMenu(ABOUT)
        clickText("View source code at GitHub")
        verifyOpensWebsite("github.com")
    }

    @Test
    @Throws(Exception::class)
    fun shouldLinkToTranslationWebsite() {
        launchApp()
        clickMenu(ABOUT)
        clickText("Help translate this app")
        verifyOpensWebsite("translate.loophabits.org")
    }

    @Test
    @Throws(Exception::class)
    fun shouldLinkToHelp() {
        launchApp()
        clickMenu(HELP)
        verifyOpensWebsite("github.com")
    }

    @Test
    @Throws(Exception::class)
    fun shouldLinkToHelpFromSettings() {
        launchApp()
        clickMenu(SETTINGS)
        clickText("Help & FAQ")
        verifyOpensWebsite("github.com")
    }
}
