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

class NumberButtonTest : BaseViewTest() {
    val base = "components/NumberButton"

    @Test
    fun testFormatValue() = asyncTest{
        assertEquals("0.12", 0.1235.toShortString())
        assertEquals("0.1", 0.1000.toShortString())
        assertEquals("5", 5.0.toShortString())
        assertEquals("5.25", 5.25.toShortString())
        assertEquals("12.3", 12.3456.toShortString())
        assertEquals("123", 123.123.toShortString())
        assertEquals("321", 321.2.toShortString())
        assertEquals("4.3k", 4321.2.toShortString())
        assertEquals("54.3k", 54321.2.toShortString())
        assertEquals("654k", 654321.2.toShortString())
        assertEquals("7.7M", 7654321.2.toShortString())
        assertEquals("87.7M", 87654321.2.toShortString())
        assertEquals("988M", 987654321.2.toShortString())
        assertEquals("2.0G", 1987654321.2.toShortString())
    }

    @Test
    fun testRenderAbove() = asyncTest {
        val btn = NumberButton(theme.color(8), 500.0, 100.0, "steps", theme)
        assertRenders(48, 48, "$base/render_above.png", btn)
    }

    @Test
    fun testRenderBelow() = asyncTest {
        val btn = NumberButton(theme.color(8), 99.0, 100.0, "steps", theme)
        assertRenders(48, 48, "$base/render_below.png", btn)
    }
}
