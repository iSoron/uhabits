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

import android.content.Intent
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList

object SettingUtils {
    @JvmStatic
    fun parseIntent(intent: Intent, allHabits: HabitList): Arguments? {
        val bundle = intent.getBundleExtra(EXTRA_BUNDLE) ?: return null
        val action = bundle.getInt("action")
        if (action < 0 || action > 4) return null
        val habit = allHabits.getById(bundle.getLong("habit")) ?: return null
        return Arguments(action, habit)
    }

    class Arguments(var action: Int, var habit: Habit)
}
