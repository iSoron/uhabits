/*
 * Copyright (C) 2016-2025 A?linson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.activities.habits.progress

import org.isoron.uhabits.core.models.PaletteColor

object ProgressSectionColors {
    val progress = PaletteColor(11)   // Blue
    val streak = PaletteColor(7)      // Green
    val score = PaletteColor(2)       // Orange
    val completion = PaletteColor(14) // Purple
    val negative = PaletteColor(2)    // Orange (for negative deltas)
    val positive = PaletteColor(7)    // Green (for positive deltas)
}
