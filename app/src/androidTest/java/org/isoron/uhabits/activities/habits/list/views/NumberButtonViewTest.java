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
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class NumberButtonViewTest extends BaseViewTest
{
    public static final String PATH = "habits/list/NumberButtonView/";

    private NumberButtonView view;

    private NumberButtonController controller;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        view = new NumberButtonView(targetContext);
        view.setUnit("steps");
        view.setThreshold(100.0);
        view.setColor(ColorUtils.getAndroidTestColor(5));

        measureView(view, dpToPixels(48), dpToPixels(48));

        controller = mock(NumberButtonController.class);
        view.setController(controller);
    }

    @Test
    public void testFormatValue()
    {
        assertThat(NumberButtonView.formatValue(0.1235), equalTo("0.12"));
        assertThat(NumberButtonView.formatValue(0.1000), equalTo("0.1"));
        assertThat(NumberButtonView.formatValue(5.0), equalTo("5"));
        assertThat(NumberButtonView.formatValue(5.25), equalTo("5.25"));
        assertThat(NumberButtonView.formatValue(12.3456), equalTo("12.3"));
        assertThat(NumberButtonView.formatValue(123.123), equalTo("123"));
        assertThat(NumberButtonView.formatValue(321.2), equalTo("321"));
        assertThat(NumberButtonView.formatValue(4321.2), equalTo("4.3k"));
        assertThat(NumberButtonView.formatValue(54321.2), equalTo("54.3k"));
        assertThat(NumberButtonView.formatValue(654321.2), equalTo("654k"));
        assertThat(NumberButtonView.formatValue(7654321.2), equalTo("7.7M"));
        assertThat(NumberButtonView.formatValue(87654321.2), equalTo("87.7M"));
        assertThat(NumberButtonView.formatValue(987654321.2), equalTo("988M"));
        assertThat(NumberButtonView.formatValue(1987654321.2), equalTo("2.0G"));
    }

    @Test
    public void testRender_aboveThreshold() throws Exception
    {
        view.setValue(500);
        assertRenders(view, PATH + "render_above.png");
    }

    @Test
    public void testRender_belowThreshold() throws Exception
    {
        view.setValue(99);
        assertRenders(view, PATH + "render_below.png");
    }

    @Test
    public void testRender_zero() throws Exception
    {
        view.setValue(0);
        assertRenders(view, PATH + "render_zero.png");
    }

    @Test
    public void test_click()
    {
        view.performClick();
        verify(controller).onClick();

        view.performLongClick();
        verify(controller).onLongClick();
    }
}