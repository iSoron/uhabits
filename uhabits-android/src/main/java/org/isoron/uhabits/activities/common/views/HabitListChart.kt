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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.StyledResources
import kotlin.math.max
import androidx.core.graphics.withTranslation
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.activities.habits.list.views.toShortString
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.models.NumericalHabitType.AT_LEAST
import org.isoron.uhabits.core.models.NumericalHabitType.AT_MOST
import org.isoron.uhabits.core.ui.screens.habits.show.views.IndividualHabitListState
import org.isoron.uhabits.utils.dim
import kotlin.math.min
import org.isoron.platform.gui.toInt

private val BOLD_TYPEFACE = Typeface.create("sans-serif-condensed", Typeface.BOLD)
private val NORMAL_TYPEFACE = Typeface.create("sans-serif-condensed", Typeface.NORMAL)


class HabitListChart : View {
    private var habits = emptyList<IndividualHabitListState>()
    private var weekDayStrings = emptyList<String>()
    private var dateStrings = emptyList<String>()
    private var amtHabits = 0
    private var maxCheckMarks = 0
    private var numCheckMarks = 0

    private var habitRowSize = 0
    private var checkMarkSize = 18.dpToPx()
    private var textBoxSize = checkMarkSize * 2
    private var padding = dpToPixels(context, 4f)
    private var scaleFactor = 25f

    private val rect = RectF()
    private val barRect = RectF()

    private var backGroundPaint: Paint? = null
    private var lowContrastTextColor = 0 // contrast20
    private var mediumContrastTextColor = 0 // contrast40
    private var highContrastTextColor = 0 // contrast60

    private val app = context.applicationContext as HabitsApplication
    private val preferences = app.component.preferences

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12.spToPx()
        color = Color.WHITE
    }

    private val pText: TextPaint = TextPaint().apply {
        textSize = (dim(R.dimen.regularTextSize) * 1.5).toFloat()
        typeface = BOLD_TYPEFACE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val pNumber: TextPaint = TextPaint().apply {
        textSize = dim(R.dimen.smallTextSize)
        typeface = BOLD_TYPEFACE
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val pUnit: TextPaint = TextPaint().apply {
        textSize = getDimension(context, R.dimen.smallerTextSize)
        typeface = NORMAL_TYPEFACE
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        // width
        var widthSpec = widthSpec
        val width = MeasureSpec.getSize(widthSpec)

        // responsive height
        var heightSpec = heightSpec
        habitRowSize = resources.getDimensionPixelSize(R.dimen.baseSize ) + 60
        val epsilonOfRowSize = 10
        val height = MeasureSpec.getSize(heightSpec)

        amtHabits = min(habits.size, (height / habitRowSize) - 1)
        var habitHeight = habitRowSize * (amtHabits + 1)

        if (amtHabits < 1){ // Always have at least one habit. Edge Case
            amtHabits++
            habitRowSize = height / (amtHabits + 1)
            habitHeight = height
        }
        else if (habits.size != amtHabits && height - habitHeight >= habitRowSize * 0.50 && (height / (amtHabits + 1) >= habitRowSize - epsilonOfRowSize) ){ // If enough room at the bottom to fit another one, fit another one.
            amtHabits++
            habitRowSize = height / (amtHabits + 1)
            habitHeight = height
        }
        else if (height - habitHeight < habitRowSize * 0.50 && (height / (amtHabits + 1) <= habitRowSize + epsilonOfRowSize) ){ // If not enough room to fit another one, make them all slightly bigger
            habitRowSize = height / (amtHabits + 1)
            habitHeight = height
        }

        heightSpec = MeasureSpec.makeMeasureSpec(habitHeight, MeasureSpec.EXACTLY)
        widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        setMeasuredDimension(widthSpec, heightSpec)


        // Amount of Checkmarks and the size of the habit text
        val firstCheckMarkWithPadding = checkMarkSize + (padding * 4)
        val otherCheckMarkWithPadding = checkMarkSize + (padding * 8)
        fun getCheckMarkWithPadding(i: Int) = firstCheckMarkWithPadding + i * otherCheckMarkWithPadding
        val ringSizeWithPadding = checkMarkSize + (padding * 1.5)
        val rowPadding = padding * 2
        val minTextBoxSize = checkMarkSize * 2 + padding

        if (width < 150.dpToPx()) {
            Log.e("JMO", "1")

            numCheckMarks = 1
            textBoxSize = (width -  (rowPadding) - ( firstCheckMarkWithPadding ) - padding  - (ringSizeWithPadding)).toFloat()
        }
        else  {
            Log.e("JMO", "2")
            numCheckMarks = ((((width + (padding * 4) - (rowPadding)) * 0.62) / otherCheckMarkWithPadding)).toInt()
            textBoxSize = (width -  (rowPadding) - ( getCheckMarkWithPadding(numCheckMarks - 1) )- padding  - (ringSizeWithPadding)).toFloat()
            if (textBoxSize - otherCheckMarkWithPadding >= minTextBoxSize){
                numCheckMarks++
                textBoxSize -= otherCheckMarkWithPadding
            }
        }
        if (numCheckMarks > maxCheckMarks)
            numCheckMarks = maxCheckMarks

    }

    private fun init() {
        backGroundPaint = Paint()
        backGroundPaint!!.textAlign = Paint.Align.CENTER
        backGroundPaint!!.isAntiAlias = true
        val res = StyledResources(context)
        lowContrastTextColor = res.getColor(R.attr.contrast20)
        mediumContrastTextColor = res.getColor(R.attr.contrast40)
        highContrastTextColor = res.getColor(R.attr.contrast60)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (habits.isEmpty()) return

        val marginTop = (height - (habitRowSize * (amtHabits + 1))) / 2.0f
        rect[0f, marginTop, width.toFloat()] = marginTop + habitRowSize

        // Draw Dates Header
        drawHeaderRow(canvas, rect)
        rect.offset(0f, habitRowSize.toFloat())

        // Draw the habit rows
        for (i in 0 until minOf(habits.size, amtHabits)) {
            drawRow(canvas, habits[i], rect)
            rect.offset(0f, habitRowSize.toFloat())
        }
    }

    private fun drawHeaderRow(canvas: Canvas, rect: RectF) {
        val round = dpToPixels(context, 2f)

        // Draw background box
        backGroundPaint!!.color = Color.TRANSPARENT
        barRect[rect.left + padding, rect.top + habitRowSize * 0.05f, rect.right - padding] =
            rect.bottom - habitRowSize * 0.05f
        canvas.drawRoundRect(barRect, round, round, backGroundPaint!!)

        // paint
        val paint = TextPaint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = dim(R.dimen.tinyTextSize)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val em = paint.measureText("m")
        val checkMarkCenterX = ((padding * 4) + checkMarkSize/2)
        val checkMarkCenterY = rect.centerY()

        // Draw dates
        repeat(numCheckMarks) { index ->

            val centerX = rect.right - (checkMarkCenterX * (index + 1) + (padding * 4) * (index))

            val y1 = checkMarkCenterY - 0.25 * em
            val y2 = checkMarkCenterY + 1.25 * em
            var dateIndex = numCheckMarks - index - 1
            if (preferences.isCheckmarkSequenceReversed){
                dateIndex = index
            }

            canvas.drawText(weekDayStrings[dateIndex], centerX, y1.toFloat(), paint)
            canvas.drawText(dateStrings[dateIndex], centerX, y2.toFloat(), paint)
        }

    }

    private fun drawRow(canvas: Canvas, habit: IndividualHabitListState, rect: RectF) {
        val round = dpToPixels(context, 2f)

        // Draw background box
        backGroundPaint!!.color = lowContrastTextColor
        barRect[rect.left + padding, rect.top + habitRowSize * 0.05f, rect.right - padding] =
            rect.bottom - habitRowSize * 0.05f
        canvas.drawRoundRect(barRect, round, round, backGroundPaint!!)

        // ScoreRing
        val ringSize  = checkMarkSize.toInt()
        val ringCenterX = (rect.left + (padding * 1.5) + ringSize/2).toFloat()
        val ringCenterY = rect.centerY()
        drawRingView(
            canvas = canvas,
            centerX = ringCenterX,
            centerY = ringCenterY,
            percentage = habit.score,
            color = habit.color.toInt()
        )

        // CheckMarks
        val checkMarkCenterX = ((padding * 4) + checkMarkSize/2)
        val checkMarkCenterY = rect.centerY()

        for (index in 1..numCheckMarks) { // 1 , 2, 3 if  numCheckMarks == 3

            var habitCheckIndex = numCheckMarks - index
            if (preferences.isCheckmarkSequenceReversed){
                habitCheckIndex = index - 1
            }

            // Checkbox Rectangle
            val centerX = rect.right - ((checkMarkCenterX * index) + (padding * 4) * (index - 1))
            val centerY = checkMarkCenterY
            val checkRect = RectF()
            checkRect.set(
                centerX - checkMarkSize / 2f,
                centerY - checkMarkSize / 2f,
                centerX + checkMarkSize / 2f,
                centerY + checkMarkSize / 2f
            )

            val paint = Paint().apply {
                this.color = habit.color.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1.dpToPx()
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }

            if (habit.isNumerical) { // If Numerical Habit
                drawNumberCheck(
                    canvas = canvas,
                    checkRect = checkRect,
                    value = (max(-1, (habit.values[habitCheckIndex])) / 1000.0),
                    threshold = habit.targetValue,
                    units = habit.unit,
                    targetType = habit.targetType,
                    paint = paint
                )
            }
            else { // If Non Numerical Habit
                drawNonNumeric(
                    canvas = canvas,
                    checkRect = checkRect,
                    value = (max(-1, habit.values[habitCheckIndex])),
                    paint = paint
                )
            }
        }

        // Draw habit name
        drawAdaptiveText(
            canvas = canvas,
            text = habit.name,
            x = ringCenterX + ringSize/2 + padding,
            y = rect.top,
            width= textBoxSize.toInt(), // textAreaWidth,
            height = habitRowSize,
            paint = textPaint,
            maxLines = 2
        )
    }


    private fun drawRingView(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        percentage: Float,
        color: Int
    ) {
        val thickness = checkMarkSize * 0.22f

        val bgPaint = Paint().apply {
            this.color = Color.argb(25, Color.red(color), Color.green(color), Color.blue(color))
            style = Paint.Style.STROKE
            strokeWidth = thickness
            isAntiAlias = true
        }

        val fgPaint = Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = thickness
            strokeCap = Paint.Cap.BUTT
            isAntiAlias = true
        }

        val rect = RectF(
            centerX - checkMarkSize / 2 + thickness / 2,
            centerY - checkMarkSize / 2 + thickness / 2,
            centerX - checkMarkSize / 2 + checkMarkSize - thickness / 2,
            centerY - checkMarkSize / 2 + checkMarkSize - thickness / 2
        )

        // Draw background
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // Draw progress
        if (percentage > 0) {
            canvas.drawArc(rect, -90f, 360f * percentage, false, fgPaint)
        }
    }

    private fun drawAdaptiveText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        height: Int,
        paint: TextPaint,
        maxLines: Int = 2
    ) {
        // Create text layout
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setMaxLines(maxLines)
            .build()

        // if single line, top-aligned if multi-line
        val verticalPos = if (layout.lineCount == 1) {
            y + (height - layout.height) / 2
        } else {
            y + (height - layout.height) / 2
        }

        canvas.withTranslation(x, verticalPos) {
            layout.draw(this)
        }
    }


    private fun drawNumberCheck(
        canvas: Canvas,
        checkRect: RectF,
        value : Double,
        threshold: Double,
        units: String,
        targetType : NumericalHabitType,
        paint : Paint
    ) {
        // Color
        val activeColor = when {
            value == SKIP.toDouble() / 1000 -> paint.color
            value < 0.0 -> mediumContrastTextColor
            (targetType == AT_LEAST) && (value >= threshold) -> paint.color
            (targetType == AT_MOST) && (value <= threshold) -> paint.color
            else -> highContrastTextColor
        }
        paint.color = activeColor

        // Prepare text
        val numberText = if (value >= 0) value.toShortString() else "0"

        pNumber.color = activeColor
        pUnit.color = activeColor

        // Draw
        val em = pNumber.measureText("m")
        var questionMarkScale = 1.0
        val verticalSpacing = em * 0.3f

        if (units.isNotBlank()){  // if have units
            questionMarkScale = 0.8
            checkRect.offset(0f, - verticalSpacing)
        }

        // Draw Number
        when {
            value == SKIP.toDouble() / 1000 -> {
                drawSkipLine(canvas, checkRect, paint)
            }
            value >= 0 -> {
                canvas.drawText(numberText,checkRect.centerX(),checkRect.centerY() + em / 3, pNumber)
            }
            preferences.areQuestionMarksEnabled -> {
                drawSimpleText(canvas, checkRect, pNumber, "?", questionMarkScale)
            }
            else -> {
                canvas.drawText(numberText,checkRect.centerX(),checkRect.centerY() + em / 3, pNumber)
            }
        }
        // Draw Units
        if (units.isNotBlank()) {  // if have units
            val unitsSub = units.substring(0, min(units.length, 7))
            checkRect.offset(0f, +verticalSpacing + em)
            canvas.drawText(unitsSub, checkRect.centerX(), checkRect.centerY() + em / 3, pUnit)
        }

    }

    private fun drawNonNumeric(
        canvas: Canvas,
        checkRect : RectF,
        value: Int,
        paint: Paint)
    {
        // Color
        paint.color = when (value) {
            YES_MANUAL, YES_AUTO, SKIP -> paint.color
            NO -> {
                if (preferences.areQuestionMarksEnabled) {
                    highContrastTextColor
                } else {
                    mediumContrastTextColor
                }
            }
            else -> mediumContrastTextColor
        }

        // Which CheckMark
        when (value) {
            SKIP -> drawSkipLine(canvas, checkRect, paint)
            NO -> drawXMark(canvas, checkRect, paint)
            UNKNOWN -> {
                if (preferences.areQuestionMarksEnabled) {
                    drawSimpleText(canvas, checkRect, paint, "?")
                } else {
                    drawXMark(canvas, checkRect, paint)
                }
            }
            YES_AUTO -> {
                drawCheckMark(canvas, checkRect, paint, false)
                drawCheckMark(canvas, checkRect, paint, true)
            }
            else -> drawCheckMark(canvas, checkRect, paint)
        }
    }

    private fun drawCheckMark(
        canvas: Canvas,
        checkRect: RectF,
        paint: Paint,
        isAuto: Boolean = false
    ) {
        val scale = checkMarkSize / scaleFactor
        canvas.withTranslation(checkRect.left , checkRect.top) {
            scale(scale, scale)

            // Draw Checkmark
            val path = Path().apply {
                moveTo(9f, 16.17f)
                lineTo(4.83f, 12f)
                lineTo(3.41f, 13.41f)
                lineTo(9f, 19f)
                lineTo(21f, 7f)
                lineTo(19.59f, 5.59f)
                close()
            }

            if (isAuto) {
                // First draw: outline
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = paint.strokeWidth  // scale the stroke width
                drawPath(path, paint)

                // Second draw: inner fill
                paint.style = Paint.Style.FILL
                paint.color = mediumContrastTextColor  // your background color
                drawPath(path, paint)

            } else {
                // Regular checkmark
                paint.style = Paint.Style.STROKE
                drawPath(path, paint)
            }
        }
    }

    private fun drawSimpleText(
        canvas: Canvas,
        checkRect: RectF,
        paint: Paint,
        text: String,
        scale: Double = 1.0
    ) {
        pText.textSize = (dim(R.dimen.regularTextSize) * 1.5 * scale).toFloat()
        pText.color = paint.color
        val em = pText.measureText("m")

        canvas.drawText(
            text,
            checkRect.centerX(),
            checkRect.centerY() + em / 2,
            pText
        )
    }

    private fun drawSkipLine(
        canvas: Canvas,
        checkRect: RectF,
        paint: Paint,
    ) {
        val scale = checkMarkSize / scaleFactor
        canvas.withTranslation(checkRect.left, checkRect.top) {
            canvas.scale(scale, scale)

            val lineWidth = 12f
            val startX = 12f - lineWidth / 2
            val endX = 12f + lineWidth / 2
            val y = 12f

            drawLine(startX, y, endX, y, paint)
        }

    }

    private fun drawXMark(
        canvas: Canvas,
        checkRect: RectF,
        paint: Paint,
    ) {
        val scale = checkMarkSize / scaleFactor
        canvas.withTranslation(checkRect.left, checkRect.top) {
            canvas.scale(scale, scale)

            // Create the X path
            val path = Path().apply {
                moveTo(6f, 6f)
                lineTo(18f, 18f)

                moveTo(6f, 18f)
                lineTo(18f, 6f)
            }
            canvas.drawPath(path, paint)
        }

    }


    fun setHabits(habits: List<IndividualHabitListState>) {
        this.habits = habits
        requestLayout()
    }
    fun setHeaderDates(weekDayStrings : List<String>, dateStrings : List<String>){
        this.weekDayStrings = weekDayStrings
        this.dateStrings = dateStrings
        requestLayout()
    }
    fun setMaxCheckMarks(maxCheckMarks : Int){
        this.maxCheckMarks = maxCheckMarks
    }

    private fun Int.dpToPx(): Float = this * resources.displayMetrics.density
    private fun Int.spToPx(): Float = this * resources.displayMetrics.scaledDensity
}