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
package org.isoron.uhabits.core.models.sqlite.records

import org.isoron.uhabits.core.database.Column
import org.isoron.uhabits.core.database.Table
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import java.util.Objects.requireNonNull

/**
 * The SQLite database record corresponding to a [Habit].
 */
@Table(name = "habits")
class HabitRecord {
    @field:Column
    var description: String? = null

    @field:Column
    var question: String? = null

    @field:Column
    var name: String? = null

    @field:Column(name = "freq_num")
    var freqNum: Int? = null

    @field:Column(name = "freq_den")
    var freqDen: Int? = null

    @field:Column
    var color: Int? = null

    @field:Column
    var position: Int? = null

    @field:Column(name = "reminder_hour")
    var reminderHour: Int? = null

    @field:Column(name = "reminder_min")
    var reminderMin: Int? = null

    @field:Column(name = "reminder_days")
    var reminderDays: Int? = null

    @field:Column
    var highlight: Int? = null

    @field:Column
    var archived: Int? = null

    @field:Column
    var type: Int? = null

    @field:Column(name = "target_value")
    var targetValue: Double? = null

    @field:Column(name = "target_type")
    var targetType: Int? = null

    @field:Column
    var unit: String? = null

    @field:Column
    var id: Long? = null

    @field:Column
    var uuid: String? = null

    fun copyFrom(model: Habit) {
        id = model.id
        name = model.name
        description = model.description
        highlight = 0
        color = model.color.paletteIndex
        archived = if (model.isArchived) 1 else 0
        type = model.type.value
        targetType = model.targetType.value
        targetValue = model.targetValue
        unit = model.unit
        position = model.position
        question = model.question
        uuid = model.uuid
        val (numerator, denominator) = model.frequency
        freqNum = numerator
        freqDen = denominator
        reminderDays = 0
        reminderMin = null
        reminderHour = null
        if (model.hasReminder()) {
            val reminder = model.reminder
            reminderHour = requireNonNull(reminder)!!.hour
            reminderMin = reminder!!.minute
            reminderDays = reminder.days.toInteger()
        }
    }

    fun copyTo(habit: Habit) {
        habit.id = id
        habit.name = name!!
        habit.description = description!!
        habit.question = question!!
        habit.frequency = Frequency(freqNum!!, freqDen!!)
        habit.color = PaletteColor(color!!)
        habit.isArchived = archived != 0
        habit.type = HabitType.fromInt(type!!)
        habit.targetType = NumericalHabitType.fromInt(targetType!!)
        habit.targetValue = targetValue!!
        habit.unit = unit!!
        habit.position = position!!
        habit.uuid = uuid
        if (reminderHour != null && reminderMin != null) {
            habit.reminder = Reminder(
                reminderHour!!,
                reminderMin!!,
                WeekdayList(reminderDays!!)
            )
        }
    }
}
