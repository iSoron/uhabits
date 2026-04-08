package org.isoron.uhabits.core.models.sqlite

import me.tatarka.inject.annotations.Inject
import org.isoron.uhabits.core.database.Repository
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList.Order
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.memory.MemoryHabitGroupList
import org.isoron.uhabits.core.models.sqlite.records.HabitGroupRecord
import org.isoron.uhabits.core.preferences.WidgetPreferences

/**
 * Implementation of a [HabitGroupList] that is backed by SQLite.
 */
@Inject
class SQLiteHabitGroupList(
    private val modelFactory: ModelFactory,
    private val widgetPreferences: WidgetPreferences // Added Injection
) : HabitGroupList() {
    private val repository: Repository<HabitGroupRecord> = modelFactory.buildHabitGroupListRepository()
    private val list: MemoryHabitGroupList = MemoryHabitGroupList()
    private var loaded = false
    private fun loadRecords() {
        if (loaded) return
        loaded = true
        list.removeAll()
        val records = repository.findAll("order by position")
        var shouldRebuildOrder = false
        for ((expectedPosition, rec) in records.withIndex()) {
            if (rec.position != expectedPosition) shouldRebuildOrder = true
            val h = modelFactory.buildHabitGroup()
            rec.copyTo(h)
            list.add(h)
        }
        if (shouldRebuildOrder) rebuildOrder()
    }

    @Synchronized
    override fun add(habitGroup: HabitGroup) {
        loadRecords()
        habitGroup.position = size()
        habitGroup.id = repository.getNextAvailableId("habitandgroup")
        val record = HabitGroupRecord()
        record.copyFrom(habitGroup)
        repository.save(record)
        habitGroup.habitList.groupId = record.id
        list.add(habitGroup)
        observable.notifyListeners()
    }

    @Synchronized
    override fun getById(id: Long): HabitGroup? {
        loadRecords()
        return list.getById(id)
    }

    @Synchronized
    override fun getByUUID(uuid: String?): HabitGroup? {
        loadRecords()
        return list.getByUUID(uuid)
    }

    @Synchronized
    override fun getByPosition(position: Int): HabitGroup {
        loadRecords()
        return list.getByPosition(position)
    }

    @Synchronized
    override fun getFiltered(matcher: HabitMatcher?): HabitGroupList {
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
    override fun indexOf(h: HabitGroup): Int {
        loadRecords()
        return list.indexOf(h)
    }

    @Synchronized
    override fun iterator(): Iterator<HabitGroup> {
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
    override fun remove(h: HabitGroup) {
        loadRecords()
        list.remove(h)
        val record = repository.find(
            h.id!!
        ) ?: throw RuntimeException("habit group not in database")

        repository.executeAsTransaction {
            // 1. Cleanup Widget Preferences for the Group itself
            widgetPreferences.removeSnoozeTime(h.id!!)

            // 2. Cleanup Widget Preferences and Data for every Habit in the Group
            for (habit in h.habitList) {
                widgetPreferences.removeSnoozeTime(habit.id!!)
            }

            // 3. Cascading Delete in Database
            // Delete performance data for habits in this group
            repository.execSQL(
                "delete from repetitions where habit in (select id from habits where group_id = ?)",
                h.id!!
            )
            // Delete habits in this group
            repository.execSQL(
                "delete from habits where group_id = ?",
                h.id!!
            )
            // 4. Delete the group itself
            repository.remove(record)
        }
        rebuildOrder()
        observable.notifyListeners()
    }

    @Synchronized
    override fun removeAll() {
        list.removeAll()
        repository.execSQL("delete from habitgroups")
        repository.execSQL("delete from habits")
        repository.execSQL("delete from repetitions")
        observable.notifyListeners()
    }

    @Synchronized
    override fun reorder(from: HabitGroup, to: HabitGroup) {
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
                "update habitgroups set position = position + 1 " +
                    "where position >= ? and position < ?",
                toRecord.position!!,
                fromRecord.position!!
            )
        } else {
            repository.execSQL(
                "update habitgroups set position = position - 1 " +
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
    override fun update(habitGroups: List<HabitGroup>) {
        loadRecords()
        list.update(habitGroups)
        for (hgr in habitGroups) {
            val record = repository.find(hgr.id!!) ?: continue
            record.copyFrom(hgr)
            repository.save(record)
        }
        observable.notifyListeners()
    }

    override fun attachHabitsToGroups() {
        list.attachHabitsToGroups()
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
