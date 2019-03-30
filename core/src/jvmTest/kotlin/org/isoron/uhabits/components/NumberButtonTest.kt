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

import org.hamcrest.CoreMatchers.*
import org.isoron.uhabits.*
import org.junit.*
import org.junit.Assert.*

class NumberButtonTest : BaseViewTest() {
    val base = "components/NumberButton/"

    @Test
    fun testFormatValue() {
        assertThat(0.1235.toShortString(), equalTo("0.12"))
        assertThat(0.1000.toShortString(), equalTo("0.1"))
        assertThat(5.0.toShortString(), equalTo("5"))
        assertThat(5.25.toShortString(), equalTo("5.25"))
        assertThat(12.3456.toShortString(), equalTo("12.3"))
        assertThat(123.123.toShortString(), equalTo("123"))
        assertThat(321.2.toShortString(), equalTo("321"))
        assertThat(4321.2.toShortString(), equalTo("4.3k"))
        assertThat(54321.2.toShortString(), equalTo("54.3k"))
        assertThat(654321.2.toShortString(), equalTo("654k"))
        assertThat(7654321.2.toShortString(), equalTo("7.7M"))
        assertThat(87654321.2.toShortString(), equalTo("87.7M"))
        assertThat(987654321.2.toShortString(), equalTo("988M"))
        assertThat(1987654321.2.toShortString(), equalTo("2.0G"))
    }

    @Test
    fun testRenderAbove() {
        val btn = NumberButton(theme.color(8), 500.0, 100.0, "steps", theme)
        assertRenders(96, 96, "$base/render_above.png", btn)
    }

    @Test
    fun testRenderBelow() {
        val btn = NumberButton(theme.color(8), 99.0, 100.0, "steps", theme)
        assertRenders(96, 96, "$base/render_below.png", btn)
    }

    @Test
    fun testRenderZero() {
        val btn = NumberButton(theme.color(8), 0.0, 100.0, "steps", theme)
        assertRenders(96, 96, "$base/render_zero.png", btn)
    }
}