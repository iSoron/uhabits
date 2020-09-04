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

import android.appwidget.*
import android.content.*
import org.isoron.androidbase.*
import org.isoron.uhabits.core.commands.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.tasks.*
import org.isoron.uhabits.core.utils.*
import org.isoron.uhabits.intents.*
import javax.inject.*

/**
 * A WidgetUpdater listens to the commands being executed by the application and
 * updates the home-screen widgets accordingly.
 */
class WidgetUpdater
@Inject constructor(
        @AppContext private val context: Context,
        private val commandRunner: CommandRunner,
        private val taskRunner: TaskRunner,
        private val widgetPrefs: WidgetPreferences,
        private val intentScheduler: IntentScheduler
) : CommandRunner.Listener {

    override fun onCommandExecuted(command: Command, refreshKey: Long?) {
        updateWidgets(refreshKey)
    }

    /**
     * Instructs the updater to start listening to commands. If any relevant
     * commands are executed after this method is called, the corresponding
     * widgets will get updated.
     */
    fun startListening() {
        commandRunner.addListener(this)
    }

    /**
     * Instructs the updater to stop listening to commands. Every command
     * executed after this method is called will be ignored by the updater.
     */
    fun stopListening() {
        commandRunner.removeListener(this)
    }

    fun scheduleStartDayWidgetUpdate() {
        val timestamp = DateUtils.getTomorrowStart()
        intentScheduler.scheduleWidgetUpdate(timestamp);
    }

    fun updateWidgets(modifiedHabitId: Long?) {
        taskRunner.execute {
            updateWidgets(modifiedHabitId, CheckmarkWidgetProvider::class.java)
            updateWidgets(modifiedHabitId, HistoryWidgetProvider::class.java)
            updateWidgets(modifiedHabitId, ScoreWidgetProvider::class.java)
            updateWidgets(modifiedHabitId, StreakWidgetProvider::class.java)
            updateWidgets(modifiedHabitId, FrequencyWidgetProvider::class.java)
            updateWidgets(modifiedHabitId, TargetWidgetProvider::class.java)
        }
    }

    private fun updateWidgets(modifiedHabitId: Long?, providerClass: Class<*>) {
        val widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, providerClass))

        val modifiedWidgetIds = when (modifiedHabitId) {
            null -> widgetIds.toList()
            else -> widgetIds.filter { w ->
                widgetPrefs.getHabitIdsFromWidgetId(w).contains(modifiedHabitId)
            }
        }

        context.sendBroadcast(Intent(context, providerClass).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, modifiedWidgetIds.toIntArray())
        })
    }

    fun updateWidgets() {
        updateWidgets(null)
    }
}
