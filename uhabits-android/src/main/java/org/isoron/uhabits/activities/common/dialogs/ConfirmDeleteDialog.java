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

package org.isoron.uhabits.activities.common.dialogs;

import android.content.*;
import android.content.res.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.core.ui.callbacks.*;
import org.isoron.uhabits.inject.*;

/**
 * Dialog that asks the user confirmation before executing a delete operation.
 */
public class ConfirmDeleteDialog extends AlertDialog
{
    public ConfirmDeleteDialog(@ActivityContext Context context,
                               @NonNull OnConfirmedCallback callback,
                               int quantity)
    {
        super(context);
        Resources res = context.getResources();
        setTitle(res.getQuantityString(R.plurals.delete_habits_title, quantity));
        setMessage(res.getQuantityString(R.plurals.delete_habits_message, quantity));
        setButton(BUTTON_POSITIVE,
                res.getString(R.string.yes),
                (dialog, which) -> callback.onConfirmed()
        );
        setButton(BUTTON_NEGATIVE,
                res.getString(R.string.no),
                (dialog, which) -> { }
        );
    }
}
