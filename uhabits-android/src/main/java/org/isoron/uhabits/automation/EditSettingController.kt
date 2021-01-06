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

package org.isoron.uhabits.automation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Habit

class EditSettingController(private val activity: Activity) {

    fun onSave(habit: Habit, action: Int) {
        if (habit.id == null) return
        val actionName = getActionName(action)
        val blurb = String.format("%s: %s", actionName, habit.name)

        val bundle = Bundle()
        bundle.putInt("action", action)
        bundle.putLong("habit", habit.id!!)

        activity.setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(EXTRA_STRING_BLURB, blurb)
                putExtra(EXTRA_BUNDLE, bundle)
            }
        )
        activity.finish()
    }

    private fun getActionName(action: Int): String {
        return when (action) {
            ACTION_CHECK -> activity.getString(R.string.check)
            ACTION_UNCHECK -> activity.getString(R.string.uncheck)
            ACTION_TOGGLE -> activity.getString(R.string.toggle)
            ACTION_INCREMENT -> activity.getString(R.string.increment)
            ACTION_DECREMENT -> activity.getString(R.string.decrement)
            else -> "???"
        }
    }
}
