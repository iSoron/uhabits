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
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import org.isoron.uhabits.R
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import org.isoron.uhabits.inject.ActivityContext

/**
 * Dialog that asks the user confirmation before executing a delete operation.
 */
class ConfirmDeleteDialog(
    @ActivityContext context: Context,
    callback: OnConfirmedCallback,
    quantity: Int
) : AlertDialog(context) {
    init {
        val res = context.resources
        setTitle(res.getQuantityString(R.plurals.delete_habits_title, quantity))
        setMessage(res.getQuantityString(R.plurals.delete_habits_message, quantity))
        setButton(
            BUTTON_POSITIVE,
            res.getString(R.string.yes)
        ) { dialog: DialogInterface?, which: Int -> callback.onConfirmed() }
        setButton(
            BUTTON_NEGATIVE,
            res.getString(R.string.no)
        ) { dialog: DialogInterface?, which: Int -> }
    }
}
