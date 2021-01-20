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
package org.isoron.uhabits.core.ui.screens.habits.list

import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday

/**
 * Provides a list of hints to be shown at the application startup, and takes
 * care of deciding when a new hint should be shown.
 */
open class HintList(private val prefs: Preferences, private val hints: Array<String>) {
    /**
     * Returns a new hint to be shown to the user.
     *
     *
     * The hint returned is marked as read on the list, and will not be returned
     * again. In case all hints have already been read, and there is nothing
     * left, returns null.
     *
     * @return the next hint to be shown, or null if none
     */
    open fun pop(): String? {
        val next = prefs.lastHintNumber + 1
        if (next >= hints.size) return null
        prefs.updateLastHint(next, getToday())
        return hints[next]
    }

    /**
     * Returns whether it is time to show a new hint or not.
     *
     * @return true if hint should be shown, false otherwise
     */
    open fun shouldShow(): Boolean {
        val today = getToday()
        val lastHintTimestamp = prefs.lastHintTimestamp
        return lastHintTimestamp?.isOlderThan(today) == true
    }
}
