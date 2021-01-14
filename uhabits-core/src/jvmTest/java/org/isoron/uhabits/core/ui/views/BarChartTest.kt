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

import kotlinx.coroutines.runBlocking
import org.isoron.platform.gui.assertRenders
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.LocalDate
import org.junit.Test
import java.util.Locale

class BarChartTest {
    val base = "views/BarChart"
    val today = LocalDate(2015, 1, 25)
    private val fmt = JavaLocalDateFormatter(Locale.US)
    val theme = LightTheme()
    val component = BarChart(theme, fmt)
    private val axis = (0..100).map { today.minus(it) }
    private val series1 = listOf(200.0, 0.0, 150.0, 137.0, 0.0, 0.0, 500.0, 30.0, 100.0, 0.0, 300.0)

    init {
        component.axis = axis
        component.series.add(series1)
        component.colors.add(theme.color(8))
    }

    @Test
    fun testDraw() = runBlocking {
        assertRenders(300, 200, "$base/base.png", component)
    }

    @Test
    fun testDrawDarkTheme() = runBlocking {
        component.theme = DarkTheme()
        assertRenders(300, 200, "$base/themeDark.png", component)
    }

    @Test
    fun testDrawWidgetTheme() = runBlocking {
        component.theme = WidgetTheme()
        assertRenders(300, 200, "$base/themeWidget.png", component)
    }

    @Test
    fun testDrawWithOffset() = runBlocking {
        component.dataOffset = 5
        assertRenders(300, 200, "$base/offset.png", component)
    }
}
