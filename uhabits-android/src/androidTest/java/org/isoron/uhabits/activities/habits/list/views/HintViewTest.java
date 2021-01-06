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

package org.isoron.uhabits.activities.habits.list.views;

import androidx.test.filters.*;
import androidx.test.runner.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;
import org.junit.*;
import org.junit.runner.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class HintViewTest extends BaseViewTest
{
    public static final String PATH = "habits/list/HintView/";

    private HintView view;

    private HintList list;

    @Before
    @Override
    public void setUp()
    {
        super.setUp();

        list = mock(HintList.class);
        view = new HintView(targetContext, list);
        measureView(view, 400, 200);

        String text =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

        when(list.shouldShow()).thenReturn(true);
        when(list.pop()).thenReturn(text);

        view.showNext();
        skipAnimation(view);
    }

    @Test
    public void testRender() throws Exception
    {
        assertRenders(view, PATH + "render.png");
    }

    @Test
    public void testClick() throws Exception
    {
        assertThat(view.getAlpha(), equalTo(1f));
        view.performClick();
        skipAnimation(view);
        assertThat(view.getAlpha(), equalTo(0f));
    }
}
