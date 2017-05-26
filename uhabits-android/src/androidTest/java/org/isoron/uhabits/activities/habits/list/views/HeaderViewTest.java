/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.activities.habits.list.views;

import android.support.test.filters.*;
import android.support.test.runner.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class HeaderViewTest extends BaseViewTest
{
    public static final String PATH = "habits/list/HeaderView/";

    private HeaderView view;

    private AndroidPreferences prefs;

    private MidnightTimer midnightTimer;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        prefs = mock(AndroidPreferences.class);
        midnightTimer = mock(MidnightTimer.class);
        view = new HeaderView(targetContext, prefs, midnightTimer);
        view.setButtonCount(5);
        measureView(view, dpToPixels(600), dpToPixels(48));
    }

    @Test
    public void testRender() throws Exception
    {
        when(prefs.shouldReverseCheckmarks()).thenReturn(false);

        assertRenders(view, PATH + "render.png");

        verify(prefs).shouldReverseCheckmarks();
        verifyNoMoreInteractions(prefs);
    }

    @Test
    public void testRender_reverse() throws Exception
    {
        when(prefs.shouldReverseCheckmarks()).thenReturn(true);

        assertRenders(view, PATH + "render_reverse.png");

        verify(prefs).shouldReverseCheckmarks();
        verifyNoMoreInteractions(prefs);
    }
}