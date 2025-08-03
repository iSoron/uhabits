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

package org.isoron.uhabits.activities.common.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatDialogFragment
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.databinding.CheckmarkPopupBinding
import org.isoron.uhabits.utils.InterfaceUtils.getFontAwesome
import org.isoron.uhabits.utils.sres

class CheckmarkDialog : AppCompatDialogFragment() {
    var onToggle: (Int, String) -> Unit = { _, _ -> }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val appComponent = (requireActivity().application as HabitsApplication).component
        val prefs = appComponent.preferences
        val view = CheckmarkPopupBinding.inflate(LayoutInflater.from(context))
        val color = requireArguments().getInt("color")

        // Get the habit ID and load description
        val habitId = requireArguments().getLong("habitId", -1)
        if (habitId != -1L) {
            val habit = appComponent.habitList.getById(habitId)
            habit?.let {
                val description = it.question.trim()
                if (description.isNotEmpty()) {
                    view.habitDescription.text = description
                    view.habitDescription.visibility = VISIBLE
                } else {
                    view.habitDescription.visibility = GONE
                }
            }
        } else {
            view.habitDescription.visibility = GONE
        }

        arrayOf(view.yesBtn, view.skipBtn).forEach {
            it.setTextColor(color)
        }
        arrayOf(view.noBtn, view.unknownBtn).forEach {
            it.setTextColor(view.root.sres.getColor(R.attr.contrast60))
        }
        arrayOf(view.yesBtn, view.noBtn, view.skipBtn, view.unknownBtn).forEach {
            it.typeface = getFontAwesome(requireContext())
        }
        view.notes.setText(requireArguments().getString("notes")!!)
        if (!prefs.isSkipEnabled) view.skipBtn.visibility = GONE
        if (!prefs.areQuestionMarksEnabled) view.unknownBtn.visibility = GONE
        view.booleanButtons.visibility = VISIBLE
        val dialog = Dialog(requireContext())
        dialog.setContentView(view.root)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        fun onClick(v: Int) {
            val notes = view.notes.text.toString().trim()
            onToggle(v, notes)
            requireDialog().dismiss()
        }
        view.yesBtn.setOnClickListener { onClick(YES_MANUAL) }
        view.noBtn.setOnClickListener { onClick(NO) }
        view.skipBtn.setOnClickListener { onClick(SKIP) }
        view.unknownBtn.setOnClickListener { onClick(UNKNOWN) }
        view.notes.setOnEditorActionListener { v, actionId, event ->
            onClick(requireArguments().getInt("value"))
            true
        }

        return dialog
    }
}
