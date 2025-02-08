package org.isoron.uhabits.core.models.sqlite.records

import org.isoron.uhabits.core.database.Column
import org.isoron.uhabits.core.database.Table
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import java.util.Objects.requireNonNull

/**
 * The SQLite database record corresponding to a [HabitGroup].
 */
@Table(name = "habitgroups")
class HabitGroupRecord {
    @field:Column
    var description: String? = null

    @field:Column
    var question: String? = null

    @field:Column
    var name: String? = null

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
    var id: Long? = null

    @field:Column
    var uuid: String? = null

    fun copyFrom(model: HabitGroup) {
        id = model.id
        name = model.name
        description = model.description
        highlight = 0
        color = model.color.paletteIndex
        archived = if (model.isArchived) 1 else 0
        position = model.position
        question = model.question
        uuid = model.uuid
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

    fun copyTo(habitGroup: HabitGroup) {
        habitGroup.id = id
        habitGroup.name = name!!
        habitGroup.description = description!!
        habitGroup.question = question!!
        habitGroup.color = PaletteColor(color!!)
        habitGroup.isArchived = archived != 0
        habitGroup.position = position!!
        habitGroup.uuid = uuid
        habitGroup.habitList.groupId = id
        if (reminderHour != null && reminderMin != null) {
            habitGroup.reminder = Reminder(
                reminderHour!!,
                reminderMin!!,
                WeekdayList(reminderDays!!)
            )
        }
    }
}
