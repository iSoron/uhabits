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
package org.isoron.uhabits.activities.common.views

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.ClassLoaderCreator
import androidx.customview.view.AbsSavedState

class BundleSavedState : AbsSavedState {
    @JvmField val bundle: Bundle?

    constructor(superState: Parcelable?, bundle: Bundle?) : super(superState!!) {
        this.bundle = bundle
    }

    constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
        bundle = source.readBundle(loader)
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeBundle(bundle)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<BundleSavedState> =
            object : ClassLoaderCreator<BundleSavedState> {
                override fun createFromParcel(
                    source: Parcel,
                    loader: ClassLoader
                ): BundleSavedState {
                    return BundleSavedState(source, loader)
                }

                override fun createFromParcel(source: Parcel): BundleSavedState? {
                    return null
                }

                override fun newArray(size: Int): Array<BundleSavedState?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
