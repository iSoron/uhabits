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

package org.isoron.platform.gui

import org.isoron.*
import org.isoron.uhabits.*
import kotlin.test.*

class CanvasTest: BaseViewTest() {
    @Test
    fun run() = asyncTest{
        val canvas = DependencyResolver.createCanvas(500, 400)

        canvas.setColor(Color(0x303030))
        canvas.fillRect(0.0, 0.0, 500.0, 400.0)

        canvas.setColor(Color(0x606060))
        canvas.setStrokeWidth(25.0)
        canvas.drawRect(100.0, 100.0, 300.0, 200.0)

        canvas.setColor(Color(0xFFFF00))
        canvas.setStrokeWidth(1.0)
        canvas.drawRect(0.0, 0.0, 100.0, 100.0)
        canvas.fillCircle(50.0, 50.0, 30.0)
        canvas.drawRect(0.0, 100.0, 100.0, 100.0)
        canvas.fillArc(50.0, 150.0, 30.0, 90.0, 135.0)
        canvas.drawRect(0.0, 200.0, 100.0, 100.0)
        canvas.fillArc(50.0, 250.0, 30.0, 90.0, -135.0)
        canvas.drawRect(0.0, 300.0, 100.0, 100.0)
        canvas.fillArc(50.0, 350.0, 30.0, 45.0, 90.0)

        canvas.setColor(Color(0xFF0000))
        canvas.setStrokeWidth(2.0)
        canvas.drawLine(0.0, 0.0, 500.0, 400.0)
        canvas.drawLine(500.0, 0.0, 0.0, 400.0)

        canvas.setFont(Font.BOLD)
        canvas.setFontSize(50.0)
        canvas.setColor(Color(0x00FF00))
        canvas.setTextAlign(TextAlign.CENTER)
        canvas.drawText("HELLO", 250.0, 100.0)

        canvas.setTextAlign(TextAlign.RIGHT)
        canvas.drawText("HELLO", 250.0, 150.0)

        canvas.setTextAlign(TextAlign.LEFT)
        canvas.drawText("HELLO", 250.0, 200.0)

        canvas.setFont(Font.FONT_AWESOME)
        canvas.drawText(FontAwesome.CHECK, 250.0, 300.0)

        assertRenders("components/CanvasTest.png", canvas)
    }
}