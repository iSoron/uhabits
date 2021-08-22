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

import android.content.Context
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.inject.ActivityScope
import org.isoron.uhabits.utils.StyledResources
import javax.inject.Inject

@ActivityScope
class ColorPickerDialogFactory @Inject constructor(@param:ActivityContext private val context: Context) {
    fun create(color: PaletteColor, theme: Theme): ColorPickerDialog {
        val dialog = ColorPickerDialog()
        val res = StyledResources(context)
        val androidColor = theme.color(color).toInt()
        dialog.initialize(
            R.string.color_picker_default_title,
            res.getPalette(),
            androidColor,
            4,
            com.android.colorpicker.ColorPickerDialog.SIZE_SMALL
        )
        return dialog
    }
}
