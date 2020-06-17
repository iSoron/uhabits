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
import androidx.appcompat.app.*
import org.isoron.androidbase.utils.*
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
    var freqNum = 1
    var freqDen = 1

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
            dialog.onFrequencyPicked = {num, den ->
                freqNum = num
                freqDen = den
                populateFrequency()
            }
            dialog.show(supportFragmentManager, "frequencyPicker")
        }

        binding.buttonSave.setOnClickListener {
            finish()
        }
    }

    private fun populateFrequency() {
        val label = when {
            freqNum == 1 && freqDen == 1 -> "Every day"
            freqNum == 1 && freqDen == 7 -> "Every week"
            freqNum == 1 && freqDen > 1 -> "Every $freqDen days"
            freqDen == 7 -> "$freqNum times per week"
            freqDen == 31 -> "$freqNum times per month"
            else -> "Unknown"
        }
        binding.frequencyPicker.text = label
    }

    private fun updateColors() {
        val androidColor = PaletteUtils.getColor(this, paletteColor)
        binding.colorButton.backgroundTintList = ColorStateList.valueOf(androidColor)
        if(!themeSwitcher.isNightMode) {
            val darkerAndroidColor = ColorUtils.mixColors(Color.BLACK, androidColor, 0.15f)
            window.statusBarColor = darkerAndroidColor
            binding.toolbar.setBackgroundColor(androidColor)
        }
    }
}
