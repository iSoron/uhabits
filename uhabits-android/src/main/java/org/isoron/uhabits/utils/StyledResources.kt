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
package org.isoron.uhabits.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import org.isoron.uhabits.R

class StyledResources(private val context: Context) {

    fun getBoolean(@AttrRes attrId: Int): Boolean {
        val ta = getTypedArray(attrId)
        val bool = ta.getBoolean(0, false)
        ta.recycle()
        return bool
    }

    fun getDimension(@AttrRes attrId: Int): Int {
        val ta = getTypedArray(attrId)
        val dim = ta.getDimensionPixelSize(0, 0)
        ta.recycle()
        return dim
    }

    fun getColor(@AttrRes attrId: Int): Int {
        val ta = getTypedArray(attrId)
        val color = ta.getColor(0, 0)
        ta.recycle()
        return color
    }

    fun getDrawable(@AttrRes attrId: Int): Drawable? {
        val ta = getTypedArray(attrId)
        val drawable = ta.getDrawable(0)
        ta.recycle()
        return drawable
    }

    fun getFloat(@AttrRes attrId: Int): Float {
        val ta = getTypedArray(attrId)
        val f = ta.getFloat(0, 0f)
        ta.recycle()
        return f
    }

    fun getPalette(): IntArray {
        val resourceId = getResource(R.attr.palette)
        if (resourceId < 0) throw RuntimeException("palette resource not found")
        return context.resources.getIntArray(resourceId)
    }

    fun getResource(@AttrRes attrId: Int): Int {
        val ta = getTypedArray(attrId)
        val resourceId = ta.getResourceId(0, -1)
        ta.recycle()
        return resourceId
    }

    private fun getTypedArray(@AttrRes attrId: Int): TypedArray {
        val attrs = intArrayOf(attrId)
        if (fixedTheme != null) {
            return context.theme.obtainStyledAttributes(fixedTheme!!, attrs)
        }
        return context.obtainStyledAttributes(attrs)
    }

    companion object {
        private var fixedTheme: Int? = null

        @JvmStatic
        fun setFixedTheme(theme: Int?) {
            fixedTheme = theme
        }
    }
}
