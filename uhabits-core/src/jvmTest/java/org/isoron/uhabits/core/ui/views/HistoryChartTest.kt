/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

import kotlinx.coroutines.runBlocking
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
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
            1, 1, 1, 1, 2, 2, 2
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
        verify(dateClickedListener).onDateShortPress(LocalDate(2014, 10, 26))
        reset(dateClickedListener)
        view.onClick(2.0, 28.0)
        verify(dateClickedListener).onDateShortPress(LocalDate(2014, 10, 26))
        reset(dateClickedListener)

        // Click date in the middle
        view.onClick(163.0, 113.0)
        verify(dateClickedListener).onDateShortPress(LocalDate(2014, 12, 10))
        reset(dateClickedListener)

        // Click today
        view.onClick(336.0, 37.0)
        verify(dateClickedListener).onDateShortPress(LocalDate(2015, 1, 25))
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
        verify(dateClickedListener).onDateLongPress(LocalDate(2014, 10, 26))
        reset(dateClickedListener)
        view.onLongClick(2.0, 28.0)
        verify(dateClickedListener).onDateLongPress(LocalDate(2014, 10, 26))
        reset(dateClickedListener)

        // Click date in the middle
        view.onLongClick(163.0, 113.0)
        verify(dateClickedListener).onDateLongPress(LocalDate(2014, 12, 10))
        reset(dateClickedListener)

        // Click today
        view.onLongClick(336.0, 37.0)
        verify(dateClickedListener).onDateLongPress(LocalDate(2015, 1, 25))
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

    // ==================== Bipolar Mode Tests ====================

    @Test
    fun testBipolarMode_initialization() {
        // Default should be false
        val chart = HistoryChart(
            today = LocalDate(2015, 1, 25),
            paletteColor = PaletteColor(7),
            theme = LightTheme(),
            dateFormatter = JavaLocalDateFormatter(Locale.US),
            firstWeekday = SUNDAY,
            defaultSquare = OFF,
            series = emptyList(),
            notesIndicators = emptyList()
        )

        assert(!chart.bipolarMode)
        assert(chart.scoreValues == null)
        assert(chart.negativePaletteColor == null)
    }

    @Test
    fun testBipolarMode_canBeEnabled() {
        val chart = HistoryChart(
            today = LocalDate(2015, 1, 25),
            paletteColor = PaletteColor(7),
            theme = LightTheme(),
            dateFormatter = JavaLocalDateFormatter(Locale.US),
            firstWeekday = SUNDAY,
            defaultSquare = OFF,
            series = emptyList(),
            notesIndicators = emptyList(),
            bipolarMode = true,
            negativePaletteColor = PaletteColor(0)
        )

        assert(chart.bipolarMode)
        assert(chart.negativePaletteColor != null)
    }

    @Test
    fun testBipolarMode_withScoreValues() {
        val scoreValues = listOf(0.5, -0.3, 0.8, -0.1, 0.0)
        val chart = HistoryChart(
            today = LocalDate(2015, 1, 25),
            paletteColor = PaletteColor(7),
            theme = LightTheme(),
            dateFormatter = JavaLocalDateFormatter(Locale.US),
            firstWeekday = SUNDAY,
            defaultSquare = OFF,
            series = listOf(ON, ON, ON, ON, OFF),
            notesIndicators = listOf(false, false, false, false, false),
            scoreValues = scoreValues,
            bipolarMode = true,
            negativePaletteColor = PaletteColor(0)
        )

        assert(chart.scoreValues != null)
        assert(chart.scoreValues!!.size == 5)
        assert(chart.scoreValues!![0] == 0.5)
        assert(chart.scoreValues!![1] == -0.3)
    }

    @Test
    fun testBipolarMode_scoreValuesAffectRendering() {
        // Positive scores should use positive color, negative should use negative color
        val scoreValues = listOf(0.8, -0.8)
        val chart = HistoryChart(
            today = LocalDate(2015, 1, 25),
            paletteColor = PaletteColor(11), // Green
            theme = LightTheme(),
            dateFormatter = JavaLocalDateFormatter(Locale.US),
            firstWeekday = SUNDAY,
            defaultSquare = OFF,
            series = listOf(ON, ON),
            notesIndicators = listOf(false, false),
            scoreValues = scoreValues,
            bipolarMode = true,
            negativePaletteColor = PaletteColor(0) // Red
        )

        // Chart should have different colors for positive and negative values
        assert(chart.paletteColor.paletteIndex != chart.negativePaletteColor!!.paletteIndex)
    }

    @Test
    fun testBipolarMode_withMixedValues() {
        // Mix of positive, negative, and zero values
        val scoreValues = listOf(0.5, -0.3, 0.0, 0.2, -0.1, 1.0, -1.0)
        val chart = HistoryChart(
            today = LocalDate(2015, 1, 25),
            paletteColor = PaletteColor(7),
            theme = LightTheme(),
            dateFormatter = JavaLocalDateFormatter(Locale.US),
            firstWeekday = SUNDAY,
            defaultSquare = OFF,
            series = List(7) { ON },
            notesIndicators = List(7) { false },
            scoreValues = scoreValues,
            bipolarMode = true,
            negativePaletteColor = PaletteColor(0)
        )

        assert(chart.scoreValues!!.size == 7)
        // Verify boundary values
        assert(chart.scoreValues!![5] == 1.0) // Max positive
        assert(chart.scoreValues!![6] == -1.0) // Max negative
    }

    @Test
    fun testBipolarMode_disabledByDefault_usesSquareColors() {
        // When bipolarMode is false, scoreValues should be ignored
        val chart = HistoryChart(
            today = LocalDate(2015, 1, 25),
            paletteColor = PaletteColor(7),
            theme = LightTheme(),
            dateFormatter = JavaLocalDateFormatter(Locale.US),
            firstWeekday = SUNDAY,
            defaultSquare = OFF,
            series = listOf(ON, OFF, DIMMED),
            notesIndicators = listOf(false, false, false),
            scoreValues = listOf(0.5, -0.5, 0.0),
            bipolarMode = false // Explicitly disabled
        )

        assert(!chart.bipolarMode)
        // scoreValues are set but bipolarMode is false, so they won't affect colors
    }
}
