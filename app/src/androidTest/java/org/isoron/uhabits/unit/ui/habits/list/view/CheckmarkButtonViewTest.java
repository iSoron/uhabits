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

package org.isoron.uhabits.unit.ui.habits.list.view;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.isoron.uhabits.models.Checkmark;
import org.isoron.uhabits.ui.habits.list.views.CheckmarkButtonView;
import org.isoron.uhabits.unit.views.ViewTest;
import org.isoron.uhabits.utils.ColorUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CheckmarkButtonViewTest extends ViewTest
{
    public static final String PATH = "ui/habits/list/CheckmarkButtonView/";

    private CountDownLatch latch;
    private CheckmarkButtonView view;

    @Before
    public void setUp()
    {
        super.setUp();
        setSimilarityCutoff(0.03f);

        latch = new CountDownLatch(1);
        view = new CheckmarkButtonView(targetContext);
        view.setValue(Checkmark.UNCHECKED);
        view.setColor(ColorUtils.CSV_PALETTE[7]);

        measureView(dpToPixels(40), dpToPixels(40), view);
    }

    protected void assertRendersCheckedExplicitly() throws IOException
    {
        assertRenders(view, PATH + "render_explicit_check.png");
    }

    protected void assertRendersUnchecked() throws IOException
    {
        assertRenders(view, PATH + "render_unchecked.png");
    }

    protected void assertRendersCheckedImplicitly() throws IOException
    {
        assertRenders(view, PATH + "render_implicit_check.png");
    }

    @Test
    public void testRender_unchecked() throws Exception
    {
        view.setValue(Checkmark.UNCHECKED);
        assertRendersUnchecked();
    }

    @Test
    public void testRender_explicitCheck() throws Exception
    {
        view.setValue(Checkmark.CHECKED_EXPLICITLY);
        assertRendersCheckedExplicitly();
    }

    @Test
    public void testRender_implicitCheck() throws Exception
    {
        view.setValue(Checkmark.CHECKED_IMPLICITLY);
        assertRendersCheckedImplicitly();
    }

//    @Test
//    public void testLongClick() throws Exception
//    {
//        setOnToggleListener();
//        view.performLongClick();
//        waitForLatch();
//        assertRendersCheckedExplicitly();
//    }
//
//    @Test
//    public void testClick_withShortToggle_fromUnchecked() throws Exception
//    {
//        Preferences.getInstance().setShortToggleEnabled(true);
//        view.setValue(Checkmark.UNCHECKED);
//        setOnToggleListenerAndPerformClick();
//        assertRendersCheckedExplicitly();
//    }
//
//    @Test
//    public void testClick_withShortToggle_fromChecked() throws Exception
//    {
//        Preferences.getInstance().setShortToggleEnabled(true);
//        view.setValue(Checkmark.CHECKED_EXPLICITLY);
//        setOnToggleListenerAndPerformClick();
//        assertRendersUnchecked();
//    }
//
//    @Test
//    public void testClick_withShortToggle_withoutListener() throws Exception
//    {
//        Preferences.getInstance().setShortToggleEnabled(true);
//        view.setValue(Checkmark.CHECKED_EXPLICITLY);
//        view.setController(null);
//        view.performClick();
//        assertRendersUnchecked();
//    }
//
//    protected void setOnToggleListenerAndPerformClick() throws InterruptedException
//    {
//        setOnToggleListener();
//        view.performClick();
//        waitForLatch();
//    }
//
//    @Test
//    public void testClick_withoutShortToggle() throws Exception
//    {
//        Preferences.getInstance().setShortToggleEnabled(false);
//        setOnInvalidToggleListener();
//        view.performClick();
//        waitForLatch();
//        assertRendersUnchecked();
//    }

//    protected void setOnInvalidToggleListener()
//    {
//        view.setController(new CheckmarkButtonView.Controller()
//        {
//            @Override
//            public void onToggleCheckmark(CheckmarkButtonView view, long timestamp)
//            {
//                fail();
//            }
//
//            @Override
//            public void onInvalidToggle(CheckmarkButtonView v)
//            {
//                assertThat(v, equalTo(view));
//                latch.countDown();
//            }
//        });
//    }

//    protected void setOnToggleListener()
//    {
//        view.setController(new CheckmarkButtonView.Controller()
//        {
//            @Override
//            public void onToggleCheckmark(CheckmarkButtonView v, long t)
//            {
//                assertThat(v, equalTo(view));
//                assertThat(t, equalTo(DateUtils.getStartOfToday()));
//                latch.countDown();
//            }
//
//            @Override
//            public void onInvalidToggle(CheckmarkButtonView view)
//            {
//                fail();
//            }
//        });
//    }
}