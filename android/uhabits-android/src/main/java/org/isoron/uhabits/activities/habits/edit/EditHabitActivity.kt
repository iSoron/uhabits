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
import android.view.*
import androidx.appcompat.app.*
import androidx.fragment.app.*
import com.android.datetimepicker.time.*
import kotlinx.android.synthetic.main.activity_edit_habit.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.*
import org.isoron.uhabits.activities.common.dialogs.*
import org.isoron.uhabits.core.commands.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

class EditHabitActivity : AppCompatActivity() {

    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private lateinit var binding: ActivityEditHabitBinding
    private lateinit var commandRunner: CommandRunner

    var habitId = -1L
    var paletteColor = 11
    var androidColor = 0
    var freqNum = 1
    var freqDen = 1
    var reminderHour = -1
    var reminderMin = -1
    var reminderDays: WeekdayList = WeekdayList.EVERY_DAY

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val component = (application as HabitsApplication).component
        themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
        themeSwitcher.apply()

        binding = ActivityEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("habitId")) {
            binding.toolbar.title = getString(R.string.edit_habit)
            habitId = intent.getLongExtra("habitId", -1)
            val habit = component.habitList.getById(habitId)!!
            paletteColor = habit.color
            freqNum = habit.frequency.numerator
            freqDen = habit.frequency.denominator
            if (habit.hasReminder()) {
                reminderHour = habit.reminder.hour
                reminderMin = habit.reminder.minute
                reminderDays = habit.reminder.days
            }
            binding.nameInput.setText(habit.name)
            binding.questionInput.setText(habit.question)
            binding.notesInput.setText(habit.description)
        }

        if (state != null) {
            habitId = state.getLong("habitId")
            paletteColor = state.getInt("paletteColor")
            freqNum = state.getInt("freqNum")
            freqDen = state.getInt("freqDen")
            reminderHour = state.getInt("reminderHour")
            reminderMin = state.getInt("reminderMin")
            reminderDays = WeekdayList(state.getInt("reminderDays"))
        }

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

        populateReminder()
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
                    reminderDays = WeekdayList.EVERY_DAY
                    populateReminder()
                }
            }, currentHour, currentMin, is24HourMode, androidColor)
            dialog.show(supportFragmentManager, "timePicker")
        }

        binding.reminderDatePicker.setOnClickListener {
            val dialog = WeekdayPickerDialog()
            dialog.setListener { days ->
                reminderDays = days
                if (reminderDays.isEmpty) reminderDays = WeekdayList.EVERY_DAY
                populateReminder()
            }
            dialog.setSelectedDays(reminderDays)
            dialog.show(supportFragmentManager, "dayPicker")
        }

        binding.buttonSave.setOnClickListener {
            if(validate()) save()
        }

        for (fragment in supportFragmentManager.fragments) {
            (fragment as DialogFragment).dismiss()
        }
    }

    private fun save() {
        val component = (application as HabitsApplication).component
        val habit = component.modelFactory.buildHabit()

        var original: Habit? = null
        if (habitId >= 0) {
            original = component.habitList.getById(habitId)!!
            habit.copyFrom(original)
        }

        habit.name = nameInput.text.trim().toString()
        habit.question = questionInput.text.trim().toString()
        habit.description = notesInput.text.trim().toString()
        habit.color = paletteColor
        if (reminderHour >= 0) {
            habit.setReminder(Reminder(reminderHour, reminderMin, reminderDays))
        }
        habit.frequency = Frequency(freqNum, freqDen)
        habit.unit = ""
        habit.targetValue = 1.0
        habit.type = Habit.YES_NO_HABIT

        val command = if (habitId >= 0) {
            component.editHabitCommandFactory.create(component.habitList, original, habit)
        } else {
            component.createHabitCommandFactory.create(component.habitList, habit)
        }
        component.commandRunner.execute(command, null)
        finish()
    }

    private fun validate(): Boolean {
        if (nameInput.text.isEmpty()) {
            nameInput.error = getString(R.string.validation_name_should_not_be_blank)
            return false
        }
        return true
    }

    private fun populateReminder() {
        if (reminderHour < 0) {
            binding.reminderTimePicker.text = getString(R.string.reminder_off)
            binding.reminderDatePicker.visibility = View.GONE
            binding.reminderDivider.visibility = View.GONE
        } else {
            val time = AndroidDateUtils.formatTime(this, reminderHour, reminderMin)
            val daysArray = reminderDays.toArray()
            binding.reminderTimePicker.text = time
            binding.reminderDatePicker.visibility = View.VISIBLE
            binding.reminderDivider.visibility = View.VISIBLE
            binding.reminderDatePicker.text = AndroidDateUtils.formatWeekdayList(this, daysArray)
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

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        with(state) {
            putLong("habitId", habitId)
            putInt("paletteColor", paletteColor)
            putInt("androidColor", androidColor)
            putInt("freqNum", freqNum)
            putInt("freqDen", freqDen)
            putInt("reminderHour", reminderHour)
            putInt("reminderMin", reminderMin)
            putInt("reminderDays", reminderDays.toInteger())
        }
    }
}
