/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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
package org.isoron.androidbase.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import org.isoron.androidbase.R

class StyledResources(private val context: Context) {
    fun getBoolean(@AttrRes attrId: Int): Boolean = getFromTypedArray(attrId) { getBoolean(0, false) }

    fun getDimension(@AttrRes attrId: Int): Int = getFromTypedArray(attrId) { getDimensionPixelSize(0, 0) }

    fun getColor(@AttrRes attrId: Int): Int = getFromTypedArray(attrId) { getColor(0, 0) }

    fun getDrawable(@AttrRes attrId: Int): Drawable? = getFromTypedArray(attrId) { getDrawable(0) }

    fun getFloat(@AttrRes attrId: Int): Float = getFromTypedArray(attrId) { getFloat(0, 0f) }

    fun getResource(@AttrRes attrId: Int): Int = getFromTypedArray(attrId) { getResourceId(0, -1) }

    val palette: IntArray
        get() {
            val resourceId = getResource(R.attr.palette)
            if (resourceId < 0) throw RuntimeException("palette resource not found")
            return context.resources.getIntArray(resourceId)
        }

    private inline fun <TResult> getFromTypedArray(@AttrRes attrId: Int, resultProvider: TypedArray.() -> TResult): TResult {
        val ta = getTypedArray(attrId)
        val result = ta.resultProvider()
        ta.recycle()
        return result
    }

    private fun getTypedArray(@AttrRes attrId: Int): TypedArray {
        val attrs = intArrayOf(attrId)
        return fixedTheme?.let { context.theme.obtainStyledAttributes(it, attrs) }
                ?: context.obtainStyledAttributes(attrs)
    }

    companion object {
        private var fixedTheme: Int? = null

        @JvmStatic
        fun setFixedTheme(theme: Int?) {
            fixedTheme = theme
        }

    }
}