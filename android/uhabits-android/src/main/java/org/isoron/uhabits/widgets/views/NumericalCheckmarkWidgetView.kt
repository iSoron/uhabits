package org.isoron.uhabits.widgets.views

import android.content.Context
import android.util.AttributeSet

import org.isoron.androidbase.utils.StyledResources
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.dialogs.NumberPickerFactory
import org.isoron.uhabits.activities.habits.list.views.toShortString
import org.isoron.uhabits.core.models.Checkmark
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior


class NumericalCheckmarkWidgetView : CheckmarkWidgetView {

    private var checkmarkState : Int = Checkmark.UNCHECKED

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    /**
     * @param state the new state/style of the widget, either:
     * - Checkmark.CHECKED_EXPLICITLY
     * - Checkmark.CHECKED_IMPLICITLY
     * - Checkmark.UNCHECKED
     */
    fun setCheckmarkState(state : Int) {
        checkmarkState = state
    }

    override fun getCheckmarkState(): Int {
        return checkmarkState
    }

    override fun getText(): String {
        val numberValue : Double = Habit.checkMarkValueToDouble(checkmarkValue)
        return numberValue.toShortString()
    }

}
