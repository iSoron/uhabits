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
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RingViewTest extends ViewTest
{
    @Test
    public void renderTest1() throws IOException
    {
        RingView view = new RingView(targetContext);
        view.setLabel("Hello world");
        view.setPercentage(0.6f);
        view.setColor(ColorHelper.palette[0]);
        view.setMaxDiameter(dpToPixels(100));
        measureView(dpToPixels(100), dpToPixels(100), view);

        assertRenders(view, "Views/RingView/renderTest1.png");
    }
}
