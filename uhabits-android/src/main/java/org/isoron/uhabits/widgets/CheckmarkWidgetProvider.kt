/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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
package org.isoron.uhabits.widgets

import android.content.*
import org.isoron.uhabits.HabitsApplication

class CheckmarkWidgetProvider : BaseWidgetProvider() {
    override fun getWidgetFromId(context: Context, id: Int): BaseWidget {
        try {
            val habit = getHabitFromWidgetId(id)
            return CheckmarkWidget(context, id, habit)
        } catch (e: Exception) {
            val habitIds = getHabitIdsGroupFromWidget(context, id)
            return CheckmarkStackWidget(context, id, habitIds)
        }
    }

    private fun getHabitIdsGroupFromWidget(context: Context, widgetId: Int) : List<Long> {
        val app = context.getApplicationContext() as HabitsApplication
        val widgetPrefs = app.component.widgetPreferences
        val habitIds = widgetPrefs.getHabitIdsGroupFromWidgetId(widgetId)
        return habitIds
    }
}
