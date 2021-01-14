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

import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.database.Database
import org.isoron.uhabits.core.database.Repository
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcherBuilder
import org.isoron.uhabits.core.models.ModelObservable
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.models.sqlite.records.HabitRecord
import org.isoron.uhabits.core.test.HabitFixtures
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mockito
import java.util.ArrayList

class SQLiteHabitListTest : BaseUnitTest() {
    @get:Rule
    var exception = ExpectedException.none()!!
    private lateinit var repository: Repository<HabitRecord>
    private lateinit var listener: ModelObservable.Listener
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
        repository = Repository(
            HabitRecord::class.java,
            db
        )
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
        activeHabits = habitList.getFiltered(HabitMatcherBuilder().build())
        reminderHabits = habitList.getFiltered(
            HabitMatcherBuilder()
                .setArchivedAllowed(true)
                .setReminderRequired(true)
                .build()
        )
        listener = Mockito.mock(ModelObservable.Listener::class.java)
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
        Mockito.verify(listener)!!.onModelChange()
        exception.expect(IllegalArgumentException::class.java)
        habitList.add(habit)
    }

    @Test
    fun testAdd_withId() {
        val habit = modelFactory.buildHabit()
        habit.name = "Hello world with id"
        habit.id = 12300L
        habitList.add(habit)
        assertThat(habit.id, CoreMatchers.equalTo(12300L))
        val record = repository.find(12300L)
        assertNotNull(record)
        assertThat(record!!.name, CoreMatchers.equalTo(habit.name))
    }

    @Test
    fun testAdd_withoutId() {
        val habit = modelFactory.buildHabit()
        habit.name = "Hello world"
        assertNull(habit.id)
        habitList.add(habit)
        assertNotNull(habit.id)
        val record = repository.find(
            habit.id!!
        )
        assertNotNull(record)
        assertThat(record!!.name, CoreMatchers.equalTo(habit.name))
    }

    @Test
    fun testSize() {
        assertThat(habitList.size(), CoreMatchers.equalTo(10))
    }

    @Test
    fun testGetById() {
        val h1 = habitList.getById(1)
        assertNotNull(h1)
        assertThat(h1!!.name, CoreMatchers.equalTo("habit 1"))
        val h2 = habitList.getById(2)
        assertNotNull(h2)
        assertThat(h2, CoreMatchers.equalTo(h2))
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
        assertNotNull(h)
        assertThat(h.name, CoreMatchers.equalTo("habit 5"))
    }

    @Test
    fun testIndexOf() {
        val h1 = habitList.getByPosition(5)
        assertNotNull(h1)
        assertThat(habitList.indexOf(h1), CoreMatchers.equalTo(5))
        val h2 = modelFactory.buildHabit()
        assertThat(habitList.indexOf(h2), CoreMatchers.equalTo(-1))
        h2.id = 1000L
        assertThat(habitList.indexOf(h2), CoreMatchers.equalTo(-1))
    }

    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val h = habitList.getById(2)
        habitList.remove(h!!)
        assertThat(habitList.indexOf(h), CoreMatchers.equalTo(-1))
        var rec = repository.find(2L)
        assertNull(rec)
        rec = repository.find(3L)
        assertNotNull(rec)
        assertThat(rec!!.position, CoreMatchers.equalTo(1))
    }

    @Test
    fun testReorder() {
        val habit3 = habitList.getById(3)
        val habit4 = habitList.getById(4)
        assertNotNull(habit3)
        assertNotNull(habit4)
        habitList.reorder(habit4!!, habit3!!)
        val record3 = repository.find(3L)
        assertNotNull(record3)
        assertThat(record3!!.position, CoreMatchers.equalTo(3))
        val record4 = repository.find(4L)
        assertNotNull(record4)
        assertThat(record4!!.position, CoreMatchers.equalTo(2))
    }
}
