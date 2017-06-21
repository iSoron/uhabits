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

package org.isoron.uhabits.core.ui.screens.about;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.preferences.*;
import org.junit.*;
import org.mockito.*;

import static org.isoron.uhabits.core.ui.screens.about.AboutBehavior.Message.YOU_ARE_NOW_A_DEVELOPER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class AboutBehaviorTest extends BaseUnitTest
{
    private AboutBehavior behavior;

    @Mock
    private Preferences prefs;

    @Mock
    private AboutBehavior.Screen screen;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        behavior = new AboutBehavior(prefs, screen);
    }

    @Test
    public void onPressDeveloperCountdown() throws Exception
    {
        behavior.onPressDeveloperCountdown();
        behavior.onPressDeveloperCountdown();
        behavior.onPressDeveloperCountdown();
        behavior.onPressDeveloperCountdown();
        verifyZeroInteractions(screen);
        verifyZeroInteractions(prefs);

        behavior.onPressDeveloperCountdown();
        verify(screen).showMessage(YOU_ARE_NOW_A_DEVELOPER);
        verify(prefs).setDeveloper(true);

        behavior.onPressDeveloperCountdown();
        verifyZeroInteractions(screen);
        verifyZeroInteractions(prefs);
    }

    @Test
    public void onRateApp() throws Exception
    {
        behavior.onRateApp();
        verify(screen).showRateAppWebsite();
    }

    @Test
    public void onSendFeedback() throws Exception
    {
        behavior.onSendFeedback();
        verify(screen).showSendFeedbackScreen();
    }

    @Test
    public void onTranslateApp() throws Exception
    {
        behavior.onTranslateApp();
        verify(screen).showTranslationWebsite();
    }

    @Test
    public void onViewSourceCode() throws Exception
    {
        behavior.onViewSourceCode();
        verify(screen).showSourceCodeWebsite();
    }

}