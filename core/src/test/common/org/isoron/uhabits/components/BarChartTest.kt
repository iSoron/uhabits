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

import org.isoron.*
import org.isoron.platform.time.*
import org.isoron.uhabits.*
import kotlin.test.*

class BarChartTest : BaseViewTest() {
    val base = "components/BarChart"
    val today = LocalDate(2015, 1, 25)
    val dailyAxis = (0..100).map { today.minus(it) }
    val weeklyAxis = (0..100).map { today.minus(it * 7) }
    val monthlyAxis = (0..100).map { today.minus(it * 30) }
    val yearlyAxis = (0..100).map { today.minus(it * 365) }
    val fmt = DependencyResolver.getDateFormatter(Locale.US)
    val component = BarChart(theme, fmt)

    val series1 = listOf(200.0, 80.0, 150.0, 437.0, 50.0, 80.0, 420.0,
                         350.0, 100.0, 375.0, 300.0, 50.0, 60.0, 350.0,
                         125.0)

    val series2 = listOf(300.0, 500.0, 280.0, 50.0, 425.0, 300.0, 150.0,
                         10.0, 50.0, 200.0, 230.0, 20.0, 60.0, 34.0, 100.0)

    init {
        component.axis = dailyAxis
        component.series.add(series1)
        component.colors.add(theme.color(1))
    }

    @Test
    fun testDraw() = asyncTest {
        assertRenders(400, 200, "$base/base.png", component)
    }

    @Test
    fun testDrawWeeklyAxis() = asyncTest {
        component.axis = weeklyAxis
        assertRenders(400, 200, "$base/axis-weekly.png", component)
    }

    @Test
    fun testDrawMonthlyAxis() = asyncTest {
        component.axis = monthlyAxis
        assertRenders(400, 200, "$base/axis-monthly.png", component)
    }

    @Test
    fun testDrawYearlyAxis() = asyncTest {
        component.axis = yearlyAxis
        assertRenders(400, 200, "$base/axis-yearly.png", component)
    }

    @Test
    fun testDrawTwoSeries() = asyncTest {
        component.series.add(series2)
        component.colors.add(theme.color(3))
        assertRenders(400, 200, "$base/2-series.png", component)
    }
}