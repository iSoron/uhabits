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

package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import org.isoron.uhabits.R
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.StyledResources
import org.isoron.uhabits.utils.getFontAwesome
import org.isoron.uhabits.utils.showMessage
import java.text.DecimalFormat
import javax.inject.Inject

private val BOLD_TYPEFACE = Typeface.create("sans-serif-condensed", Typeface.BOLD)
private val NORMAL_TYPEFACE = Typeface.create("sans-serif-condensed", Typeface.NORMAL)

fun Double.toShortString(): String = when {
    this >= 1e9 -> String.format("%.1fG", this / 1e9)
    this >= 1e8 -> String.format("%.0fM", this / 1e6)
    this >= 1e7 -> String.format("%.1fM", this / 1e6)
    this >= 1e6 -> String.format("%.1fM", this / 1e6)
    this >= 1e5 -> String.format("%.0fk", this / 1e3)
    this >= 1e4 -> String.format("%.1fk", this / 1e3)
    this >= 1e3 -> String.format("%.1fk", this / 1e3)
    this >= 1e2 -> DecimalFormat("#").format(this)
    this >= 1e1 -> DecimalFormat("#.#").format(this)
    else -> DecimalFormat("#.##").format(this)
}

class NumberButtonViewFactory
@Inject constructor(
    @ActivityContext val context: Context,
    val preferences: Preferences
) {
    fun create() = NumberButtonView(context, preferences)
}

class NumberButtonView(
    @ActivityContext context: Context,
    val preferences: Preferences
) : View(context),
    OnClickListener,
    OnLongClickListener {

    var color = 0
        set(value) {
            field = value
            invalidate()
        }

    var value = 0.0
        set(value) {
            field = value
            invalidate()
        }

    var threshold = 0.0
        set(value) {
            field = value
            invalidate()
        }

    var units = ""
        set(value) {
            field = value
            invalidate()
        }

    var onEdit: () -> Unit = {}
    private var drawer: Drawer = Drawer(context)

    init {
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    override fun onClick(v: View) {
        if (preferences.isShortToggleEnabled) onEdit()
        else showMessage(resources.getString(R.string.long_press_to_edit))
    }

    override fun onLongClick(v: View): Boolean {
        onEdit()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDimension(context, R.dimen.checkmarkWidth).toInt()
        val height = getDimension(context, R.dimen.checkmarkHeight).toInt()
        setMeasuredDimension(width, height)
    }

    private inner class Drawer(context: Context) {

        private val em: Float
        private val rect: RectF = RectF()
        private val sr = StyledResources(context)

        private val lowContrast: Int
        private val mediumContrast: Int

        private val pRegular: TextPaint = TextPaint().apply {
            textSize = getDimension(context, R.dimen.smallerTextSize)
            typeface = NORMAL_TYPEFACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        private val pBold: TextPaint = TextPaint().apply {
            textSize = getDimension(context, R.dimen.smallTextSize)
            typeface = BOLD_TYPEFACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        init {
            em = pBold.measureText("m")
            lowContrast = sr.getColor(R.attr.contrast40)
            mediumContrast = sr.getColor(R.attr.contrast60)
        }

        fun draw(canvas: Canvas) {
            val activeColor = when {
                value <= 0.0 -> lowContrast
                value < threshold -> mediumContrast
                else -> color
            }

            val label: String
            val typeface: Typeface

            when {
                value >= 0 -> {
                    label = value.toShortString()
                    typeface = BOLD_TYPEFACE
                }
                preferences.areQuestionMarksEnabled() -> {
                    label = resources.getString(R.string.fa_question)
                    typeface = getFontAwesome()
                }
                else -> {
                    label = "0"
                    typeface = BOLD_TYPEFACE
                }
            }

            pBold.color = activeColor
            pBold.typeface = typeface
            pRegular.color = activeColor

            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            canvas.drawText(label, rect.centerX(), rect.centerY(), pBold)

            rect.offset(0f, 1.2f * em)
            canvas.drawText(units, rect.centerX(), rect.centerY(), pRegular)
        }
    }
}
