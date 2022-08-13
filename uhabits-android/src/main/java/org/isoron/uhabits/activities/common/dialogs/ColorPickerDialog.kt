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
import android.content.DialogInterface
import android.os.Bundle
import com.android.colorpicker.ColorPickerDialog
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.core.ui.callbacks.OnColorPickedCallback
import org.isoron.uhabits.utils.toPaletteColor

/**
 * Dialog that allows the user to choose a color.
 */
class ColorPickerDialog : ColorPickerDialog() {
    fun setListener(callback: OnColorPickedCallback) {
        super.setOnColorSelectedListener { c: Int ->
            val pc = c.toPaletteColor(requireContext())
            callback.onColorPicked(pc)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        HabitsApplication.clearCurrentDialog()
        HabitsApplication.currentDialog = this.dialog
        return super.onCreateDialog(savedInstanceState)
    }
    override fun onColorSelected(color: Int) {
        super.onColorSelected(color)
    }

    override fun onDismiss(dialog: DialogInterface) {
        HabitsApplication.currentDialog = null
        super.onDismiss(dialog)
    }
}
