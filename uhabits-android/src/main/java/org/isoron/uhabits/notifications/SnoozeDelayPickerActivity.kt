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
package org.isoron.uhabits.notifications

import android.app.AlertDialog
import android.content.ContentUris
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.FragmentActivity
import com.android.datetimepicker.time.RadialPickerLayout
import com.android.datetimepicker.time.TimePickerDialog
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.ui.ThemeSwitcher.Companion.THEME_LIGHT
import org.isoron.uhabits.receivers.ReminderController
import org.isoron.uhabits.utils.SystemUtils
import java.util.Calendar

class SnoozeDelayPickerActivity : FragmentActivity(), OnItemClickListener {
    private var habit: Habit? = null
    private var reminderController: ReminderController? = null
    private var dialog: AlertDialog? = null
    private var androidColor: Int = 0

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        val intent = intent
        if (intent == null) finish()
        val app = applicationContext as HabitsApplication
        val appComponent = app.component
        val themeSwitcher = AndroidThemeSwitcher(this, appComponent.preferences)
        if (themeSwitcher.getSystemTheme() == THEME_LIGHT) {
            setTheme(R.style.BaseDialog)
        } else {
            setTheme(R.style.BaseDialogDark)
        }
        val data = intent.data
        if (data == null) {
            finish()
        } else {
            habit = appComponent.habitList.getById(ContentUris.parseId(data))
        }
        if (habit == null) finish()
        androidColor = themeSwitcher.currentTheme.color(habit!!.color).toInt()
        reminderController = appComponent.reminderController
        dialog = AlertDialog.Builder(this)
            .setTitle(R.string.select_snooze_delay)
            .setItems(R.array.snooze_picker_names, null)
            .create()
        dialog!!.listView.onItemClickListener = this
        dialog!!.setOnDismissListener { finish() }
        dialog!!.show()
        SystemUtils.unlockScreen(this)
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val dialog = TimePickerDialog.newInstance(
            { view: RadialPickerLayout?, hour: Int, minute: Int ->
                reminderController!!.onSnoozeTimePicked(habit, hour, minute)
                finish()
            },
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            DateFormat.is24HourFormat(this),
            androidColor
        )
        dialog.show(supportFragmentManager, "timePicker")
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val snoozeValues = resources.getIntArray(R.array.snooze_picker_values)
        if (snoozeValues[position] >= 0) {
            reminderController!!.onSnoozeDelayPicked(habit!!, snoozeValues[position])
            finish()
        } else showTimePicker()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
