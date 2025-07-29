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
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.utils.StyledResources
import kotlin.math.max
import androidx.core.graphics.withTranslation
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.ui.screens.habits.show.views.IndividualHabitListState
import org.isoron.uhabits.utils.dim
import kotlin.math.min
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.activities.habits.list.views.CheckmarkButtonView
import org.isoron.uhabits.activities.habits.list.views.NumberButtonView
import org.isoron.uhabits.utils.dp



class HabitListChart : View {
    private var habits = emptyList<IndividualHabitListState>()
    private var weekDayStrings = emptyList<String>()
    private var dateStrings = emptyList<String>()
    private var amtHabits = 0
    private var maxCheckMarks = 0
    private var numCheckMarks = 0

    private var habitRowSize = 0
    private var padding = dpToPixels(context, 4f)
    private var checkMarkSize =  dim(R.dimen.checkmarkWidth) * .85f
    private var ringSize = 18.dpToPx()
    private val minTextBoxWidth = dim(R.dimen.checkmarkWidth)
    private var textBoxSize = minTextBoxWidth

    private val rect = RectF()
    private val barRect = RectF()

    private var backGroundPaint: Paint? = null
    private var backGroundColor = 0
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
        val ringSizeWithPadding = ringSize + (padding * 2.5)
        val widthLeftOver = width - ringSizeWithPadding

        if ((widthLeftOver - checkMarkSize - padding) < minTextBoxWidth) {
            numCheckMarks = 1
            textBoxSize = (width - checkMarkSize - padding - ringSizeWithPadding).toFloat()
        }
        numCheckMarks = ((((width ) * 0.64) / checkMarkSize)).toInt()
        textBoxSize = (widthLeftOver - (checkMarkSize * numCheckMarks)).toFloat()
        if (textBoxSize < minTextBoxWidth && numCheckMarks > 1) {
            numCheckMarks--
            textBoxSize += checkMarkSize
        }
        else if (textBoxSize - checkMarkSize >= minTextBoxWidth * 1.5){
            numCheckMarks++
            textBoxSize -= checkMarkSize
        }
        if (numCheckMarks > maxCheckMarks)
            numCheckMarks = maxCheckMarks

    }

    private fun init() {
        val res = StyledResources(context)

        backGroundPaint = Paint()
        backGroundPaint!!.textAlign = Paint.Align.CENTER
        backGroundPaint!!.isAntiAlias = true
        backGroundPaint!!.color = res.getColor(R.attr.cardBgColor)

        backGroundColor = res.getColor(R.attr.cardBgColor)
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
        val checkMarkCenterY = rect.centerY()

        // Draw dates
        repeat(numCheckMarks) { index ->

            val centerX = rect.right - ((checkMarkSize * (index + 1)) - (checkMarkSize / 2f) + padding)

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
        val ringSize  = ringSize.toInt()
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
        val checkMarkCenterY = rect.centerY()

        for (index in 1..numCheckMarks) { // 1 , 2, 3 if  numCheckMarks == 3

            var habitCheckIndex = numCheckMarks - index
            if (preferences.isCheckmarkSequenceReversed){
                habitCheckIndex = index - 1
            }

            // Checkbox Rectangle
            val centerX = rect.right - ((checkMarkSize * index) - (checkMarkSize / 2f) + padding)
            val centerY = checkMarkCenterY
            val checkRect = RectF()
            checkRect.set(
                centerX - (checkMarkSize / 2f), // + padding * 3),
                centerY - checkMarkSize / 2f,
                centerX + (checkMarkSize / 2f), // + padding * 3),
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
        textPaint.color = habit.color.toInt() // Add color to name
        drawAdaptiveText(
            canvas = canvas,
            text = habit.name,
            x = (ringCenterX + ringSize/2 + padding),
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
        canvas.withTranslation(centerX, centerY) {
            val ringSize = dp(16f).toInt()
            val scoreRing = RingView(context).apply {
                setThickness(dp(3.5f))
                setColor(color)
                setPercentage(percentage)
                setIsTransparencyEnabled(true)
            }

            scoreRing.measure(
                MeasureSpec.makeMeasureSpec(ringSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(ringSize, MeasureSpec.EXACTLY)
            )
            scoreRing.layout(0, 0, ringSize, ringSize)

            canvas.translate(-ringSize/2f, -ringSize/2f)
            scoreRing.draw(canvas)
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
        val button = NumberButtonView(context, preferences)

        button.value = value
        button.notes = ""
        button.color = paint.color
        button.targetType = targetType
        button.threshold = threshold
        button.units = units.substring(0, min(units.length, 7))

        val buttonWidth = (checkMarkSize * 1.1f ).toInt()
        button.layout(0, 0, buttonWidth, habitRowSize)

        canvas.withTranslation(checkRect.centerX() - button.width / 2f, checkRect.centerY() - button.height / 2f) {
            button.draw(canvas)
        }

    }

    private fun drawNonNumeric(
        canvas: Canvas,
        checkRect : RectF,
        value: Int,
        paint: Paint)
    {

        // Which CheckMark
        val button = CheckmarkButtonView(context, preferences)
        button.value = value
        button.notes = ""
        button.color = paint.color

        canvas.withTranslation(checkRect.centerX() , checkRect.centerY()) {
            button.draw(canvas)
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