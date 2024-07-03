package org.isoron.uhabits.activities.habits.edit

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.format.DateFormat
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.android.datetimepicker.time.RadialPickerLayout
import com.android.datetimepicker.time.TimePickerDialog
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.activities.common.dialogs.ColorPickerDialogFactory
import org.isoron.uhabits.activities.common.dialogs.WeekdayPickerDialog
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateHabitGroupCommand
import org.isoron.uhabits.core.commands.EditHabitGroupCommand
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.databinding.ActivityEditHabitGroupBinding
import org.isoron.uhabits.utils.ColorUtils
import org.isoron.uhabits.utils.dismissCurrentAndShow
import org.isoron.uhabits.utils.formatTime
import org.isoron.uhabits.utils.toFormattedString

class EditHabitGroupActivity : AppCompatActivity() {

    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private lateinit var binding: ActivityEditHabitGroupBinding
    private lateinit var commandRunner: CommandRunner

    var habitGroupId = -1L
    var color = PaletteColor(11)
    var androidColor = 0
    var reminderHour = -1
    var reminderMin = -1
    var reminderDays: WeekdayList = WeekdayList.EVERY_DAY

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val component = (application as HabitsApplication).component
        themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
        themeSwitcher.apply()

        binding = ActivityEditHabitGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("habitGroupId")) {
            binding.toolbar.title = getString(R.string.edit_habit_group)
            habitGroupId = intent.getLongExtra("habitGroupId", -1)
            val hgr = component.habitGroupList.getById(habitGroupId)!!
            color = hgr.color
            hgr.reminder?.let {
                reminderHour = it.hour
                reminderMin = it.minute
                reminderDays = it.days
            }
            binding.nameInput.setText(hgr.name)
            binding.questionInput.setText(hgr.question)
            binding.notesInput.setText(hgr.description)
        }

        if (state != null) {
            habitGroupId = state.getLong("habitGroupId")
            color = PaletteColor(state.getInt("paletteColor"))
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
            val picker = colorPickerDialogFactory.create(color, themeSwitcher.currentTheme)
            picker.setListener { paletteColor ->
                this.color = paletteColor
                updateColors()
            }
            picker.dismissCurrentAndShow(supportFragmentManager, "colorPicker")
        }

        populateReminder()
        binding.reminderTimePicker.setOnClickListener {
            val currentHour = if (reminderHour >= 0) reminderHour else 8
            val currentMin = if (reminderMin >= 0) reminderMin else 0
            val is24HourMode = DateFormat.is24HourFormat(this)
            val dialog = TimePickerDialog.newInstance(
                object : TimePickerDialog.OnTimeSetListener {
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
                },
                currentHour,
                currentMin,
                is24HourMode,
                androidColor
            )
            dialog.dismissCurrentAndShow(supportFragmentManager, "timePicker")
        }

        binding.reminderDatePicker.setOnClickListener {
            val dialog = WeekdayPickerDialog()

            dialog.setListener { days: WeekdayList ->
                reminderDays = days
                if (reminderDays.isEmpty) reminderDays = WeekdayList.EVERY_DAY
                populateReminder()
            }
            dialog.setSelectedDays(reminderDays)
            dialog.dismissCurrentAndShow(supportFragmentManager, "dayPicker")
        }

        binding.buttonSave.setOnClickListener {
            if (validate()) save()
        }

        for (fragment in supportFragmentManager.fragments) {
            (fragment as DialogFragment).dismiss()
        }
    }

    private fun save() {
        val component = (application as HabitsApplication).component
        val hgr = component.modelFactory.buildHabitGroup()

        var original: HabitGroup? = null
        if (habitGroupId >= 0) {
            original = component.habitGroupList.getById(habitGroupId)!!
            hgr.copyFrom(original)
        }

        hgr.name = binding.nameInput.text.trim().toString()
        hgr.question = binding.questionInput.text.trim().toString()
        hgr.description = binding.notesInput.text.trim().toString()
        hgr.color = color
        if (reminderHour >= 0) {
            hgr.reminder = Reminder(reminderHour, reminderMin, reminderDays)
        } else {
            hgr.reminder = null
        }

        val command = if (habitGroupId >= 0) {
            EditHabitGroupCommand(
                component.habitGroupList,
                habitGroupId,
                hgr
            )
        } else {
            CreateHabitGroupCommand(
                component.modelFactory,
                component.habitGroupList,
                hgr
            )
        }
        component.commandRunner.run(command)
        finish()
    }

    private fun validate(): Boolean {
        var isValid = true
        if (binding.nameInput.text.isEmpty()) {
            binding.nameInput.error = getFormattedValidationError(R.string.validation_cannot_be_blank)
            isValid = false
        }
        return isValid
    }

    private fun populateReminder() {
        if (reminderHour < 0) {
            binding.reminderTimePicker.text = getString(R.string.reminder_off)
            binding.reminderDatePicker.visibility = View.GONE
            binding.reminderDivider.visibility = View.GONE
        } else {
            val time = formatTime(this, reminderHour, reminderMin)
            binding.reminderTimePicker.text = time
            binding.reminderDatePicker.visibility = View.VISIBLE
            binding.reminderDivider.visibility = View.VISIBLE
            binding.reminderDatePicker.text = reminderDays.toFormattedString(this)
        }
    }

    private fun updateColors() {
        androidColor = themeSwitcher.currentTheme.color(color).toInt()
        binding.colorButton.backgroundTintList = ColorStateList.valueOf(androidColor)
        if (!themeSwitcher.isNightMode) {
            val darkerAndroidColor = ColorUtils.mixColors(Color.BLACK, androidColor, 0.15f)
            window.statusBarColor = darkerAndroidColor
            binding.toolbar.setBackgroundColor(androidColor)
        }
    }

    private fun getFormattedValidationError(@StringRes resId: Int): Spanned {
        val html = "<font color=#FFFFFF>${getString(resId)}</font>"
        return Html.fromHtml(html)
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        with(state) {
            putLong("habitGroupId", habitGroupId)
            putInt("paletteColor", color.paletteIndex)
            putInt("androidColor", androidColor)
            putInt("reminderHour", reminderHour)
            putInt("reminderMin", reminderMin)
            putInt("reminderDays", reminderDays.toInteger())
        }
    }
}
