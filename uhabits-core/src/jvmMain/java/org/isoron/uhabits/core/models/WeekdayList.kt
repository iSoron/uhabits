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

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.util.Arrays

class WeekdayList {
    private val weekdays: BooleanArray

    constructor(packedList: Int) {
        weekdays = BooleanArray(7)
        var current = 1
        for (i in 0..6) {
            if (packedList and current != 0) weekdays[i] = true
            current = current shl 1
        }
    }

    constructor(weekdays: BooleanArray?) {
        this.weekdays = Arrays.copyOf(weekdays, 7)
    }

    val isEmpty: Boolean
        get() {
            for (d in weekdays) if (d) return false
            return true
        }

    fun toArray(): BooleanArray {
        return weekdays.copyOf(7)
    }

    fun toInteger(): Int {
        var packedList = 0
        var current = 1
        for (i in 0..6) {
            if (weekdays[i]) packedList = packedList or current
            current = current shl 1
        }
        return packedList
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as WeekdayList
        return EqualsBuilder().append(weekdays, that.weekdays).isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37).append(weekdays).toHashCode()
    }

    override fun toString() = "{weekdays: [${weekdays.joinToString(separator = ",")}]}"

    companion object {
        val EVERY_DAY = WeekdayList(127)
    }
}
