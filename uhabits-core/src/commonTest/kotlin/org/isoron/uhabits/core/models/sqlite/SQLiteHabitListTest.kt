/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

import dev.mokkery.mock
import dev.mokkery.verify
import kotlinx.coroutines.test.runTest
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.database.HabitRepository
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.ModelObservable
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.test.HabitFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SQLiteHabitListTest : BaseUnitTest() {
    private lateinit var repository: HabitRepository
    private var listener: ModelObservable.Listener = mock()
    private lateinit var habitsArray: ArrayList<Habit>
    private lateinit var activeHabits: HabitList
    private lateinit var reminderHabits: HabitList

    private suspend fun initDb() {
        val db = buildMemoryDatabase()
        modelFactory = SQLModelFactory(db)
        habitList = SQLiteHabitList(modelFactory)
        fixtures = HabitFixtures(modelFactory, habitList)
        repository = (modelFactory as SQLModelFactory).habitRepository
        habitsArray = ArrayList()
        for (i in 0..9) {
            val habit = fixtures.createEmptyHabit()
            habit.name = "habit " + (i + 1)
            habitList.update(habit)
            habitsArray.add(habit)
            if (i % 3 == 0) habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        }
        habitsArray[0].isArchived = true
        habitsArray[1].isArchived = true
        habitsArray[4].isArchived = true
        habitsArray[7].isArchived = true
        habitList.update(habitsArray)
        activeHabits = habitList.getFiltered(HabitMatcher())
        reminderHabits = habitList.getFiltered(
            HabitMatcher(
                isArchivedAllowed = true,
                isReminderRequired = true
            )
        )
        habitList.observable.addListener(listener)
    }

    private fun dbTest(block: suspend () -> Unit) = runTest {
        initDb()
        try {
            block()
        } finally {
            habitList.observable.removeListener(listener)
        }
    }

    @Test
    fun testAdd_withDuplicate() = dbTest {
        val habit = modelFactory.buildHabit()
        habitList.add(habit)
        verify { listener.onModelChange() }
        assertFailsWith<IllegalArgumentException> {
            habitList.add(habit)
        }
    }

    @Test
    fun testAdd_withId() = dbTest {
        val habit = modelFactory.buildHabit()
        habit.name = "Hello world with id"
        habit.id = 12300L
        habitList.add(habit)
        assertEquals(12300L, habit.id)
        val all = repository.findAll()
        val record = all.find { it.id == 12300L }
        assertEquals(habit.name, record!!.name)
    }

    @Test
    fun testAdd_withoutId() = dbTest {
        val habit = modelFactory.buildHabit()
        habit.name = "Hello world"
        assertNull(habit.id)
        habitList.add(habit)
        val all = repository.findAll()
        val record = all.find { it.id == habit.id }
        assertEquals(habit.name, record!!.name)
    }

    @Test
    fun testSize() = dbTest {
        assertEquals(10, habitList.size())
    }

    @Test
    fun testGetFiltered_nullMatcher() = dbTest {
        assertEquals(10, habitList.getFiltered(null).size())
    }

    @Test
    fun testGetById() = dbTest {
        val h1 = habitList.getById(1)!!
        assertEquals("habit 1", h1.name)
        val h2 = habitList.getById(2)!!
        assertEquals(h2, h2)
    }

    @Test
    fun testGetById_withInvalid() = dbTest {
        val invalidId = 9183792001L
        val h1 = habitList.getById(invalidId)
        assertNull(h1)
    }

    @Test
    fun testGetByPosition() = dbTest {
        val h = habitList.getByPosition(4)
        assertEquals("habit 5", h.name)
    }

    @Test
    fun testIndexOf() = dbTest {
        val h1 = habitList.getByPosition(5)
        assertEquals(5, habitList.indexOf(h1))
        val h2 = modelFactory.buildHabit()
        assertEquals(-1, habitList.indexOf(h2))
        h2.id = 1000L
        assertEquals(-1, habitList.indexOf(h2))
    }

    @Test
    fun testRemove() = dbTest {
        val h = habitList.getById(2)
        habitList.remove(h!!)
        assertEquals(-1, habitList.indexOf(h))

        val all = repository.findAll()
        val rec2 = all.find { it.id == 2L }
        assertNull(rec2)
        val rec3 = all.find { it.id == 3L }!!
        assertEquals(1, rec3.position)
    }

    @Test
    fun testRemove_orderByName() = dbTest {
        habitList.primaryOrder = HabitList.Order.BY_NAME_DESC
        val h = habitList.getById(2)
        habitList.remove(h!!)
        assertEquals(-1, habitList.indexOf(h))

        val all = repository.findAll()
        val rec2 = all.find { it.id == 2L }
        assertNull(rec2)
        val rec3 = all.find { it.id == 3L }!!
        assertEquals(1, rec3.position)
    }

    @Test
    fun testReorder() = dbTest {
        val habit3 = habitList.getById(3)!!
        val habit4 = habitList.getById(4)!!
        habitList.reorder(habit4, habit3)
        val all = repository.findAll()
        val record3 = all.find { it.id == 3L }!!
        assertEquals(3, record3.position)
        val record4 = all.find { it.id == 4L }!!
        assertEquals(2, record4.position)
    }

    @Test
    fun testMoveBefore_moveUp() = dbTest {
        habitList.moveBefore(habitList.getById(5)!!, habitList.getById(1)!!)
        assertEquals(
            listOf(5L, 1L, 2L, 3L, 4L, 6L, 7L, 8L, 9L, 10L),
            repository.findAll().map { it.id!! }
        )
        val all = repository.findAll()
        assertEquals(0, all.find { it.id == 5L }!!.position)
        assertEquals(1, all.find { it.id == 1L }!!.position)
        all.forEachIndexed { i, r -> assertEquals(i, r.position) }
    }

    @Test
    fun testMoveAfter_moveDown() = dbTest {
        habitList.moveAfter(habitList.getById(1)!!, habitList.getById(8)!!)
        assertEquals(
            listOf(2L, 3L, 4L, 5L, 6L, 7L, 8L, 1L, 9L, 10L),
            repository.findAll().map { it.id!! }
        )
        val all = repository.findAll()
        assertEquals(7, all.find { it.id == 1L }!!.position)
        all.forEachIndexed { i, r -> assertEquals(i, r.position) }
    }

    @Test
    fun testMoveAfter_hiddenItemsPresent() = dbTest {
        habitList.moveAfter(habitList.getById(3)!!, habitList.getById(9)!!)
        assertEquals(
            listOf(1L, 2L, 4L, 5L, 6L, 7L, 8L, 9L, 3L, 10L),
            repository.findAll().map { it.id!! }
        )
        val all = repository.findAll()
        all.forEachIndexed { i, r -> assertEquals(i, r.position) }
    }

    @Test
    fun testMoveBefore_onSortedList() = dbTest {
        habitList.primaryOrder = HabitList.Order.BY_SCORE_DESC
        assertFailsWith<IllegalStateException> {
            habitList.moveBefore(habitList.getById(1)!!, habitList.getById(2)!!)
        }
        assertFailsWith<IllegalStateException> {
            habitList.moveAfter(habitList.getById(1)!!, habitList.getById(2)!!)
        }
    }
}
