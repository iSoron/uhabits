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
package org.isoron.uhabits.widgets.views

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.utils.StyledResources
import java.util.Arrays
import kotlin.math.max

abstract class HabitWidgetView : FrameLayout {
    protected var background: InsetDrawable? = null
    protected var backgroundPaint: Paint? = null
    protected var frame: ViewGroup? = null
    private var shadowAlpha = 0
    private var res: StyledResources? = null
    private var backgroundAlpha = 0

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init()
    }

    fun setShadowAlpha(shadowAlpha: Int) {
        this.shadowAlpha = shadowAlpha
        rebuildBackground()
    }

    fun setBackgroundAlpha(backgroundAlpha: Int) {
        this.backgroundAlpha = backgroundAlpha
        rebuildBackground()
    }

    protected abstract val innerLayoutId: Int
    fun rebuildBackground() {
        val context = context
        val shadowRadius = dpToPixels(context, 2f).toInt()
        val shadowOffset = dpToPixels(context, 1f).toInt()
        val shadowColor = Color.argb(shadowAlpha, 0, 0, 0)
        val cornerRadius = dpToPixels(context, 5f)
        val radii = FloatArray(8)
        Arrays.fill(radii, cornerRadius)
        val shape = RoundRectShape(radii, null, null)
        val innerDrawable = ShapeDrawable(shape)
        val insetLeftTop = max(shadowRadius - shadowOffset, 0)
        val insetRightBottom = shadowRadius + shadowOffset
        background = InsetDrawable(
            innerDrawable,
            insetLeftTop,
            insetLeftTop,
            insetRightBottom,
            insetRightBottom
        )
        backgroundPaint = innerDrawable.paint
        backgroundPaint?.setShadowLayer(
            shadowRadius.toFloat(),
            shadowOffset.toFloat(),
            shadowOffset.toFloat(),
            shadowColor
        )
        backgroundPaint?.color = res!!.getColor(R.attr.cardBgColor)
        backgroundPaint?.alpha = backgroundAlpha
        frame = findViewById<View>(R.id.frame) as ViewGroup
        if (frame != null) frame!!.background = background
    }

    private fun init() {
        inflate(context, innerLayoutId, this)
        res = StyledResources(context)
        shadowAlpha = (255 * res!!.getFloat(R.attr.widgetShadowAlpha)).toInt()
        rebuildBackground()
    }
}
