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
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener

object InterfaceUtils {
    private var fontAwesome: Typeface? = null
    private var fixedResolution: Float? = null

    @JvmStatic
    fun setFixedResolution(f: Float) {
        fixedResolution = f
    }

    @JvmStatic
    fun getFontAwesome(context: Context): Typeface? {
        if (fontAwesome == null) fontAwesome = Typeface.createFromAsset(context.assets,
                "fontawesome-webfont.ttf")
        return fontAwesome
    }

    @JvmStatic
    fun dpToPixels(context: Context, dp: Float): Float =
            getPixels(
                    context,
                    fixedResolutionTransform = { dp * it },
                    actualResolutionTransform = { TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, it) }
            )

    @JvmStatic
    fun spToPixels(context: Context, sp: Float): Float =
            getPixels(
                    context,
                    fixedResolutionTransform = { sp * it },
                    actualResolutionTransform = { TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, it) }
            )

    @JvmStatic
    fun getDimension(context: Context, id: Int): Float {
        val dim = context.resources.getDimension(id)
        return fixedResolution
                ?.let { fixedRes ->
                    val dm = context.resources.displayMetrics
                    val actualDensity = dm.density
                    dim / actualDensity * fixedRes
                }
                ?: dim
    }

    fun setupEditorAction(parent: ViewGroup, listener: OnEditorActionListener) {
        for (i in 0 until parent.childCount) {
            when (val child = parent.getChildAt(i)) {
                is ViewGroup -> setupEditorAction(child, listener)
                is TextView -> child.setOnEditorActionListener(listener)
            }
        }
    }

    private fun getPixels(
            context: Context,
            fixedResolutionTransform: (Float) -> Float,
            actualResolutionTransform: (DisplayMetrics) -> Float
    ): Float =
            fixedResolution?.let(fixedResolutionTransform)
                    ?: run {
                        val resources = context.resources
                        val metrics = resources.displayMetrics
                        return actualResolutionTransform(metrics)
                    }
}