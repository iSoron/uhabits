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
package org.isoron.uhabits.core.models

import javax.annotation.concurrent.ThreadSafe
import kotlin.math.min

@ThreadSafe
class StreakList {
    private val list = ArrayList<Streak>()

    @Synchronized
    fun getBest(limit: Int): List<Streak> {
        list.sortWith { s1: Streak, s2: Streak -> s2.compareLonger(s1) }
        return list.subList(0, min(list.size, limit)).apply {
            sortWith { s1: Streak, s2: Streak -> s2.compareNewer(s1) }
        }.toList()
    }

    @Synchronized
    fun recompute(
        computedEntries: EntryList,
        from: Timestamp,
        to: Timestamp,
    ) {
        list.clear()
        val timestamps = computedEntries
            .getByInterval(from, to)
            .filter { it.value > 0 }
            .map { it.timestamp }
            .toTypedArray()

        if (timestamps.isEmpty()) return

        var begin = timestamps[0]
        var end = timestamps[0]
        for (i in 1 until timestamps.size) {
            val current = timestamps[i]
            if (current == begin.minus(1)) {
                begin = current
            } else {
                list.add(Streak(begin, end))
                begin = current
                end = current
            }
        }
        list.add(Streak(begin, end))
    }
}
