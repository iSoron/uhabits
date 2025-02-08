package org.isoron.uhabits.core.models

import org.isoron.uhabits.core.utils.DateUtils
import java.util.UUID

data class HabitGroup(
    var color: PaletteColor = PaletteColor(8),
    var description: String = "",
    var id: Long? = null,
    var isArchived: Boolean = false,
    var name: String = "",
    var position: Int = 0,
    var question: String = "",
    var reminder: Reminder? = null,
    var uuid: String? = null,
    var habitList: HabitList,
    val scores: ScoreList,
    val streaks: StreakList
) {

    constructor(
        parent: HabitGroup,
        matcher: HabitMatcher
    ) : this(
        parent.color,
        parent.description,
        parent.id,
        parent.isArchived,
        parent.name,
        parent.position,
        parent.question,
        parent.reminder,
        parent.uuid,
        parent.habitList.getFiltered(matcher),
        parent.scores,
        parent.streaks
    ) {
        this.collapsed = parent.collapsed
        this.parent = parent
    }

    init {
        if (uuid == null) this.uuid = UUID.randomUUID().toString().replace("-", "")
    }

    var observable = ModelObservable()

    var parent: HabitGroup? = null

    val uriString: String
        get() = "content://org.isoron.uhabits/habitgroup/$id"

    var collapsed = false
        set(value) {
            field = value
            habitList.collapsed = value
            if (parent != null) parent!!.collapsed = value
        }

    fun hasReminder(): Boolean = reminder != null

    fun isCompletedToday(): Boolean {
        if (habitList.isEmpty) return false
        return habitList.all { it.isCompletedToday() }
    }

    fun isEnteredToday(): Boolean {
        if (habitList.isEmpty) return false
        return habitList.all { it.isEnteredToday() }
    }

    fun firstEntryDate(): Timestamp {
        val today = DateUtils.getTodayWithOffset()
        var earliest = today
        for (h in habitList) {
            val first = h.firstEntryDate()
            if (earliest.isNewerThan(first)) earliest = first
        }
        return earliest
    }

    fun recompute() {
        for (h in habitList) h.recompute()

        val today = DateUtils.getTodayWithOffset()
        val to = today.plus(30)
        var from = firstEntryDate()
        if (from.isNewerThan(to)) from = to

        scores.combineFrom(
            habitList = habitList,
            from = from,
            to = to
        )

        streaks.combineFrom(
            habitList = habitList,
            from = from,
            to = to
        )
    }

    fun copyFrom(other: HabitGroup) {
        this.color = other.color
        this.description = other.description
        // this.id should not be copied
        this.isArchived = other.isArchived
        this.name = other.name
        this.position = other.position
        this.question = other.question
        this.reminder = other.reminder
        this.uuid = other.uuid
        this.habitList.groupId = this.id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HabitGroup) return false

        if (color != other.color) return false
        if (description != other.description) return false
        if (id != other.id) return false
        if (isArchived != other.isArchived) return false
        if (name != other.name) return false
        if (position != other.position) return false
        if (question != other.question) return false
        if (reminder != other.reminder) return false
        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + isArchived.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + position
        result = 31 * result + question.hashCode()
        result = 31 * result + (reminder?.hashCode() ?: 0)
        result = 31 * result + (uuid?.hashCode() ?: 0)
        return result
    }

    fun getHabitByUUID(uuid: String?): Habit? =
        habitList.getByUUID(uuid)
}
