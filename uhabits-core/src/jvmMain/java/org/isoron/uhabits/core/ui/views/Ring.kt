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

import org.isoron.platform.gui.Canvas
import org.isoron.platform.gui.Color
import org.isoron.platform.gui.View
import java.lang.String.format
import kotlin.math.max
import kotlin.math.min

class Ring(
    val color: Color,
    val percentage: Double,
    val thickness: Double,
    val radius: Double,
    val theme: Theme,
    val label: Boolean = false
) : View {

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()
        val angle = 360.0 * max(0.0, min(360.0, percentage))

        canvas.setColor(theme.lowContrastTextColor)
        canvas.fillCircle(width / 2, height / 2, radius)

        canvas.setColor(color)
        canvas.fillArc(width / 2, height / 2, radius, 90.0, -angle)

        canvas.setColor(theme.cardBackgroundColor)
        canvas.fillCircle(width / 2, height / 2, radius - thickness)

        if (label) {
            canvas.setColor(color)
            canvas.setFontSize(radius * 0.4)
            canvas.drawText(format("%.0f%%", percentage * 100), width / 2, height / 2)
        }
    }
}
