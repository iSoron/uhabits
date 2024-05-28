package org.isoron.uhabits.core.models

import com.opencsv.CSVWriter
import org.isoron.uhabits.core.models.HabitList.Order
import java.io.IOException
import java.io.Writer
import java.util.LinkedList
import javax.annotation.concurrent.ThreadSafe

/**
 * An ordered collection of [HabitGroup]s.
 */
@ThreadSafe
abstract class HabitGroupList : Iterable<HabitGroup> {
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
     * @param habitGroup the habit to be inserted
     * @throws IllegalArgumentException if the habit is already on the list.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun add(habitGroup: HabitGroup)

    /**
     * Returns the habit with specified id.
     *
     * @param id the id of the habit
     * @return the habit, or null if none exist
     */
    abstract fun getById(id: Long): HabitGroup?

    /**
     * Returns the habit with specified UUID.
     *
     * @param uuid the UUID of the habit
     * @return the habit, or null if none exist
     */
    abstract fun getByUUID(uuid: String?): HabitGroup?

    /**
     *  Returns the habit with the specified UUID which is
     *  present at any hierarchy within this list.
     */
    fun getHabitByUUIDDeep(uuid: String?): Habit? {
        for (hgr in this) {
            val habit = hgr.getHabitByUUIDDeep(uuid)
            if (habit != null) {
                return habit
            }
        }
        return null
    }

    /**
     * Returns the habit that occupies a certain position.
     *
     * @param position the position of the desired habit
     * @return the habit at that position
     * @throws IndexOutOfBoundsException when the position is invalid
     */
    abstract fun getByPosition(position: Int): HabitGroup

    /**
     * Returns the list of habits that match a given condition.
     *
     * @param matcher the matcher that checks the condition
     * @return the list of matching habits
     */
    abstract fun getFiltered(matcher: HabitMatcher?): HabitGroupList
    abstract var primaryOrder: Order
    abstract var secondaryOrder: Order

    /**
     * Returns the index of the given habit in the list, or -1 if the list does
     * not contain the habit.
     *
     * @param h the habit
     * @return the index of the habit, or -1 if not in the list
     */
    abstract fun indexOf(h: HabitGroup): Int
    val isEmpty: Boolean
        get() = size() == 0

    /**
     * Removes the given habit from the list.
     *
     * If the given habit is not in the list, does nothing.
     *
     * @param h the habit to be removed.
     */
    abstract fun remove(h: HabitGroup)

    /**
     * Removes all the habits from the list.
     */
    open fun removeAll() {
        val copy: MutableList<HabitGroup> = LinkedList()
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
    abstract fun reorder(from: HabitGroup, to: HabitGroup)
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
     * @param habitGroups the list of habits that have been modified.
     */
    abstract fun update(habitGroups: List<HabitGroup>)

    /**
     * Notifies the list that a certain habit has been modified.
     *
     * See [.update] for more details.
     *
     * @param habitGroup the habit that has been modified.
     */
    fun update(habitGroup: HabitGroup) {
        update(listOf(habitGroup))
    }

    fun populateGroupsWith(habitList: HabitList) {
        val toRemove = mutableListOf<String?>()
        for (habit in habitList) {
            val hgr = getByUUID(habit.parentUUID)
            if (hgr != null) {
                hgr.habitList.add(habit)
                habit.parent = hgr
                toRemove.add(habit.uuid)
            }
        }
        for (uuid in toRemove) {
            val h = habitList.getByUUID(uuid)
            if (h != null) {
                habitList.remove(h)
            }
        }
        for (hgr in this) {
            hgr.recompute()
        }
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
            val cols = arrayOf(
                String.format("%03d", indexOf(habit) + 1),
                habit.name,
                habit.question,
                habit.description,
                habit.color.toCsvColor()
            )
            csv.writeNext(cols, false)
        }
        csv.close()
    }

    abstract fun resort()
}
