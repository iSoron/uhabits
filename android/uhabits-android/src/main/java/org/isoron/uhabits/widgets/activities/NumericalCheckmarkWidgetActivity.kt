/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.widgets.activities

import android.app.*
import android.content.*
import android.os.*
import android.view.*
import android.widget.FrameLayout
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.*
import org.isoron.uhabits.activities.common.dialogs.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import org.isoron.uhabits.core.ui.widgets.*
import org.isoron.uhabits.intents.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.widgets.*

class NumericalCheckmarkWidgetActivity : Activity(), ListHabitsBehavior.NumberPickerCallback {

    private lateinit var behavior: WidgetBehavior
    private lateinit var data: IntentParser.CheckmarkIntentData
    private lateinit var widgetUpdater: WidgetUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(FrameLayout(this))
        val app = this.applicationContext as HabitsApplication
        val component = app.component
        val parser = app.component.intentParser
        data = parser.parseCheckmarkIntent(intent)
        behavior = WidgetBehavior(component.habitList,
                                  component.commandRunner,
                                  component.notificationTray,
                                  component.preferences)
        widgetUpdater = component.widgetUpdater
        showNumberSelector(this)

        SystemUtils.unlockScreen(this)
    }

    override fun onNumberPicked(newValue: Double) {
        behavior.setNumericValue(data.habit, data.timestamp, (newValue * 1000).toInt())
        widgetUpdater.updateWidgets()
        finish()
    }

    override fun onNumberPickerDismissed() {
        finish()
    }

    private fun showNumberSelector(context: Context) {
        val app = this.applicationContext as HabitsApplication
        AndroidThemeSwitcher(this, app.component.preferences).apply()
        val numberPickerFactory = NumberPickerFactory(context)
        numberPickerFactory.create(data.habit.checkmarks.today!!.value.toDouble() / 1000,
                data.habit.unit,
                this).show()
    }

    companion object {
        const val ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY = "org.isoron.uhabits.ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY"
    }
}