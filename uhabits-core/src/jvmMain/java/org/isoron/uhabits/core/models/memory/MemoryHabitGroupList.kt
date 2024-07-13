package org.isoron.uhabits.core.models.memory

import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList.Order
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import java.util.LinkedList
import java.util.Objects

/**
 * In-memory implementation of [HabitGroupList].
 */
class MemoryHabitGroupList : HabitGroupList {
    private val list = LinkedList<HabitGroup>()

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

    private var comparator: Comparator<HabitGroup>? =
        getComposedComparatorByOrder(primaryOrder, secondaryOrder)
    private var parent: MemoryHabitGroupList? = null

    constructor() : super()
    constructor(
        matcher: HabitMatcher,
        comparator: Comparator<HabitGroup>?,
        parent: MemoryHabitGroupList
    ) : super(matcher) {
        this.parent = parent
        this.comparator = comparator
        primaryOrder = parent.primaryOrder
        secondaryOrder = parent.secondaryOrder
        parent.observable.addListener { loadFromParent() }
        for (hgr in parent.list) {
            hgr.habitList.observable.addListener { loadFromParent() }
            hgr.observable.notifyListeners()
        }
        loadFromParent()
    }

    @Synchronized
    @Throws(IllegalArgumentException::class)
    override fun add(habitGroup: HabitGroup) {
        throwIfHasParent()
        require(!list.contains(habitGroup)) { "habit already added" }
        val id = habitGroup.id
        if (id != null && getById(id) != null) throw RuntimeException("duplicate id")
        if (id == null) habitGroup.id = list.size.toLong()
        list.addLast(habitGroup)
        resort()
    }

    @Synchronized
    override fun getById(id: Long): HabitGroup? {
        for (h in list) {
            checkNotNull(h.id)
            if (h.id == id) return h
        }
        return null
    }

    @Synchronized
    override fun getByUUID(uuid: String?): HabitGroup? {
        for (h in list) if (Objects.requireNonNull(h.uuid) == uuid) return h
        return null
    }

    @Synchronized
    override fun getByPosition(position: Int): HabitGroup {
        return list[position]
    }

    @Synchronized
    override fun getFiltered(matcher: HabitMatcher?): HabitGroupList {
        return MemoryHabitGroupList(matcher!!, comparator, this)
    }

    private fun getComposedComparatorByOrder(
        firstOrder: Order,
        secondOrder: Order?
    ): Comparator<HabitGroup> {
        return Comparator { h1: HabitGroup, h2: HabitGroup ->
            val firstResult = getComparatorByOrder(firstOrder).compare(h1, h2)
            if (firstResult != 0 || secondOrder == null) {
                return@Comparator firstResult
            }
            getComparatorByOrder(secondOrder).compare(h1, h2)
        }
    }

    private fun getComparatorByOrder(order: Order): Comparator<HabitGroup> {
        val nameComparatorAsc = Comparator<HabitGroup> { habit1, habit2 ->
            habit1.name.compareTo(habit2.name)
        }
        val nameComparatorDesc =
            Comparator { h1: HabitGroup, h2: HabitGroup -> nameComparatorAsc.compare(h2, h1) }
        val colorComparatorAsc = Comparator<HabitGroup> { (color1), (color2) ->
            color1.compareTo(color2)
        }
        val colorComparatorDesc =
            Comparator { h1: HabitGroup, h2: HabitGroup -> colorComparatorAsc.compare(h2, h1) }
        val scoreComparatorDesc =
            Comparator<HabitGroup> { habit1, habit2 ->
                val today = getTodayWithOffset()
                habit1.scores[today].value.compareTo(habit2.scores[today].value)
            }
        val scoreComparatorAsc =
            Comparator { h1: HabitGroup, h2: HabitGroup -> scoreComparatorDesc.compare(h2, h1) }
        val positionComparator =
            Comparator<HabitGroup> { habit1, habit2 -> habit1.position.compareTo(habit2.position) }
        val statusComparatorDesc = Comparator { h1: HabitGroup, h2: HabitGroup ->
            if (h1.isCompletedToday() != h2.isCompletedToday()) {
                return@Comparator if (h1.isCompletedToday()) -1 else 1
            }
            val today = getTodayWithOffset()
            val v1 = h1.scores[today].value
            val v2 = h2.scores[today].value
            v2.compareTo(v1)
        }
        val statusComparatorAsc =
            Comparator { h1: HabitGroup, h2: HabitGroup -> statusComparatorDesc.compare(h2, h1) }
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
    override fun indexOf(h: HabitGroup): Int {
        return list.indexOf(h)
    }

    @Synchronized
    override fun iterator(): Iterator<HabitGroup> {
        return ArrayList(list).iterator()
    }

    @Synchronized
    override fun remove(h: HabitGroup) {
        throwIfHasParent()
        list.remove(h)
        observable.notifyListeners()
    }

    @Synchronized
    override fun reorder(from: HabitGroup, to: HabitGroup) {
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
    override fun update(habitGroups: List<HabitGroup>) {
        resort()
    }

    override fun attachHabitsToGroups() {
        for (hgr in list) {
            for (h in hgr.habitList) {
                h.group = hgr
            }
        }
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
        for (hgr in parent!!) {
            if (filter.matches(hgr)) {
                val filteredHgr = HabitGroup(hgr, filter)
                list.add(filteredHgr)
            }
        }
        resort()
    }

    @Synchronized
    override fun resort() {
        for (hgr in list) {
            hgr.habitList.primaryOrder = primaryOrder
            hgr.habitList.secondaryOrder = secondaryOrder
            hgr.habitList.resort()
        }
        if (comparator != null) list.sortWith(comparator!!)
        observable.notifyListeners()
    }
}
