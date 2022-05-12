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

package org.isoron.uhabits.widgets.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import org.isoron.platform.gui.ScreenLocation
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.activities.common.dialogs.NumberPopup
import org.isoron.uhabits.activities.common.dialogs.POPUP_HEIGHT
import org.isoron.uhabits.activities.common.dialogs.POPUP_WIDTH
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.core.ui.widgets.WidgetBehavior
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.intents.IntentParser
import org.isoron.uhabits.utils.SystemUtils
import org.isoron.uhabits.utils.getCenter
import org.isoron.uhabits.widgets.WidgetUpdater

class NumericalCheckmarkWidgetActivity : Activity(), ListHabitsBehavior.NumberPickerCallback {

    private lateinit var behavior: WidgetBehavior
    private lateinit var data: IntentParser.CheckmarkIntentData
    private lateinit var widgetUpdater: WidgetUpdater
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootView = FrameLayout(this)
        rootView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        setContentView(rootView)
        val app = this.applicationContext as HabitsApplication
        val component = app.component
        val parser = app.component.intentParser
        data = parser.parseCheckmarkIntent(intent)
        behavior = WidgetBehavior(
            component.habitList,
            component.commandRunner,
            component.notificationTray,
            component.preferences
        )
        widgetUpdater = component.widgetUpdater
        rootView.post {
            showNumberSelector(this)
        }
        SystemUtils.unlockScreen(this)
    }

    override fun onNumberPicked(newValue: Double, notes: String) {
        behavior.setValue(data.habit, data.timestamp, (newValue * 1000).toInt(), notes)
        widgetUpdater.updateWidgets()
        finish()
    }

    override fun onNumberPickerDismissed() {
        finish()
    }

    private fun showNumberSelector(context: Context) {
        val app = this.applicationContext as HabitsApplication
        AndroidThemeSwitcher(this, app.component.preferences).apply()
        val today = DateUtils.getTodayWithOffset()
        val entry = data.habit.computedEntries.get(today)
        NumberPopup(
            context = context,
            prefs = app.component.preferences,
            anchor = rootView,
            notes = entry.notes,
            value = entry.value / 1000.0,
        ).apply {
            onToggle = { value, notes ->
                onNumberPicked(value, notes)
                finish()
                overridePendingTransition(0, 0)
            }
            val center = rootView.getCenter()
            show(
                ScreenLocation(
                    x = center.x - POPUP_WIDTH / 2,
                    y = center.y - POPUP_HEIGHT / 2
                )
            )
        }
    }

    companion object {
        const val ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY = "org.isoron.uhabits.ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY"
    }
}
