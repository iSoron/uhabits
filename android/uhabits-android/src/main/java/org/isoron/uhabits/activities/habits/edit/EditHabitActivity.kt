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

package org.isoron.uhabits.activities.habits.edit

import android.content.res.*
import android.graphics.*
import android.os.*
import android.text.format.*
import androidx.appcompat.app.*
import com.android.datetimepicker.time.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.*
import org.isoron.uhabits.activities.common.dialogs.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.preferences.*
import org.isoron.uhabits.utils.*

class EditHabitActivity : AppCompatActivity() {

    private lateinit var themeSwitcher: AndroidThemeSwitcher

    private lateinit var binding: ActivityEditHabitBinding

    var paletteColor = 11
    var androidColor = 0

    var freqNum = 1
    var freqDen = 1
    var reminderHour = -1
    var reminderMin = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Preferences(SharedPreferencesStorage(this))
        themeSwitcher = AndroidThemeSwitcher(this, prefs)
        themeSwitcher.apply()

        binding = ActivityEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateColors()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.elevation = 10.0f

        val colorPickerDialogFactory = ColorPickerDialogFactory(this)
        binding.colorButton.setOnClickListener {
            val dialog = colorPickerDialogFactory.create(paletteColor)
            dialog.setListener { paletteColor ->
                this.paletteColor = paletteColor
                updateColors()
            }
            dialog.show(supportFragmentManager, "colorPicker")
        }

        populateFrequency()
        binding.frequencyPicker.setOnClickListener {
            val dialog = FrequencyPickerDialog(freqNum, freqDen)
            dialog.onFrequencyPicked = { num, den ->
                freqNum = num
                freqDen = den
                populateFrequency()
            }
            dialog.show(supportFragmentManager, "frequencyPicker")
        }

        binding.reminderTimePicker.setOnClickListener {
            val currentHour = if (reminderHour >= 0) reminderHour else 8
            val currentMin = if (reminderMin >= 0) reminderMin else 0
            val is24HourMode = DateFormat.is24HourFormat(this)
            val dialog = TimePickerDialog.newInstance(object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: RadialPickerLayout?, hourOfDay: Int, minute: Int) {
                    reminderHour = hourOfDay
                    reminderMin = minute
                    populateReminder()
                }
                override fun onTimeCleared(view: RadialPickerLayout?) {
                    reminderHour = -1
                    reminderMin = -1
                    populateReminder()
                }
            }, currentHour, currentMin, is24HourMode, androidColor)
            dialog.show(supportFragmentManager, "timePicker")
        }

        binding.buttonSave.setOnClickListener {
            finish()
        }
    }

    private fun populateReminder() {
        if (reminderHour < 0) {
            binding.reminderTimePicker.text = getString(R.string.reminder_off)
        } else {
            val time = AndroidDateUtils.formatTime(this, reminderHour, reminderMin)
            binding.reminderTimePicker.text = time
        }
    }

    private fun populateFrequency() {
        val label = when {
            freqNum == 1 && freqDen == 1 -> getString(R.string.every_day)
            freqNum == 1 && freqDen == 7 -> getString(R.string.every_week)
            freqNum == 1 && freqDen > 1 -> getString(R.string.every_x_days, freqDen)
            freqDen == 7 -> getString(R.string.x_times_per_week, freqNum)
            freqDen == 31 -> getString(R.string.x_times_per_month, freqNum)
            else -> "Unknown"
        }
        binding.frequencyPicker.text = label
    }

    private fun updateColors() {
        androidColor = PaletteUtils.getColor(this, paletteColor)
        binding.colorButton.backgroundTintList = ColorStateList.valueOf(androidColor)
        if (!themeSwitcher.isNightMode) {
            val darkerAndroidColor = ColorUtils.mixColors(Color.BLACK, androidColor, 0.15f)
            window.statusBarColor = darkerAndroidColor
            binding.toolbar.setBackgroundColor(androidColor)
        }
    }
}
