package org.isoron.uhabits.widgets

import android.content.Context
import org.isoron.uhabits.HabitsApplication

class StackWidgetProvider : BaseWidgetProvider() {

    override fun getWidgetFromId(context: Context, id: Int): StackGroupWidget {
        val habitIds = getHabitGroupFromWidget(context, id)
        return StackGroupWidget(context, id, habitIds)
    }

    private fun getHabitGroupFromWidget(context: Context, widgetId: Int) : List<Long> {
        val app = context.getApplicationContext() as HabitsApplication
        val widgetPrefs = app.component.widgetPreferences
        val habitIds = widgetPrefs.getHabitIdsGroupFromWidgetId(widgetId)
        return habitIds
    }
}
