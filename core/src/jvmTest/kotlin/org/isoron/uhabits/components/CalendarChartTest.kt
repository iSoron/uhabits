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

import org.isoron.platform.time.*
import org.isoron.uhabits.*
import org.junit.*
import java.util.*

class CalendarChartTest : BaseViewTest() {
    val base = "components/CalendarChart"

    @Test
    fun testDraw() {
        val component = CalendarChart(LocalDate(2015, 1, 25),
                                      theme.color(4),
                                      theme,
                                      JavaLocalDateCalculator(),
                                      JavaLocalDateFormatter(Locale.US))
        component.series = listOf(1.0, // today
                                  0.2, 0.5, 0.7, 0.0, 0.3, 0.4, 0.6,
                                  0.6, 0.0, 0.3, 0.6, 0.5, 0.8, 0.0,
                                  0.0, 0.0, 0.0, 0.6, 0.5, 0.7, 0.7,
                                  0.5, 0.5, 0.8, 0.9, 1.0, 1.0, 1.0,
                                  1.0, 1.0, 1.0, 1.0, 1.0, 0.5, 0.2)
        assertRenders(800, 400, "$base/base.png", component)

        component.scrollPosition = 2
        assertRenders(800, 400, "$base/scroll.png", component)

        component.dateFormatter = JavaLocalDateFormatter(Locale.JAPAN)
        assertRenders(800, 400, "$base/base-jp.png", component)
    }
}