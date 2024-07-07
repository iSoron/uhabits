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
     * Creates a new HabitGroupList.
     *
     * Depending on the implementation, this list can either be empty or be
     * populated by some pre-existing habitgroups, for example, from a certain
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
     * Inserts a new habit group in the list.
     *
     * If the id of the habit group is null, the list will assign it a new id, which
     * is guaranteed to be unique in the scope of the list. If id is not null,
     * the caller should make sure that the list does not already contain
     * another habit group with same id, otherwise a RuntimeException will be thrown.
     *
     * @param habitGroup the habit to be inserted
     * @throws IllegalArgumentException if the habit is already on the list.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun add(habitGroup: HabitGroup)

    /**
     * Returns the habit group with specified id.
     *
     * @param id the id of the habit group
     * @return the habit group, or null if none exist
     */
    abstract fun getById(id: Long): HabitGroup?

    /**
     * Returns the habit group with specified UUID.
     *
     * @param uuid the UUID of the habit group
     * @return the habit group, or null if none exist
     */
    abstract fun getByUUID(uuid: String?): HabitGroup?

    /**
     *  Returns the habit with the specified UUID which is
     *  present in any of the habit groups within this habit group list.
     */
    fun getHabitByUUID(uuid: String?): Habit? {
        for (hgr in this) {
            val habit = hgr.getHabitByUUID(uuid)
            if (habit != null) {
                return habit
            }
        }
        return null
    }

    /**
     *  Returns the habit with the specified UUID which is
     *  present in any of the habit groups within this habit group list.
     */
    fun getHabitByID(id: Long): Habit? {
        for (hgr in this) {
            val habit = hgr.habitList.getById(id)
            if (habit != null) {
                return habit
            }
        }
        return null
    }

    /**
     * Returns the habit group that occupies a certain position.
     *
     * @param position the position of the desired habit group
     * @return the habit group at that position
     * @throws IndexOutOfBoundsException when the position is invalid
     */
    abstract fun getByPosition(position: Int): HabitGroup

    /**
     * Returns the list of habit groups that match a given condition.
     *
     * @param matcher the matcher that checks the condition
     * @return the list of matching habit groups
     */
    abstract fun getFiltered(matcher: HabitMatcher?): HabitGroupList
    abstract var primaryOrder: Order
    abstract var secondaryOrder: Order

    /**
     * Returns the index of the given habit group in the list, or -1 if the list does
     * not contain the habit group.
     *
     * @param h the habit group
     * @return the index of the habit group, or -1 if not in the list
     */
    abstract fun indexOf(h: HabitGroup): Int
    val isEmpty: Boolean
        get() = size() == 0

    /**
     * Removes the given habit group from the list.
     *
     * If the given habit group is not in the list, does nothing.
     *
     * @param h the habit group to be removed.
     */
    abstract fun remove(h: HabitGroup)

    /**
     * Removes all the habit groups from the list.
     */
    open fun removeAll() {
        val copy: MutableList<HabitGroup> = LinkedList()
        for (h in this) copy.add(h)
        for (h in copy) remove(h)
        observable.notifyListeners()
    }

    /**
     * Changes the position of a habit group in the list.
     *
     * @param from the habit group that should be moved
     * @param to   the habit group that currently occupies the desired position
     */
    abstract fun reorder(from: HabitGroup, to: HabitGroup)
    open fun repair() {}

    /**
     * Returns the number of habit groups in this list.
     *
     * @return number of habit groups
     */
    abstract fun size(): Int

    /**
     * Notifies the list that a certain list of habit groups has been modified.
     *
     * Depending on the implementation, this operation might trigger a write to
     * disk, or do nothing at all. To make sure that the habit groups get persisted,
     * this operation must be called.
     *
     * @param habitGroups the list of habit groups that have been modified.
     */
    abstract fun update(habitGroups: List<HabitGroup>)

    /**
     * Notifies the list that a certain habit group has been modified.
     *
     * See [.update] for more details.
     *
     * @param habitGroup the habit groups that has been modified.
     */
    fun update(habitGroup: HabitGroup) {
        update(listOf(habitGroup))
    }

    /**
     * For each habit group, point all the habits in it
     * to the group it is contained in
     * */
    abstract fun attachHabitsToGroups()

    /**
     * Writes the list of habit groups to the given writer, in CSV format. There is
     * one line for each habit group, containing the fields name, description,
     * , and color. The color is written in HTML format (#000000).
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
        for (hgr in this) {
            val cols = arrayOf(
                String.format("%03d", indexOf(hgr) + 1),
                hgr.name,
                hgr.question,
                hgr.description,
                hgr.color.toCsvColor()
            )
            csv.writeNext(cols, false)
        }
        csv.close()
    }

    abstract fun resort()
}
