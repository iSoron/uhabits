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

package org.isoron.platform.gui

import android.content.Context
import android.util.AttributeSet

open class AndroidView<T : View>(
    context: Context,
    attrs: AttributeSet? = null,
) : android.view.View(context, attrs) {

    var view: T? = null
    val canvas = AndroidCanvas()

    override fun onDraw(canvas: android.graphics.Canvas) {
        this.canvas.context = context
        this.canvas.innerCanvas = canvas
        this.canvas.innerWidth = width
        this.canvas.innerHeight = height
        this.canvas.innerDensity = resources.displayMetrics.density.toDouble()
        view?.draw(this.canvas)
    }
}
