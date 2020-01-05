/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.activities.common.views;

import android.os.*;
import androidx.core.os.*;
import androidx.customview.view.AbsSavedState;

public class BundleSavedState extends AbsSavedState
{
    public static final Parcelable.Creator<BundleSavedState> CREATOR =
        ParcelableCompat.newCreator(
            new ParcelableCompatCreatorCallbacks<BundleSavedState>()
            {
                @Override
                public BundleSavedState createFromParcel(Parcel source,
                                                         ClassLoader loader)
                {
                    return new BundleSavedState(source, loader);
                }

                @Override
                public BundleSavedState[] newArray(int size)
                {
                    return new BundleSavedState[size];
                }
            });

    public final Bundle bundle;

    public BundleSavedState(Parcelable superState, Bundle bundle)
    {
        super(superState);
        this.bundle = bundle;
    }

    public BundleSavedState(Parcel source, ClassLoader loader)
    {
        super(source, loader);
        this.bundle = source.readBundle(loader);
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        super.writeToParcel(out, flags);
        out.writeBundle(bundle);
    }
}