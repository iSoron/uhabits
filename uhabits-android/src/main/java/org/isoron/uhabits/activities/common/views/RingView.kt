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
package org.isoron.uhabits.activities.common.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.AttributeSetUtils.getAttribute
import org.isoron.uhabits.utils.AttributeSetUtils.getBooleanAttribute
import org.isoron.uhabits.utils.AttributeSetUtils.getColorAttribute
import org.isoron.uhabits.utils.AttributeSetUtils.getFloatAttribute
import org.isoron.uhabits.utils.ColorUtils.setAlpha
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.InterfaceUtils.getFontAwesome
import org.isoron.uhabits.utils.InterfaceUtils.spToPixels
import org.isoron.uhabits.utils.PaletteUtils.getAndroidTestColor
import org.isoron.uhabits.utils.StyledResources
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class RingView : View {
    private var color: Int
    private var precision: Float
    private var percentage: Float
    private var diameter = 1
    private var thickness: Float
    private var rect: RectF? = null
    private var pRing: TextPaint? = null
    private var backgroundColor: Int? = null
    private var inactiveColor: Int? = null
    private var em = 0f
    private var text: String?
    private var textSize: Float
    private var enableFontAwesome = false
    private var internalDrawingCache: Bitmap? = null
    private var cacheCanvas: Canvas? = null
    private var isTransparencyEnabled = false

    constructor(context: Context?) : super(context) {
        percentage = 0.0f
        precision = 0.01f
        color = getAndroidTestColor(0)
        thickness = dpToPixels(getContext(), 2f)
        text = ""
        textSize = getDimension(context!!, R.dimen.smallTextSize)
        init()
    }

    constructor(ctx: Context?, attrs: AttributeSet?) : super(ctx, attrs) {
        percentage = getFloatAttribute(ctx!!, attrs!!, "percentage", 0f)
        precision = getFloatAttribute(ctx, attrs, "precision", 0.01f)
        color = getColorAttribute(ctx, attrs, "color", 0)!!
        backgroundColor = getColorAttribute(ctx, attrs, "backgroundColor", null)
        inactiveColor = getColorAttribute(ctx, attrs, "inactiveColor", null)
        thickness = getFloatAttribute(ctx, attrs, "thickness", 0f)
        thickness = dpToPixels(ctx, thickness)
        val defaultTextSize = getDimension(ctx, R.dimen.smallTextSize)
        textSize = getFloatAttribute(ctx, attrs, "textSize", defaultTextSize)
        textSize = spToPixels(ctx, textSize)
        text = getAttribute(ctx, attrs, "text", "")
        enableFontAwesome = getBooleanAttribute(ctx, attrs, "enableFontAwesome", false)
        init()
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        invalidate()
    }

    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }

    fun getColor(): Int {
        return color
    }

    fun setIsTransparencyEnabled(isTransparencyEnabled: Boolean) {
        this.isTransparencyEnabled = isTransparencyEnabled
    }

    fun setPercentage(percentage: Float) {
        this.percentage = percentage
        invalidate()
    }

    fun setPrecision(precision: Float) {
        this.precision = precision
        invalidate()
    }

    fun setText(text: String?) {
        this.text = text
        invalidate()
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
    }

    fun setThickness(thickness: Float) {
        this.thickness = thickness
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val activeCanvas: Canvas?
        if (isTransparencyEnabled) {
            if (internalDrawingCache == null) reallocateCache()
            activeCanvas = cacheCanvas
            internalDrawingCache!!.eraseColor(Color.TRANSPARENT)
        } else {
            activeCanvas = canvas
        }
        pRing!!.color = color
        rect!![0f, 0f, diameter.toFloat()] = diameter.toFloat()
        val angle = 360 * (percentage / precision).roundToLong() * precision
        activeCanvas!!.drawArc(rect!!, -90f, angle, true, pRing!!)
        pRing!!.color = inactiveColor!!
        activeCanvas.drawArc(rect!!, angle - 90, 360 - angle, true, pRing!!)
        if (thickness > 0) {
            if (isTransparencyEnabled) pRing!!.xfermode = XFERMODE_CLEAR else pRing!!.color =
                backgroundColor!!
            rect!!.inset(thickness, thickness)
            activeCanvas.drawArc(rect!!, 0f, 360f, true, pRing!!)
            pRing!!.xfermode = null
            pRing!!.color = color
            pRing!!.textSize = textSize
            if (enableFontAwesome) pRing!!.typeface = getFontAwesome(context)
            activeCanvas.drawText(
                text!!,
                rect!!.centerX(),
                rect!!.centerY() + 0.4f * em,
                pRing!!
            )
        }
        if (activeCanvas !== canvas) canvas.drawBitmap(internalDrawingCache!!, 0f, 0f, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        diameter = max(1, min(height, width))
        pRing!!.textSize = textSize
        em = pRing!!.measureText("M")
        setMeasuredDimension(diameter, diameter)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isTransparencyEnabled) reallocateCache()
    }

    private fun init() {
        pRing = TextPaint()
        pRing!!.isAntiAlias = true
        pRing!!.color = color
        pRing!!.textAlign = Paint.Align.CENTER
        val res = StyledResources(context)
        if (backgroundColor == null) backgroundColor = res.getColor(R.attr.cardBgColor)
        if (inactiveColor == null) inactiveColor = res.getColor(R.attr.contrast100)
        inactiveColor = setAlpha(inactiveColor!!, 0.1f)
        rect = RectF()
    }

    private fun reallocateCache() {
        if (internalDrawingCache != null) internalDrawingCache!!.recycle()
        val newDrawingCache = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
        internalDrawingCache = newDrawingCache
        cacheCanvas = Canvas(newDrawingCache)
    }

    fun getPercentage(): Float {
        return percentage
    }

    fun getPrecision(): Float {
        return precision
    }

    companion object {
        val XFERMODE_CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
}
