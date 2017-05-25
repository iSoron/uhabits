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
import android.support.v7.app.*;

import com.google.auto.factory.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.*;

import butterknife.*;

/**
 * Dialog that asks the user confirmation before executing a delete operation.
 */
@AutoFactory(allowSubclasses = true)
public class ConfirmDeleteDialog extends AlertDialog
{
    @BindString(R.string.delete_habits_message)
    protected String question;

    @BindString(android.R.string.yes)
    protected String yes;

    @BindString(android.R.string.no)
    protected String no;

    protected ConfirmDeleteDialog(@Provided @ActivityContext Context context,
                                  Callback callback)
    {
        super(context);
        ButterKnife.bind(this);

        setTitle(R.string.delete_habits);
        setMessage(question);
        setButton(BUTTON_POSITIVE, yes, (dialog, which) -> callback.run());
        setButton(BUTTON_NEGATIVE, no, (dialog, which) -> {});
    }

    public interface Callback
    {
        void run();
    }
}
