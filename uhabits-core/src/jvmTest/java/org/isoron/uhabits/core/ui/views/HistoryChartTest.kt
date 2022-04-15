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

package org.isoron.uhabits.core.ui.views

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import kotlinx.coroutines.runBlocking
import org.isoron.platform.gui.ScreenLocation
import org.isoron.platform.gui.assertRenders
import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.DayOfWeek.SUNDAY
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.views.HistoryChart.Square.DIMMED
import org.isoron.uhabits.core.ui.views.HistoryChart.Square.HATCHED
import org.isoron.uhabits.core.ui.views.HistoryChart.Square.OFF
import org.isoron.uhabits.core.ui.views.HistoryChart.Square.ON
import org.junit.Test
import java.util.Locale

class HistoryChartTest {
    val base = "views/HistoryChart"

    private val dateClickedListener: OnDateClickedListener = mock()

    val view = HistoryChart(
        today = LocalDate(2015, 1, 25),
        paletteColor = PaletteColor(7),
        theme = LightTheme(),
        dateFormatter = JavaLocalDateFormatter(Locale.US),
        firstWeekday = SUNDAY,
        onDateClickedListener = dateClickedListener,
        defaultSquare = OFF,
        series = listOf(
            2, // today
            2, 1, 2, 1, 2, 1, 2,
            2, 3, 3, 3, 3, 1, 2,
            2, 1, 2, 1, 2, 2, 1,
            1, 1, 1, 1, 2, 2, 2,
            1, 3, 3, 3, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1,
            2, 2, 2, 3, 3, 3, 1,
            1, 2, 1, 2, 1, 1, 2,
            1, 2, 1, 1, 1, 1, 2,
            2, 2, 2, 2, 2, 1, 1,
            1, 1, 2, 2, 1, 2, 1,
            1, 1, 1, 1, 2, 2, 2,
        ).map {
            when (it) {
                3 -> HATCHED
                2 -> ON
                1 -> DIMMED
                else -> OFF
            }
        },
        notesIndicators = MutableList(85) { index: Int ->
            index % 3 == 0
        }
    )

    @Test
    fun testDraw() = runBlocking {
        assertRenders(400, 200, "$base/base.png", view)
    }

    @Test
    fun testClick() = runBlocking {
        assertRenders(400, 200, "$base/base.png", view)

        // Click top left date
        view.onClick(20.0, 46.0)
        verify(dateClickedListener).onDateShortPress(
            ScreenLocation(20.0, 46.0),
            LocalDate(2014, 10, 26)
        )
        reset(dateClickedListener)
        view.onClick(2.0, 28.0)
        verify(dateClickedListener).onDateShortPress(
            ScreenLocation(2.0, 28.0),
            LocalDate(2014, 10, 26)
        )
        reset(dateClickedListener)

        // Click date in the middle
        view.onClick(163.0, 113.0)
        verify(dateClickedListener).onDateShortPress(
            ScreenLocation(163.0, 113.0),
            LocalDate(2014, 12, 10)
        )
        reset(dateClickedListener)

        // Click today
        view.onClick(336.0, 37.0)
        verify(dateClickedListener).onDateShortPress(
            ScreenLocation(336.0, 37.0),
            LocalDate(2015, 1, 25)
        )
        reset(dateClickedListener)

        // Click header
        view.onClick(160.0, 15.0)
        verifyNoMoreInteractions(dateClickedListener)

        // Click right axis
        view.onClick(360.0, 60.0)
        verifyNoMoreInteractions(dateClickedListener)
    }

    @Test
    fun testLongClick() = runBlocking {
        assertRenders(400, 200, "$base/base.png", view)

        // Click top left date
        view.onLongClick(20.0, 46.0)
        verify(dateClickedListener).onDateLongPress(
            ScreenLocation(20.0, 46.0),
            LocalDate(2014, 10, 26)
        )
        reset(dateClickedListener)
        view.onLongClick(2.0, 28.0)
        verify(dateClickedListener).onDateLongPress(
            ScreenLocation(2.0, 28.0),
            LocalDate(2014, 10, 26)
        )
        reset(dateClickedListener)

        // Click date in the middle
        view.onLongClick(163.0, 113.0)
        verify(dateClickedListener).onDateLongPress(
            ScreenLocation(163.0, 113.0),
            LocalDate(2014, 12, 10)
        )
        reset(dateClickedListener)

        // Click today
        view.onLongClick(336.0, 37.0)
        verify(dateClickedListener).onDateLongPress(
            ScreenLocation(336.0, 37.0),
            LocalDate(2015, 1, 25)
        )
        reset(dateClickedListener)

        // Click header
        view.onLongClick(160.0, 15.0)
        verifyNoMoreInteractions(dateClickedListener)

        // Click right axis
        view.onLongClick(360.0, 60.0)
        verifyNoMoreInteractions(dateClickedListener)
    }

    @Test
    fun testDrawWeekDay() = runBlocking {
        view.firstWeekday = DayOfWeek.MONDAY
        assertRenders(400, 200, "$base/weekday.png", view)
    }

    @Test
    fun testDrawDifferentSize() = runBlocking {
        assertRenders(200, 200, "$base/small.png", view)
    }

    @Test
    fun testDrawDarkTheme() = runBlocking {
        view.theme = DarkTheme()
        assertRenders(400, 200, "$base/themeDark.png", view)
    }

    @Test
    fun testDrawWidgetTheme() = runBlocking {
        view.theme = WidgetTheme()
        assertRenders(400, 200, "$base/themeWidget.png", view)
    }

    @Test
    fun testDrawOffset() = runBlocking {
        view.dataOffset = 2
        assertRenders(400, 200, "$base/scroll.png", view)
    }
}
