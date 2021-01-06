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

package org.isoron.uhabits.acceptance;

import androidx.test.filters.*;
import androidx.test.runner.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.isoron.uhabits.*;
import org.junit.*;
import org.junit.runner.*;

import static org.isoron.uhabits.acceptance.steps.CommonSteps.*;
import static org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.*;
import static org.isoron.uhabits.acceptance.steps.ListHabitsSteps.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LinksTest extends BaseUserInterfaceTest
{
    @Test
    public void shouldLinkToSourceCode() throws Exception
    {
        launchApp();
        clickMenu(ABOUT);
        clickText("View source code at GitHub");
        verifyOpensWebsite("github.com");
    }

    @Test
    public void shouldLinkToTranslationWebsite() throws Exception
    {
        launchApp();
        clickMenu(ABOUT);
        clickText("Help translate this app");
        verifyOpensWebsite("translate.loophabits.org");
    }

    @Test
    public void shouldLinkToHelp() throws Exception {
        launchApp();
        clickMenu(HELP);
        verifyOpensWebsite("github.com");
    }

    @Test
    public void shouldLinkToHelpFromSettings() throws Exception {
        launchApp();
        clickMenu(SETTINGS);
        clickText("Help & FAQ");
        verifyOpensWebsite("github.com");
    }
}
