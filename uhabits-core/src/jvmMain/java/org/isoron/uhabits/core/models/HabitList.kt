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

import com.opencsv.CSVWriter
import java.io.IOException
import java.io.Writer
import java.util.LinkedList
import javax.annotation.concurrent.ThreadSafe

/**
 * An ordered collection of [Habit]s.
 */
@ThreadSafe
abstract class HabitList : Iterable<Habit> {
    val observable: ModelObservable

    @JvmField
    protected val filter: HabitMatcher

    /**
     * Creates a new HabitList.
     *
     * Depending on the implementation, this list can either be empty or be
     * populated by some pre-existing habits, for example, from a certain
     * database.
     */
    constructor() {
        observable = ModelObservable()
        filter = HabitMatcher(isArchivedAllowed = true)
    }

    protected constructor(filter: HabitMatcher) {
        observable = ModelObservable()
        this.filter = filter
    }

    /**
     * Inserts a new habit in the list.
     *
     * If the id of the habit is null, the list will assign it a new id, which
     * is guaranteed to be unique in the scope of the list. If id is not null,
     * the caller should make sure that the list does not already contain
     * another habit with same id, otherwise a RuntimeException will be thrown.
     *
     * @param habit the habit to be inserted
     * @throws IllegalArgumentException if the habit is already on the list.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun add(habit: Habit)

    /**
     * Returns the habit with specified id.
     *
     * @param id the id of the habit
     * @return the habit, or null if none exist
     */
    abstract fun getById(id: Long): Habit?

    /**
     * Returns the habit with specified UUID.
     *
     * @param uuid the UUID of the habit
     * @return the habit, or null if none exist
     */
    abstract fun getByUUID(uuid: String?): Habit?

    /**
     * Returns the habit that occupies a certain position.
     *
     * @param position the position of the desired habit
     * @return the habit at that position
     * @throws IndexOutOfBoundsException when the position is invalid
     */
    abstract fun getByPosition(position: Int): Habit

    /**
     * Returns the list of habits that match a given condition.
     *
     * @param matcher the matcher that checks the condition
     * @return the list of matching habits
     */
    abstract fun getFiltered(matcher: HabitMatcher?): HabitList
    abstract var primaryOrder: Order
    abstract var secondaryOrder: Order

    /**
     * Returns the index of the given habit in the list, or -1 if the list does
     * not contain the habit.
     *
     * @param h the habit
     * @return the index of the habit, or -1 if not in the list
     */
    abstract fun indexOf(h: Habit): Int
    val isEmpty: Boolean
        get() = size() == 0

    /**
     * Removes the given habit from the list.
     *
     * If the given habit is not in the list, does nothing.
     *
     * @param h the habit to be removed.
     */
    abstract fun remove(h: Habit)

    /**
     * Removes all the habits from the list.
     */
    open fun removeAll() {
        val copy: MutableList<Habit> = LinkedList()
        for (h in this) copy.add(h)
        for (h in copy) remove(h)
        observable.notifyListeners()
    }

    /**
     * Changes the position of a habit in the list.
     *
     * @param from the habit that should be moved
     * @param to   the habit that currently occupies the desired position
     */
    abstract fun reorder(from: Habit, to: Habit)
    open fun repair() {}

    /**
     * Returns the number of habits in this list.
     *
     * @return number of habits
     */
    abstract fun size(): Int

    /**
     * Notifies the list that a certain list of habits has been modified.
     *
     * Depending on the implementation, this operation might trigger a write to
     * disk, or do nothing at all. To make sure that the habits get persisted,
     * this operation must be called.
     *
     * @param habits the list of habits that have been modified.
     */
    abstract fun update(habits: List<Habit>)

    /**
     * Notifies the list that a certain habit has been modified.
     *
     * See [.update] for more details.
     *
     * @param habit the habit that has been modified.
     */
    fun update(habit: Habit) {
        update(listOf(habit))
    }

    /**
     * Writes the list of habits to the given writer, in CSV format. There is
     * one line for each habit, containing the fields name, description,
     * frequency numerator, frequency denominator and color. The color is
     * written in HTML format (#000000).
     *
     * @param out the writer that will receive the result
     * @throws IOException if write operations fail
     */
    @Throws(IOException::class)
    fun writeCSV(out: Writer) {
        val header = arrayOf(
            "Position",
            "Name",
            "Question",
            "Description",
            "NumRepetitions",
            "Interval",
            "Color"
        )
        val csv = CSVWriter(out)
        csv.writeNext(header, false)
        for (habit in this) {
            val (numerator, denominator) = habit.frequency
            val cols = arrayOf(
                String.format("%03d", indexOf(habit) + 1),
                habit.name,
                habit.question,
                habit.description,
                numerator.toString(),
                denominator.toString(),
                habit.color.toCsvColor()
            )
            csv.writeNext(cols, false)
        }
        csv.close()
    }

    abstract fun resort()
    enum class Order {
        BY_NAME_ASC,
        BY_NAME_DESC,
        BY_COLOR_ASC,
        BY_COLOR_DESC,
        BY_SCORE_ASC,
        BY_SCORE_DESC,
        BY_STATUS_ASC,
        BY_STATUS_DESC,
        BY_POSITION
    }
}
