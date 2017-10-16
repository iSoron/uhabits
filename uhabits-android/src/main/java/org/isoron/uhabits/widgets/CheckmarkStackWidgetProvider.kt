package org.isoron.uhabits.widgets

import android.content.Context
import org.isoron.uhabits.HabitsApplication

class CheckmarkStackWidgetProvider : BaseWidgetProvider() {

    override fun getWidgetFromId(context: Context, id: Int): CheckmarkStackWidget {
        val habitIds = getHabitGroupFromWidget(context, id)
        return CheckmarkStackWidget(context, id, habitIds)
    }

    private fun getHabitGroupFromWidget(context: Context, widgetId: Int) : List<Long> {
        val app = context.getApplicationContext() as HabitsApplication
        val widgetPrefs = app.component.widgetPreferences
        val habitIds = widgetPrefs.getHabitIdsGroupFromWidgetId(widgetId)
        return habitIds
    }
}
