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
 * A Score is a number which indicates how strong the habit is at a given date.
 *
 * Scores are computed by taking an exponential moving average of the values of
 * the checkmarks in preceding days. For boolean habits, when computing the
 * average, each checked day (whether the check was manual or automatic) has
 * value as 1, while days without checkmarks have value 0.
 *
 * For numerical habits, each day that exceeded the target has value 1, while
 * days which failed to exceed the target receive a partial value, based on the
 * proportion that was completed. For example, if the target is 100 units and
 * the user completed 70 units, then the value for that day is 0.7 when
 * computing the average.
 */
data class Score(val date: LocalDate,
                 val value: Double)