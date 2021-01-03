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

import org.khronos.webgl.*
import org.w3c.dom.*
import kotlin.browser.*
import kotlin.math.*

class JsImage(val canvas: JsCanvas,
              val imageData: ImageData) : Image {

    override val width: Int
        get() = imageData.width

    override val height: Int
        get() = imageData.height

    val pixels = imageData.unsafeCast<Uint16Array>()

    init {
        console.log(width, height, imageData.data.length)
    }

    override suspend fun export(path: String) {
        canvas.ctx.putImageData(imageData, 0.0, 0.0)
        val container = document.createElement("div")
        container.className = "export"
        val title = document.createElement("div")
        title.innerHTML = path
        document.body?.appendChild(container)
        container.appendChild(title)
        container.appendChild(canvas.element)
    }

    override fun getPixel(x: Int, y: Int): Color {
        val offset = 4 * (y * width + x)
        return Color(imageData.data[offset + 0] / 255.0,
                     imageData.data[offset + 1] / 255.0,
                     imageData.data[offset + 2] / 255.0,
                     imageData.data[offset + 3] / 255.0)
    }

    override fun setPixel(x: Int, y: Int, color: Color) {
        val offset = 4 * (y * width + x)
        inline fun map(x: Double): Byte {
           return (x * 255).roundToInt().unsafeCast<Byte>()
        }
        imageData.data.set(offset + 0, map(color.red))
        imageData.data.set(offset + 1, map(color.green))
        imageData.data.set(offset + 2, map(color.blue))
        imageData.data.set(offset + 3, map(color.alpha))
    }

}