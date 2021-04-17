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
package org.isoron.uhabits.core.models.sqlite

import org.isoron.uhabits.core.database.Repository
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.memory.MemoryHabitList
import org.isoron.uhabits.core.models.sqlite.records.HabitRecord
import javax.inject.Inject

/**
 * Implementation of a [HabitList] that is backed by SQLite.
 */
class SQLiteHabitList @Inject constructor(private val modelFactory: ModelFactory) : HabitList() {
    private val repository: Repository<HabitRecord> = modelFactory.buildHabitListRepository()
    private val list: MemoryHabitList = MemoryHabitList()
    private var loaded = false
    private fun loadRecords() {
        if (loaded) return
        loaded = true
        list.removeAll()
        val records = repository.findAll("order by position")
        var shouldRebuildOrder = false
        for ((expectedPosition, rec) in records.withIndex()) {
            if (rec.position != expectedPosition) shouldRebuildOrder = true
            val h = modelFactory.buildHabit()
            rec.copyTo(h)
            (h.originalEntries as SQLiteEntryList).habitId = h.id
            list.add(h)
        }
        if (shouldRebuildOrder) rebuildOrder()
    }

    @Synchronized
    override fun add(habit: Habit) {
        loadRecords()
        habit.position = size()
        val record = HabitRecord()
        record.copyFrom(habit)
        repository.save(record)
        habit.id = record.id
        (habit.originalEntries as SQLiteEntryList).habitId = record.id
        list.add(habit)
        observable.notifyListeners()
    }

    @Synchronized
    override fun getById(id: Long): Habit? {
        loadRecords()
        return list.getById(id)
    }

    @Synchronized
    override fun getByUUID(uuid: String?): Habit? {
        loadRecords()
        return list.getByUUID(uuid)
    }

    @Synchronized
    override fun getByPosition(position: Int): Habit {
        loadRecords()
        return list.getByPosition(position)
    }

    @Synchronized
    override fun getFiltered(matcher: HabitMatcher?): HabitList {
        loadRecords()
        return list.getFiltered(matcher)
    }

    @set:Synchronized
    override var primaryOrder: Order
        get() = list.primaryOrder
        set(order) {
            list.primaryOrder = order
            observable.notifyListeners()
        }

    @set:Synchronized
    override var secondaryOrder: Order
        get() = list.secondaryOrder
        set(order) {
            list.secondaryOrder = order
            observable.notifyListeners()
        }

    @Synchronized
    override fun indexOf(h: Habit): Int {
        loadRecords()
        return list.indexOf(h)
    }

    @Synchronized
    override fun iterator(): Iterator<Habit> {
        loadRecords()
        return list.iterator()
    }

    @Synchronized
    private fun rebuildOrder() {
        val records = repository.findAll("order by position")
        repository.executeAsTransaction {
            for ((pos, r) in records.withIndex()) {
                if (r.position != pos) {
                    r.position = pos
                    repository.save(r)
                }
            }
        }
    }

    @Synchronized
    override fun remove(h: Habit) {
        loadRecords()
        list.remove(h)
        val record = repository.find(
            h.id!!
        ) ?: throw RuntimeException("habit not in database")
        repository.executeAsTransaction {
            h.originalEntries.clear()
            repository.remove(record)
        }
        rebuildOrder()
        observable.notifyListeners()
    }

    @Synchronized
    override fun removeAll() {
        list.removeAll()
        repository.execSQL("delete from habits")
        repository.execSQL("delete from repetitions")
        observable.notifyListeners()
    }

    @Synchronized
    override fun reorder(from: Habit, to: Habit) {
        loadRecords()
        list.reorder(from, to)
        val fromRecord = repository.find(
            from.id!!
        )
        val toRecord = repository.find(
            to.id!!
        )
        if (fromRecord == null) throw RuntimeException("habit not in database")
        if (toRecord == null) throw RuntimeException("habit not in database")
        if (toRecord.position!! < fromRecord.position!!) {
            repository.execSQL(
                "update habits set position = position + 1 " +
                    "where position >= ? and position < ?",
                toRecord.position!!,
                fromRecord.position!!
            )
        } else {
            repository.execSQL(
                "update habits set position = position - 1 " +
                    "where position > ? and position <= ?",
                fromRecord.position!!,
                toRecord.position!!
            )
        }
        fromRecord.position = toRecord.position
        repository.save(fromRecord)
        observable.notifyListeners()
    }

    @Synchronized
    override fun repair() {
        loadRecords()
        rebuildOrder()
        observable.notifyListeners()
    }

    @Synchronized
    override fun size(): Int {
        loadRecords()
        return list.size()
    }

    @Synchronized
    override fun update(habits: List<Habit>) {
        loadRecords()
        list.update(habits)
        for (h in habits) {
            val record = repository.find(h.id!!) ?: continue
            record.copyFrom(h)
            repository.save(record)
        }
        observable.notifyListeners()
    }

    override fun resort() {
        list.resort()
        observable.notifyListeners()
    }

    @Synchronized
    fun reload() {
        loaded = false
    }
}
