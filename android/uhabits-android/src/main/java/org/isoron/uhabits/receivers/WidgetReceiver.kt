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

package org.isoron.uhabits.receivers

import android.content.*
import android.util.*

import org.isoron.uhabits.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.ui.widgets.*
import org.isoron.uhabits.intents.*
import org.isoron.uhabits.sync.*

import dagger.*
import org.isoron.uhabits.activities.common.dialogs.NumberPickerFactory
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.widgets.activities.NumericalCheckmarkWidgetActivity
import android.content.Intent



/**
 * The Android BroadcastReceiver for Loop Habit Tracker.
 *
 *
 * All broadcast messages are received and processed by this class.
 */
class WidgetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HabitsApplication

        val component = DaggerWidgetReceiver_WidgetComponent
                .builder()
                .habitsApplicationComponent(app.component)
                .build()

        val parser = app.component.intentParser
        val controller = component.widgetController
        val prefs = app.component.preferences

        if (prefs.isSyncEnabled)
            context.startService(Intent(context, SyncService::class.java))

        try {
            val data: IntentParser.CheckmarkIntentData = parser.parseCheckmarkIntent(intent)

            when (intent.action) {
                ACTION_ADD_REPETITION -> controller.onAddRepetition(data.habit, data.timestamp)

                ACTION_TOGGLE_REPETITION -> controller.onToggleRepetition(data.habit, data.timestamp)

                ACTION_REMOVE_REPETITION -> controller.onRemoveRepetition(data.habit, data.timestamp)

                ACTION_SET_NUMERICAL_VALUE -> {
                    val numberSelectorIntent = Intent(context, NumericalCheckmarkWidgetActivity::class.java)
                    numberSelectorIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    numberSelectorIntent.action = NumericalCheckmarkWidgetActivity.ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY
                    parser.copyIntentData(intent,numberSelectorIntent)//give the habit and timestamp data to the numericalCheckmarkWidgetActivity
                    context.startActivity(numberSelectorIntent)
                }
            }
        } catch (e: RuntimeException) {
            Log.e("WidgetReceiver", "could not process intent", e)
        }

    }




    @ReceiverScope
    @Component(dependencies = [HabitsApplicationComponent::class])
    internal interface WidgetComponent {
        val widgetController: WidgetBehavior
    }

    companion object {
        val ACTION_ADD_REPETITION = "org.isoron.uhabits.ACTION_ADD_REPETITION"

        val ACTION_DISMISS_REMINDER = "org.isoron.uhabits.ACTION_DISMISS_REMINDER"

        val ACTION_REMOVE_REPETITION = "org.isoron.uhabits.ACTION_REMOVE_REPETITION"

        val ACTION_TOGGLE_REPETITION = "org.isoron.uhabits.ACTION_TOGGLE_REPETITION"

        val ACTION_SET_NUMERICAL_VALUE = "org.isoron.uhabits.ACTION_SET_NUMERICAL_VALUE"
    }
}
