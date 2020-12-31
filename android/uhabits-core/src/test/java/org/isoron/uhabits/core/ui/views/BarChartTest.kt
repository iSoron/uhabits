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
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.ui.views.BarChart
import org.isoron.uhabits.core.ui.views.LightTheme
import org.junit.Test
import java.util.Locale

class BarChartTest {
    val base = "components/BarChart"
    val today = LocalDate(2015, 1, 25)
    val fmt = JavaLocalDateFormatter(Locale.US)
    val theme = LightTheme()
    val component = BarChart(theme, fmt)
    val axis = (0..100).map { today.minus(it) }
    val series1 = listOf(200.0, 0.0, 150.0, 137.0, 0.0, 0.0, 500.0, 30.0, 100.0, 0.0, 300.0)

    @Test
    fun testDraw() = runBlocking {
        component.axis = axis
        component.series.add(series1)
        component.colors.add(theme.color(8))
        assertRenders(300, 200, "$base/base.png", component)
    }
}
