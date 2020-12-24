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

package org.isoron.uhabits.activities.common.views;

import androidx.test.ext.junit.runners.*;
import androidx.test.filters.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.callbacks.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class HistoryChartTest extends BaseViewTest
{
    private static final String BASE_PATH = "common/HistoryChart/";

    private HistoryChart chart;

    private Habit habit;

    Timestamp today;

    private OnToggleCheckmarkListener onToggleEntryListener;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        fixtures.purgeHabits(habitList);
        habit = fixtures.createLongHabit();
        today = new Timestamp(DateUtils.getStartOfToday());

        chart = new HistoryChart(targetContext);
        chart.setSkipEnabled(true);
        chart.setEntries(habit.getComputedEntries().getAllValues());
        chart.setColor(PaletteUtilsKt.toFixedAndroidColor(habit.getColor()));
        measureView(chart, dpToPixels(400), dpToPixels(200));

        onToggleEntryListener = mock(OnToggleCheckmarkListener.class);
        chart.setOnToggleCheckmarkListener(onToggleEntryListener);
    }

    @Test
    public void tapDate_atInvalidLocations() throws Throwable
    {
        chart.setIsEditable(true);
        chart.tap(dpToPixels(118), dpToPixels(13)); // header
        chart.tap(dpToPixels(336), dpToPixels(60)); // tomorrow's square
        chart.tap(dpToPixels(370), dpToPixels(60)); // right axis
        verifyNoMoreInteractions(onToggleEntryListener);
    }

    @Test
    public void tapDate_withEditableView() throws Throwable
    {
        chart.setIsEditable(true);
        chart.tap(dpToPixels(340), dpToPixels(40));
        verify(onToggleEntryListener).onToggleEntry(today, Entry.SKIP);
        verifyNoMoreInteractions(onToggleEntryListener);
    }

    @Test
    public void tapDate_withEmptyHabit()
    {
        chart.setIsEditable(true);
        chart.setEntries(new int[]{});
        chart.tap(dpToPixels(340), dpToPixels(40));
        verify(onToggleEntryListener).onToggleEntry(today, Entry.YES_MANUAL);
        verifyNoMoreInteractions(onToggleEntryListener);
    }

    @Test
    public void tapDate_withReadOnlyView() throws Throwable
    {
        chart.setIsEditable(false);
        chart.tap(dpToPixels(340), dpToPixels(40));
        verifyNoMoreInteractions(onToggleEntryListener);
    }

    @Test
    public void testRender() throws Throwable
    {
        assertRenders(chart, BASE_PATH + "render.png");
    }

    @Test
    public void testRender_withDataOffset() throws Throwable
    {
        chart.onScroll(null, null, -dpToPixels(150), 0);
        chart.invalidate();

        assertRenders(chart, BASE_PATH + "renderDataOffset.png");
    }

    @Test
    public void testRender_withDifferentSize() throws Throwable
    {
        measureView(chart, dpToPixels(200), dpToPixels(200));
        assertRenders(chart, BASE_PATH + "renderDifferentSize.png");
    }

    @Test
    public void testRender_withTransparentBackground() throws Throwable
    {
        chart.setIsBackgroundTransparent(true);
        assertRenders(chart, BASE_PATH + "renderTransparent.png");
    }
}
