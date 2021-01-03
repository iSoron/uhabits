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
import org.isoron.uhabits.*
import kotlin.test.*

class RingTest : BaseViewTest() {
    val base = "components/Ring"

    @Test
    fun testDraw() = asyncTest {
        val component = Ring(theme.color(8),
                             percentage = 0.30,
                             thickness = 5.0,
                             radius = 30.0,
                             theme = theme,
                             label = true)
        assertRenders(60, 60, "$base/draw1.png", component)
    }
}