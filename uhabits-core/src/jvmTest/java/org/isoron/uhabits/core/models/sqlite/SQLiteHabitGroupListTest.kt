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

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.database.Database
import org.isoron.uhabits.core.database.Repository
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.ModelObservable
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.models.sqlite.records.HabitGroupRecord
import org.isoron.uhabits.core.test.HabitGroupFixtures
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.assertNull

class SQLiteHabitGroupListTest : BaseUnitTest() {
    private lateinit var repository: Repository<HabitGroupRecord>
    private var listener: ModelObservable.Listener = mock()
    private lateinit var habitGroupsArray: ArrayList<HabitGroup>
    private lateinit var activeHabitGroups: HabitGroupList
    private lateinit var reminderHabitGroups: HabitGroupList

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        val db: Database = buildMemoryDatabase()
        modelFactory = SQLModelFactory(db)
        habitGroupList = SQLiteHabitGroupList(modelFactory)
        groupFixtures = HabitGroupFixtures(modelFactory, habitList, habitGroupList)
        repository = Repository(HabitGroupRecord::class.java, db)
        habitGroupsArray = ArrayList()
        for (i in 0..9) {
            val hgr = groupFixtures.createEmptyHabitGroup()
            hgr.name = "habit group " + (i + 1)
            habitGroupList.update(hgr)
            habitGroupsArray.add(hgr)
            if (i % 3 == 0) hgr.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        }
        habitGroupsArray[0].isArchived = true
        habitGroupsArray[1].isArchived = true
        habitGroupsArray[4].isArchived = true
        habitGroupsArray[7].isArchived = true
        habitGroupList.update(habitGroupsArray)
        activeHabitGroups = habitGroupList.getFiltered(HabitMatcher())
        reminderHabitGroups = habitGroupList.getFiltered(
            HabitMatcher(
                isArchivedAllowed = true,
                isReminderRequired = true
            )
        )
        habitGroupList.observable.addListener(listener)
    }

    @Throws(Exception::class)
    override fun tearDown() {
        habitGroupList.observable.removeListener(listener)
        super.tearDown()
    }

    @Test
    fun testAdd_withDuplicate() {
        val hgr = modelFactory.buildHabitGroup()
        habitGroupList.add(hgr)
        verify(listener).onModelChange()
        assertThrows(IllegalArgumentException::class.java) {
            habitGroupList.add(hgr)
        }
    }

    @Test
    fun testAdd_withId() {
        val hgr = modelFactory.buildHabitGroup()
        hgr.name = "Hello world with id"
        hgr.id = 12300L
        habitGroupList.add(hgr)
        assertThat(hgr.id, equalTo(11L))
        val record = repository.find(11L)
        assertThat(record!!.name, equalTo(hgr.name))
    }

    @Test
    fun testAdd_withoutId() {
        val hgr = modelFactory.buildHabitGroup()
        hgr.name = "Hello world"
        assertNull(hgr.id)
        habitGroupList.add(hgr)
        val record = repository.find(hgr.id!!)
        assertThat(record!!.name, equalTo(hgr.name))
    }

    @Test
    fun testSize() {
        assertThat(habitGroupList.size(), equalTo(10))
    }

    @Test
    fun testGetById() {
        val hgr1 = habitGroupList.getById(1)!!
        assertThat(hgr1.name, equalTo("habit group 1"))
        val hgr2 = habitGroupList.getById(2)!!
        assertThat(hgr2, equalTo(hgr2))
    }

    @Test
    fun testGetById_withInvalid() {
        val invalidId = 9183792001L
        val hgr1 = habitGroupList.getById(invalidId)
        assertNull(hgr1)
    }

    @Test
    fun testGetByPosition() {
        val hgr = habitGroupList.getByPosition(4)
        assertThat(hgr.name, equalTo("habit group 5"))
    }

    @Test
    fun testIndexOf() {
        val hgr1 = habitGroupList.getByPosition(5)
        assertThat(habitGroupList.indexOf(hgr1), equalTo(5))
        val hgr2 = modelFactory.buildHabitGroup()
        assertThat(habitGroupList.indexOf(hgr2), equalTo(-1))
        hgr2.id = 1000L
        assertThat(habitGroupList.indexOf(hgr2), equalTo(-1))
    }

    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val hgr = habitGroupList.getById(2)
        habitGroupList.remove(hgr!!)
        assertThat(habitGroupList.indexOf(hgr), equalTo(-1))

        var rec = repository.find(2L)
        assertNull(rec)
        rec = repository.find(3L)!!
        assertThat(rec.position, equalTo(1))
    }

    @Test
    fun testRemove_orderByName() {
        habitGroupList.primaryOrder = HabitList.Order.BY_NAME_DESC
        val hgr = habitGroupList.getById(2)
        habitGroupList.remove(hgr!!)
        assertThat(habitGroupList.indexOf(hgr), equalTo(-1))

        var rec = repository.find(2L)
        assertNull(rec)
        rec = repository.find(3L)!!
        assertThat(rec.position, equalTo(1))
    }

    @Test
    fun testReorder() {
        val hgr3 = habitGroupList.getById(3)!!
        val hgr4 = habitGroupList.getById(4)!!
        habitGroupList.reorder(hgr4, hgr3)
        val record3 = repository.find(3L)!!
        assertThat(record3.position, equalTo(3))
        val record4 = repository.find(4L)!!
        assertThat(record4.position, equalTo(2))
    }
}
