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

package org.isoron.uhabits.unit.views;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.views.RingView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RingViewTest extends ViewTest
{
    private RingView view;

    @Before
    public void setup()
    {
        super.setup();

        view = new RingView(targetContext);
        view.setLabel("Hello world");
        view.setPercentage(0.6f);
        view.setColor(ColorHelper.palette[0]);
        view.setMaxDiameter(dpToPixels(100));
    }

    @Test
    public void testRender_base() throws IOException
    {
        measureView(dpToPixels(100), dpToPixels(100), view);
        assertRenders(view, "RingView/render.png");
    }

    @Test
    public void testRender_withLongLabel() throws IOException
    {
        view.setLabel("The quick brown fox jumps over the lazy fox");

        measureView(dpToPixels(100), dpToPixels(100), view);
        assertRenders(view, "RingView/renderLongLabel.png");
    }

    @Test
    public void testRender_withDifferentParams() throws IOException
    {
        view.setLabel("Habit Strength");
        view.setPercentage(0.25f);
        view.setMaxDiameter(dpToPixels(50));
        view.setColor(ColorHelper.palette[5]);

        measureView(dpToPixels(200), dpToPixels(200), view);
        assertRenders(view, "RingView/renderDifferentParams.png");
    }
}
