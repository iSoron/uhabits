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

package org.isoron.uhabits.activities.habits.list.views

import android.content.*
import android.view.*
import android.view.Gravity.*
import android.view.ViewGroup.LayoutParams.*
import android.widget.*
import org.isoron.androidbase.activities.*
import org.isoron.uhabits.*
import org.isoron.uhabits.utils.*

class EmptyListView(context: Context) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        visibility = BaseRootView.GONE

        addView(TextView(context).apply {
            text = str(R.string.fa_star_half_o)
            typeface = getFontAwesome()
            textSize = sp(40.0f)
            gravity = CENTER
            setTextColor(sres.getColor(R.attr.mediumContrastTextColor))
        }, MATCH_PARENT, WRAP_CONTENT)

        addView(TextView(context).apply {
            text = str(R.string.no_habits_found)
            gravity = CENTER
            setPadding(0, dp(20.0f).toInt(), 0, 0)
            setTextColor(sres.getColor(R.attr.mediumContrastTextColor))
        }, MATCH_PARENT, WRAP_CONTENT)
    }
}