package org.isoron.uhabits.widgets.views

import android.content.Context
import android.util.AttributeSet

import org.isoron.androidbase.utils.StyledResources
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.dialogs.NumberPickerFactory
import org.isoron.uhabits.activities.habits.list.views.toShortString
import org.isoron.uhabits.core.models.Checkmark
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior


class NumericalCheckmarkWidgetView : CheckmarkWidgetView {

    private var callBackReciever : CallBackReciever = CallBackReciever()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun refresh() {
        if (backgroundPaint == null || frame == null || ring == null) return

        //right now most of this code is copied over from the regular CheckmarkWidget. This is for testing purposes, just to get something working.

        val res = StyledResources(context)

        val text: String
        val bgColor: Int
        val fgColor: Int
        val numberValue : Double = checkmarkValue / 1000.0
        text = numberValue.toShortString()
        bgColor = activeColor
        fgColor = res.getColor(R.attr.highContrastReverseTextColor)

        setShadowAlpha(0x4f)
        rebuildBackground()

        backgroundPaint!!.color = bgColor
        frame.setBackgroundDrawable(background)

        ring.percentage = percentage
        ring.color = fgColor
        ring.setBackgroundColor(bgColor)
        ring.setText(text)

        label.text = name
        label.setTextColor(fgColor)

        requestLayout()
        postInvalidate()
    }

    class CallBackReciever : ListHabitsBehavior.NumberPickerCallback{
        override fun onNumberPicked(newValue: Double) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    fun showNumberSelector() {
        val numberPickerFactory = NumberPickerFactory(context)
        numberPickerFactory.create(1000.0, "This is a test", callBackReciever).show()
    }

}
