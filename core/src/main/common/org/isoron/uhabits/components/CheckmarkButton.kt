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

import org.isoron.platform.gui.*

class CheckmarkButton(private val value: Int,
                      private val color: Color,
                      private val theme: Theme) : Component {
    override fun draw(canvas: Canvas) {
        canvas.setFont(Font.FONT_AWESOME)
        canvas.setFontSize(theme.smallTextSize * 1.5)
        canvas.setColor(when (value) {
                            2 -> color
                            else -> theme.lowContrastTextColor
                        })
        val text = when (value) {
            0 -> FontAwesome.TIMES
            else -> FontAwesome.CHECK
        }
        canvas.drawText(text, canvas.getWidth() / 2.0, canvas.getHeight() / 2.0)
    }
}