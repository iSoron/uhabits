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

import org.isoron.uhabits.*
import org.junit.*

class CheckmarkButtonTest : BaseViewTest() {
    val base = "components/CheckmarkButton"

    @Test
    fun testDrawExplicit() {
        val component = CheckmarkButton(2, theme.color(8), theme)
        assertRenders(96, 96, "$base/explicit.png", component)
    }

    @Test
    fun testDrawImplicit() {
        val component = CheckmarkButton(1, theme.color(8), theme)
        assertRenders(96, 96, "$base/implicit.png", component)
    }

    @Test
    fun testDrawUnchecked() {
        val component = CheckmarkButton(0, theme.color(8), theme)
        assertRenders(96, 96, "$base/unchecked.png", component)
    }
}