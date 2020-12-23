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

package org.isoron.uhabits.activities.common.dialogs;

import android.content.*;
import android.content.res.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;

import com.google.auto.factory.*;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.core.ui.callbacks.*;

@AutoFactory(allowSubclasses = true)
public class ConfirmSyncKeyDialog extends AlertDialog
{
    protected ConfirmSyncKeyDialog(@Provided @ActivityContext Context context,
                                   @NonNull OnConfirmedCallback callback)
    {
        super(context);
        setTitle(R.string.device_sync);
        Resources res = context.getResources();
        setMessage(res.getString(R.string.sync_confirm));
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
