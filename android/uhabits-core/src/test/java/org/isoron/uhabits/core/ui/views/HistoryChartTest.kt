/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.components

import kotlinx.coroutines.runBlocking
import org.isoron.platform.gui.assertRenders
import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.DayOfWeek.SUNDAY
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.views.DarkTheme
import org.isoron.uhabits.core.ui.views.HistoryChart
import org.isoron.uhabits.core.ui.views.HistoryChart.Square.DIMMED
import org.isoron.uhabits.core.ui.views.HistoryChart.Square.HATCHED
import org.isoron.uhabits.core.ui.views.HistoryChart.Square.OFF
import org.isoron.uhabits.core.ui.views.HistoryChart.Square.ON
import org.isoron.uhabits.core.ui.views.LightTheme
import org.isoron.uhabits.core.ui.views.WidgetTheme
import org.junit.Test
import java.util.Locale

class HistoryChartTest {
    val base = "views/HistoryChart"

    val view = HistoryChart(
        today = LocalDate(2015, 1, 25),
        paletteColor = PaletteColor(7),
        theme = LightTheme(),
        dateFormatter = JavaLocalDateFormatter(Locale.US),
        firstWeekday = SUNDAY,
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
        }
    )

    // TODO: Label overflow
    // TODO: onClick
    // TODO: HistoryEditorDialog
    // TODO: Remove excessive padding on widgets

    @Test
    fun testDraw() = runBlocking {
        assertRenders(400, 200, "$base/base.png", view)
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
