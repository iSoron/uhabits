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
package org.isoron.uhabits.core.models.memory

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import java.util.ArrayList
import java.util.Comparator
import java.util.LinkedList
import java.util.Objects

/**
 * In-memory implementation of [HabitList].
 */
class MemoryHabitList : HabitList {
    private val list = LinkedList<Habit>()

    @get:Synchronized
    override var primaryOrder = Order.BY_POSITION
        set(value) {
            field = value
            comparator = getComposedComparatorByOrder(primaryOrder, secondaryOrder)
            resort()
        }

    @get:Synchronized
    override var secondaryOrder = Order.BY_NAME_ASC
        set(value) {
            field = value
            comparator = getComposedComparatorByOrder(primaryOrder, secondaryOrder)
            resort()
        }

    private var comparator: Comparator<Habit>? =
        getComposedComparatorByOrder(primaryOrder, secondaryOrder)
    private var parent: MemoryHabitList? = null

    constructor() : super()
    constructor(
        matcher: HabitMatcher,
        comparator: Comparator<Habit>?,
        parent: MemoryHabitList
    ) : super(matcher) {
        this.parent = parent
        this.comparator = comparator
        primaryOrder = parent.primaryOrder
        secondaryOrder = parent.secondaryOrder
        parent.observable.addListener { loadFromParent() }
        loadFromParent()
    }

    @Synchronized
    @Throws(IllegalArgumentException::class)
    override fun add(habit: Habit) {
        throwIfHasParent()
        require(!list.contains(habit)) { "habit already added" }
        val id = habit.id
        if (id != null && getById(id) != null) throw RuntimeException("duplicate id")
        if (id == null) habit.id = list.size.toLong()
        list.addLast(habit)
        resort()
    }

    @Synchronized
    override fun getById(id: Long): Habit? {
        for (h in list) {
            checkNotNull(h.id)
            if (h.id == id) return h
        }
        return null
    }

    @Synchronized
    override fun getByUUID(uuid: String?): Habit? {
        for (h in list) if (Objects.requireNonNull(h.uuid) == uuid) return h
        return null
    }

    @Synchronized
    override fun getByPosition(position: Int): Habit {
        return list[position]
    }

    @Synchronized
    override fun getFiltered(matcher: HabitMatcher?): HabitList {
        return MemoryHabitList(matcher!!, comparator, this)
    }

    private fun getComposedComparatorByOrder(
        firstOrder: Order,
        secondOrder: Order?
    ): Comparator<Habit> {
        return Comparator { h1: Habit, h2: Habit ->
            val firstResult = getComparatorByOrder(firstOrder).compare(h1, h2)
            if (firstResult != 0 || secondOrder == null) {
                return@Comparator firstResult
            }
            getComparatorByOrder(secondOrder).compare(h1, h2)
        }
    }

    private fun getComparatorByOrder(order: Order): Comparator<Habit> {
        val nameComparatorAsc = Comparator<Habit> { habit1, habit2 ->
            habit1.name.compareTo(habit2.name)
        }
        val nameComparatorDesc =
            Comparator { h1: Habit, h2: Habit -> nameComparatorAsc.compare(h2, h1) }
        val colorComparatorAsc = Comparator<Habit> { (color1), (color2) ->
            color1.compareTo(color2)
        }
        val colorComparatorDesc =
            Comparator { h1: Habit, h2: Habit -> colorComparatorAsc.compare(h2, h1) }
        val scoreComparatorDesc =
            Comparator<Habit> { habit1, habit2 ->
                val today = getTodayWithOffset()
                habit1.scores[today].value.compareTo(habit2.scores[today].value)
            }
        val scoreComparatorAsc =
            Comparator { h1: Habit, h2: Habit -> scoreComparatorDesc.compare(h2, h1) }
        val positionComparator =
            Comparator<Habit> { habit1, habit2 -> habit1.position.compareTo(habit2.position) }
        val statusComparatorDesc = Comparator { h1: Habit, h2: Habit ->
            if (h1.isCompletedToday() != h2.isCompletedToday()) {
                return@Comparator if (h1.isCompletedToday()) -1 else 1
            }
            if (h1.isNumerical != h2.isNumerical) {
                return@Comparator if (h1.isNumerical) -1 else 1
            }
            val today = getTodayWithOffset()
            val v1 = h1.computedEntries.get(today).value
            val v2 = h2.computedEntries.get(today).value
            v2.compareTo(v1)
        }
        val statusComparatorAsc =
            Comparator { h1: Habit, h2: Habit -> statusComparatorDesc.compare(h2, h1) }
        return when {
            order === Order.BY_POSITION -> positionComparator
            order === Order.BY_NAME_ASC -> nameComparatorAsc
            order === Order.BY_NAME_DESC -> nameComparatorDesc
            order === Order.BY_COLOR_ASC -> colorComparatorAsc
            order === Order.BY_COLOR_DESC -> colorComparatorDesc
            order === Order.BY_SCORE_DESC -> scoreComparatorDesc
            order === Order.BY_SCORE_ASC -> scoreComparatorAsc
            order === Order.BY_STATUS_DESC -> statusComparatorDesc
            order === Order.BY_STATUS_ASC -> statusComparatorAsc
            else -> throw IllegalStateException()
        }
    }

    @Synchronized
    override fun indexOf(h: Habit): Int {
        return list.indexOf(h)
    }

    @Synchronized
    override fun iterator(): Iterator<Habit> {
        return ArrayList(list).iterator()
    }

    @Synchronized
    override fun remove(h: Habit) {
        throwIfHasParent()
        list.remove(h)
        observable.notifyListeners()
    }

    @Synchronized
    override fun reorder(from: Habit, to: Habit) {
        throwIfHasParent()
        check(!(primaryOrder !== Order.BY_POSITION)) { "cannot reorder automatically sorted list" }
        require(indexOf(from) >= 0) { "list does not contain (from) habit" }
        val toPos = indexOf(to)
        require(toPos >= 0) { "list does not contain (to) habit" }
        list.remove(from)
        list.add(toPos, from)
        var position = 0
        for (h in list) h.position = position++
        observable.notifyListeners()
    }

    @Synchronized
    override fun size(): Int {
        return list.size
    }

    @Synchronized
    override fun update(habits: List<Habit>) {
        resort()
    }

    private fun throwIfHasParent() {
        check(parent == null) {
            "Filtered lists cannot be modified directly. " +
                "You should modify the parent list instead."
        }
    }

    @Synchronized
    private fun loadFromParent() {
        checkNotNull(parent)
        list.clear()
        for (h in parent!!) if (filter.matches(h)) list.add(h)
        resort()
    }

    @Synchronized
    override fun resort() {
        if (comparator != null) list.sortWith(comparator!!)
        observable.notifyListeners()
    }
}
