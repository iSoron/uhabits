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

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.*;

import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.BaseViewTest;
import org.isoron.uhabits.utils.ColorUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class CheckmarkPanelViewTest extends BaseViewTest
{
    public static final String PATH = "habits/list/CheckmarkPanelView/";

    private CountDownLatch latch;
    private CheckmarkPanelView view;
    private int checkmarks[];

    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        setSimilarityCutoff(0.03f);
        prefs.setShouldReverseCheckmarks(false);

        Habit habit = fixtures.createEmptyHabit();

        latch = new CountDownLatch(1);
        checkmarks = new int[]{
            Checkmark.CHECKED_EXPLICITLY, Checkmark.UNCHECKED,
                Checkmark.CHECKED_IMPLICITLY, Checkmark.CHECKED_EXPLICITLY};

        view = new CheckmarkPanelView(targetContext);
        view.setHabit(habit);
        view.setCheckmarkValues(checkmarks);
        view.setButtonCount(4);
        view.setColor(ColorUtils.getAndroidTestColor(7));

        measureView(view, dpToPixels(200), dpToPixels(200));
    }

//    protected void waitForLatch() throws InterruptedException
//    {
//        assertTrue("Latch timeout", latch.await(1, TimeUnit.SECONDS));
//    }

    @Test
    public void testRender() throws Exception
    {
        assertRenders(view, PATH + "render.png");
    }

//    @Test
//    public void testToggleCheckmark_withLeftToRight() throws Exception
//    {
//        setToggleListener();
//        view.getButton(1).performToggle();
//        waitForLatch();
//    }
//
//    @Test
//    public void testToggleCheckmark_withReverseCheckmarks() throws Exception
//    {
//        prefs.setShouldReverseCheckmarks(true);
//        view.setCheckmarkValues(checkmarks); // refresh after preference change
//
//        setToggleListener();
//        view.getButton(2).performToggle();
//        waitForLatch();
//    }
}