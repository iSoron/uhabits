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

package org.isoron.uhabits.activities.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.databinding.SyncActivityBinding
import org.isoron.uhabits.utils.setupToolbar

class SyncActivity : AppCompatActivity() {

    private lateinit var binding: SyncActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as HabitsApplication).component
        val themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
        themeSwitcher.apply()

        binding = SyncActivityBinding.inflate(LayoutInflater.from(this))
        binding.root.setupToolbar(
            toolbar = binding.toolbar,
            title = resources.getString(R.string.device_sync),
            color = PaletteColor(11),
            theme = themeSwitcher.currentTheme,
        )
        binding.generateButton.setOnClickListener { onGenerateCode() }
        binding.enterButton.setOnClickListener {
            val et = EditText(this)
            AlertDialog.Builder(this)
                .setTitle(R.string.sync_code)
                .setView(et)
                .setPositiveButton(R.string.save) { _, _ ->
                    onEnterCode(et.text.toString())
                }
                .show()
        }
        binding.disableButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.disable_sync)
                .setMessage(R.string.disable_sync_description)
                .setPositiveButton(R.string.disable) { _, _ ->
                    onDisableSync()
                }
                .setNegativeButton(R.string.keep_enabled) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        setContentView(binding.root)
    }

    private fun onGenerateCode() {
        showCodeScreen()
    }

    private fun onEnterCode(code: String) {
        showCodeScreen()
    }

    private fun onDisableSync() {
        showIntroScreen()
    }

    private fun showCodeScreen() {
        binding.introGroup.visibility = View.GONE
        binding.codeGroup.visibility = View.VISIBLE
    }

    private fun showIntroScreen() {
        binding.introGroup.visibility = View.VISIBLE
        binding.codeGroup.visibility = View.GONE
    }
}
