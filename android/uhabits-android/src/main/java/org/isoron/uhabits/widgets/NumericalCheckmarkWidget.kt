package org.isoron.uhabits.widgets

import android.app.PendingIntent
import android.content.Context
import android.view.View
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.widgets.views.NumericalCheckmarkWidgetView

class NumericalCheckmarkWidget(context: Context, widgetId: Int, habit: Habit) : CheckmarkWidget(context, widgetId, habit) {

    private lateinit var view: NumericalCheckmarkWidgetView

    override fun getOnClickPendingIntent(context: Context): PendingIntent {
        view.showNumberSelector()
        return pendingIntentFactory.setNumericalValue(habit, 10,null)
    }

    override fun buildView(): View {
        view = NumericalCheckmarkWidgetView(context)
        return view;
    }
}