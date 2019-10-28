/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

import org.apache.commons.lang3.builder.ToStringBuilder
import org.isoron.uhabits.core.models.Checkmark.UNCHECKED
import org.isoron.uhabits.core.utils.StringUtils.defaultToStringStyle
import java.util.*
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject

/**
 * The thing that the user wants to track.
 */
@ThreadSafe
class Habit {

    @get:Synchronized
    @set:Synchronized
    var id: Long? = null

    private var data: HabitData

    var streaks: StreakList
        private set

    var scores: ScoreList
        private set

    var repetitions: RepetitionList
        private set

    /**
     * List of checkmarks belonging to this habit.
     */
    @get:Synchronized
    var checkmarks: CheckmarkList
        private set

    var observable = ModelObservable()

    /**
     * Color of the habit.
     *
     *
     * This number is not an android.graphics.Color, but an index to the
     * activity color palette, which changes according to the theme. To convert
     * this color into an android.graphics.Color, use ColorHelper.getColor(context,
     * habit.color).
     */
    var color: Int
        @Synchronized get() = data.color
        @Synchronized set(color) {
            data.color = color
        }

    var description: String
        @Synchronized get() = data.description
        @Synchronized set(description) {
            data.description = description
        }

    var frequency: Frequency
        @Synchronized get() = data.frequency
        @Synchronized set(frequency) {
            data.frequency = frequency
        }

    var name: String
        @Synchronized get() = data.name
        @Synchronized set(name) {
            data.name = name
        }

    /**
     * Returns the reminder for this habit.
     *
     *
     * Before calling this method, you should call [.hasReminder] to
     * verify that a reminder does exist, otherwise an exception will be
     * thrown.
     *
     * @return the reminder for this habit
     * @throws IllegalStateException if habit has no reminder
     */
    var reminder: Reminder?
        @Synchronized get() {
            checkNotNull(data.reminder)
            return data.reminder
        }
        @Synchronized set(reminder) {
            data.reminder = reminder
        }

    var targetType: Int
        @Synchronized get() = data.targetType
        @Synchronized set(targetType) {
            require(!(targetType != AT_LEAST && targetType != AT_MOST)) {
                String.format("invalid targetType: %d", targetType)
            }
            data.targetType = targetType
        }

    var targetValue: Double
        @Synchronized get() = data.targetValue
        @Synchronized set(targetValue) {
            require(targetValue >= 0)
            data.targetValue = targetValue
        }

    var type: Int
        @Synchronized get() = data.type
        @Synchronized set(type) {
            require(!(type != YES_NO_HABIT && type != NUMBER_HABIT))
            data.type = type
        }

    var unit: String
        @Synchronized get() = data.unit
        @Synchronized set(unit) {
            data.unit = unit
        }

    /**
     * Returns the public URI that identifies this habit
     *
     * @return the URI
     */
    val uriString: String
        get() = String.format(Locale.US, HABIT_URI_FORMAT, id)

    var isArchived: Boolean
        @Synchronized get() = data.archived
        @Synchronized set(archived) {
            data.archived = archived
        }

    val isCompletedToday: Boolean
        @Synchronized get() {
            val todayCheckmark = checkmarks.todayValue
            return if (isNumerical) {
                if (targetType == AT_LEAST)
                    todayCheckmark >= data.targetValue
                else
                    todayCheckmark <= data.targetValue
            } else
                todayCheckmark != UNCHECKED
        }

    val isNumerical: Boolean
        @Synchronized get() = data.type == NUMBER_HABIT

    val position: Int?
        get() = data.position

    /**
     * Constructs a habit with default data.
     *
     *
     * The habit is not archived, not highlighted, has no reminders and is
     * placed in the last position of the list of habits.
     */
    @Inject
    internal constructor(factory: ModelFactory) {
        this.data = HabitData()
        checkmarks = factory.buildCheckmarkList(this)
        streaks = factory.buildStreakList(this)
        scores = factory.buildScoreList(this)
        repetitions = factory.buildRepetitionList(this)
    }

    internal constructor(factory: ModelFactory, data: HabitData) {
        this.data = data.copy()
        checkmarks = factory.buildCheckmarkList(this)
        streaks = factory.buildStreakList(this)
        scores = factory.buildScoreList(this)
        repetitions = factory.buildRepetitionList(this)
        observable = ModelObservable()
    }

    /**
     * Clears the reminder for a habit.
     */
    @Synchronized
    fun clearReminder() {
        data.reminder = null
        observable.notifyListeners()
    }

    /**
     * Copies all the attributes of the specified habit into this habit
     *
     * @param model the model whose attributes should be copied from
     */
    @Synchronized
    fun copyFrom(model: Habit) {
        this.data = model.data.copy()
        observable.notifyListeners()
    }

    @Synchronized
    fun hasId(): Boolean {
        return id != null
    }

    /**
     * Returns whether the habit has a reminder.
     *
     * @return true if habit has reminder, false otherwise
     */
    @Synchronized
    fun hasReminder(): Boolean {
        return data.reminder != null
    }

    fun invalidateNewerThan(timestamp: Timestamp) {
        scores.invalidateNewerThan(timestamp)
        checkmarks.invalidateNewerThan(timestamp)
        streaks.invalidateNewerThan(timestamp)
    }

    fun getData(): HabitData {
        return data.copy()
    }

    fun setPosition(newPosition: Int) {
        data.position = newPosition
    }

    data class HabitData(
            var name: String = "",
            var description: String = "",
            var frequency: Frequency = Frequency(3, 7),
            var color: Int = 8,
            var archived: Boolean = false,
            var targetType: Int = AT_LEAST,
            var targetValue: Double = 100.0,
            var type: Int = YES_NO_HABIT,
            var unit: String = "",
            var reminder: Reminder? = null,
            var position: Int = 0
    )

    override fun toString(): String {
        return ToStringBuilder(this, defaultToStringStyle())
                .append("id", id)
                .append("data", data)
                .toString()
    }

    companion object {
        const val AT_LEAST = 0

        const val AT_MOST = 1

        const val HABIT_URI_FORMAT = "content://org.isoron.uhabits/habit/%d"

        const val NUMBER_HABIT = 1

        const val YES_NO_HABIT = 0
    }
}
