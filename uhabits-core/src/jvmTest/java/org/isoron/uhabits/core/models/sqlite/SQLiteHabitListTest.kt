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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import junit.framework.Assert.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.database.Database
import org.isoron.uhabits.core.database.Repository
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.ModelObservable
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.models.sqlite.records.HabitRecord
import org.isoron.uhabits.core.test.HabitFixtures
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.ArrayList

class SQLiteHabitListTest : BaseUnitTest() {
    @get:Rule
    var exception = ExpectedException.none()!!
    private lateinit var repository: Repository<HabitRecord>
    private var listener: ModelObservable.Listener = mock()
    private lateinit var habitsArray: ArrayList<Habit>
    private lateinit var activeHabits: HabitList
    private lateinit var reminderHabits: HabitList

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        val db: Database = buildMemoryDatabase()
        modelFactory = SQLModelFactory(db)
        habitList = SQLiteHabitList(modelFactory)
        fixtures = HabitFixtures(modelFactory, habitList)
        repository = Repository(HabitRecord::class.java, db)
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
                isReminderRequired = true,
            )
        )
        habitList.observable.addListener(listener)
    }

    @Throws(Exception::class)
    override fun tearDown() {
        habitList.observable.removeListener(listener)
        super.tearDown()
    }

    @Test
    fun testAdd_withDuplicate() {
        val habit = modelFactory.buildHabit()
        habitList.add(habit)
        verify(listener).onModelChange()
        exception.expect(IllegalArgumentException::class.java)
        habitList.add(habit)
    }

    @Test
    fun testAdd_withId() {
        val habit = modelFactory.buildHabit()
        habit.name = "Hello world with id"
        habit.id = 12300L
        habitList.add(habit)
        assertThat(habit.id, equalTo(12300L))
        val record = repository.find(12300L)
        assertThat(record!!.name, equalTo(habit.name))
    }

    @Test
    fun testAdd_withoutId() {
        val habit = modelFactory.buildHabit()
        habit.name = "Hello world"
        assertNull(habit.id)
        habitList.add(habit)
        val record = repository.find(habit.id!!)
        assertThat(record!!.name, equalTo(habit.name))
    }

    @Test
    fun testSize() {
        assertThat(habitList.size(), equalTo(10))
    }

    @Test
    fun testGetById() {
        val h1 = habitList.getById(1)!!
        assertThat(h1.name, equalTo("habit 1"))
        val h2 = habitList.getById(2)!!
        assertThat(h2, equalTo(h2))
    }

    @Test
    fun testGetById_withInvalid() {
        val invalidId = 9183792001L
        val h1 = habitList.getById(invalidId)
        assertNull(h1)
    }

    @Test
    fun testGetByPosition() {
        val h = habitList.getByPosition(4)
        assertThat(h.name, equalTo("habit 5"))
    }

    @Test
    fun testIndexOf() {
        val h1 = habitList.getByPosition(5)
        assertThat(habitList.indexOf(h1), equalTo(5))
        val h2 = modelFactory.buildHabit()
        assertThat(habitList.indexOf(h2), equalTo(-1))
        h2.id = 1000L
        assertThat(habitList.indexOf(h2), equalTo(-1))
    }

    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val h = habitList.getById(2)
        habitList.remove(h!!)
        assertThat(habitList.indexOf(h), equalTo(-1))

        var rec = repository.find(2L)
        assertNull(rec)
        rec = repository.find(3L)!!
        assertThat(rec.position, equalTo(1))
    }

    @Test
    fun testRemove_orderByName() {
        habitList.primaryOrder = HabitList.Order.BY_NAME_DESC
        val h = habitList.getById(2)
        habitList.remove(h!!)
        assertThat(habitList.indexOf(h), equalTo(-1))

        var rec = repository.find(2L)
        assertNull(rec)
        rec = repository.find(3L)!!
        assertThat(rec.position, equalTo(1))
    }

    @Test
    fun testReorder() {
        val habit3 = habitList.getById(3)!!
        val habit4 = habitList.getById(4)!!
        habitList.reorder(habit4, habit3)
        val record3 = repository.find(3L)!!
        assertThat(record3.position, equalTo(3))
        val record4 = repository.find(4L)!!
        assertThat(record4.position, equalTo(2))
    }
}
