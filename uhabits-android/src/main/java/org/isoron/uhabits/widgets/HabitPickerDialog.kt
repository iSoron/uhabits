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

import android.app.*
import android.appwidget.AppWidgetManager.*
import android.content.*
import android.os.*
import android.view.*
import android.widget.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.preferences.*
import java.util.*

class HabitPickerDialog : Activity(), AdapterView.OnItemClickListener {

    private var widgetId = 0
    private lateinit var habitList: HabitList
    private lateinit var preferences: WidgetPreferences
    private lateinit var habitIds: ArrayList<Long>
    private lateinit var widgetUpdater: WidgetUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (applicationContext as HabitsApplication).component
        habitList = component.habitList
        preferences = component.widgetPreferences
        widgetUpdater = component.widgetUpdater
        widgetId = intent.extras?.getInt(EXTRA_APPWIDGET_ID,
                                         INVALID_APPWIDGET_ID) ?: 0

        habitIds = ArrayList<Long>()
        val habitNames = ArrayList<String>()
        for (h in habitList) {
            if (h.isArchived) continue
            habitIds.add(h.getId()!!)
            habitNames.add(h.name)
        }

        setContentView(R.layout.widget_configure_activity)
        with(findViewById(R.id.listView) as ListView) {
            adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1,
                                   habitNames)
            onItemClickListener = this@HabitPickerDialog
        }
    }

    override fun onItemClick(parent: AdapterView<*>,
                             view: View,
                             position: Int,
                             id: Long) {
        preferences.addWidget(widgetId, habitIds[position])
        widgetUpdater.updateWidgets()
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_APPWIDGET_ID, widgetId)
        })
        finish()
    }
}
