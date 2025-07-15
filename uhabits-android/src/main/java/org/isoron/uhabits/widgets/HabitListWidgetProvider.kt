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
package org.isoron.uhabits.widgets

import android.content.Context
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitNotFoundException
import java.util.ArrayList

class HabitListWidgetProvider : BaseWidgetProvider() {

    override fun getWidgetFromId(context: Context, id: Int): BaseWidget {
        val habits = getNullableHabitsFromWidgetId(context, id)
        return HabitListWidget(context = context, widgetId = id, habits = habits)
    }

    private fun getNullableHabitsFromWidgetId(context: Context, widgetId: Int): List<Habit> {
        val app = context.applicationContext as HabitsApplication
        val widgetPrefs = app.component.widgetPreferences
        val selectedIds = widgetPrefs.getHabitIdsFromWidgetId(widgetId)
        val habits = app.component.habitList
        val selectedHabits = ArrayList<Habit>(selectedIds.size)
        for (id in selectedIds) {
            val h = habits.getById(id) ?: continue
            selectedHabits.add(h)
        }
        if (selectedHabits.isEmpty()) {
            throw HabitNotFoundException()
        }

        return selectedHabits
    }
}