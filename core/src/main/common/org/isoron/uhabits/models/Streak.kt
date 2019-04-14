/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.models

import org.isoron.platform.time.*

/**
 * A streak is an uninterrupted sequence of days where the habit was performed.
 *
 * For daily boolean habits, the definition is straightforward: a streak is a
 * sequence of days that have checkmarks. For non-daily habits, note
 * that automatic checkmarks (the ones added by the app) can also keep the
 * streak going. For numerical habits, a streak is a sequence of days where the
 * user has consistently exceeded the target for the habit.
 */
data class Streak(val start: LocalDate,
                  val end: LocalDate)