package org.isoron.uhabits.widgets

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import org.isoron.uhabits.core.models.Checkmark
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.receivers.WidgetReceiver
import org.isoron.uhabits.receivers.WidgetReceiver.Companion.ACTION_SET_NUMERICAL_VALUE
import org.isoron.uhabits.utils.PaletteUtils
import org.isoron.uhabits.widgets.activities.NumericalCheckmarkWidgetActivity
import org.isoron.uhabits.widgets.views.CheckmarkWidgetView
import org.isoron.uhabits.widgets.views.NumericalCheckmarkWidgetView

class NumericalCheckmarkWidget(context: Context, widgetId: Int, habit: Habit) : CheckmarkWidget(context, widgetId, habit) {

    private lateinit var view: NumericalCheckmarkWidgetView

    override fun getOnClickPendingIntent(context: Context): PendingIntent {
        return pendingIntentFactory.setNumericalValue(context, habit, 10,null)
    }

    override fun buildView(): View {
        view = NumericalCheckmarkWidgetView(context)
        return view;
    }

    override fun refreshData(v: View) {
        (v as NumericalCheckmarkWidgetView).apply {
            setPercentage(habit.scores.todayValue.toFloat())
            setActiveColor(PaletteUtils.getColor(context, habit.color))
            setName(habit.name)
            setCheckmarkValue(habit.checkmarks.todayValue)
            setCheckmarkState(getCheckmarkState())
            refresh()
        }
    }

    private fun getCheckmarkState():Int{
        return if(habit.isCompletedToday){
            Checkmark.CHECKED_EXPLICITLY
        }else{
            Checkmark.UNCHECKED
        }
    }

}