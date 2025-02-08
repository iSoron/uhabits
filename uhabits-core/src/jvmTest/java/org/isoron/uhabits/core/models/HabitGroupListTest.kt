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
package org.isoron.uhabits.core.models

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.io.StringWriter
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class HabitGroupListTest : BaseUnitTest() {
    private lateinit var habitGroupArray: ArrayList<HabitGroup>
    private lateinit var activeHabitGroups: HabitGroupList
    private lateinit var reminderHabitGroups: HabitGroupList

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habitGroupArray = ArrayList()
        for (i in 0..9) {
            val hgr = groupFixtures.createEmptyHabitGroup()
            habitGroupList.add(hgr)
            habitGroupArray.add(hgr)
            if (i % 3 == 0) hgr.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        }
        habitGroupArray[0].isArchived = true
        habitGroupArray[1].isArchived = true
        habitGroupArray[4].isArchived = true
        habitGroupArray[7].isArchived = true
        activeHabitGroups = habitGroupList.getFiltered(HabitMatcher())
        reminderHabitGroups = habitGroupList.getFiltered(
            HabitMatcher(
                isArchivedAllowed = true,
                isReminderRequired = true
            )
        )
    }

    @Test
    fun testSize() {
        assertThat(habitGroupList.size(), equalTo(10))
        assertThat(activeHabitGroups.size(), equalTo(6))
        assertThat(reminderHabitGroups.size(), equalTo(4))
    }

    @Test
    fun testGetByPosition() {
        assertThat(habitGroupList.getByPosition(0), equalTo(habitGroupArray[0]))
        assertThat(habitGroupList.getByPosition(3), equalTo(habitGroupArray[3]))
        assertThat(habitGroupList.getByPosition(9), equalTo(habitGroupArray[9]))
        assertThat(activeHabitGroups.getByPosition(0), equalTo(habitGroupArray[2]))
        assertThat(reminderHabitGroups.getByPosition(1), equalTo(habitGroupArray[3]))
    }

    @Test
    fun testGetById() {
        val hgr1 = habitGroupArray[0]
        val hgr2 = habitGroupList.getById(hgr1.id!!)
        assertThat(hgr1, equalTo(hgr2))
    }

    @Test
    fun testGetById_withInvalidId() {
        assertNull(habitGroupList.getById(100L))
    }

    @Test
    fun testOrdering() {
        val hgr1 = groupFixtures.createEmptyHabitGroup("A Habit Group", PaletteColor(2), 1)
        val hgr2 = groupFixtures.createEmptyHabitGroup("B Habit Group", PaletteColor(2), 3)
        val hgr3 = groupFixtures.createEmptyHabitGroup("C Habit Group", PaletteColor(0), 0)
        val hgr4 = groupFixtures.createEmptyHabitGroup("D Habit Group", PaletteColor(1), 2)

        val list = modelFactory.buildHabitGroupList().apply {
            add(hgr3)
            add(hgr1)
            add(hgr4)
            add(hgr2)
        }

        list.primaryOrder = HabitList.Order.BY_POSITION
        assertThat(list.getByPosition(0), equalTo(hgr3))
        assertThat(list.getByPosition(1), equalTo(hgr1))
        assertThat(list.getByPosition(2), equalTo(hgr4))
        assertThat(list.getByPosition(3), equalTo(hgr2))
        list.primaryOrder = HabitList.Order.BY_NAME_DESC
        assertThat(list.getByPosition(0), equalTo(hgr4))
        assertThat(list.getByPosition(1), equalTo(hgr3))
        assertThat(list.getByPosition(2), equalTo(hgr2))
        assertThat(list.getByPosition(3), equalTo(hgr1))
        list.primaryOrder = HabitList.Order.BY_NAME_ASC
        assertThat(list.getByPosition(0), equalTo(hgr1))
        assertThat(list.getByPosition(1), equalTo(hgr2))
        assertThat(list.getByPosition(2), equalTo(hgr3))
        assertThat(list.getByPosition(3), equalTo(hgr4))
        list.primaryOrder = HabitList.Order.BY_NAME_ASC
        list.remove(hgr1)
        list.add(hgr1)
        assertThat(list.getByPosition(0), equalTo(hgr1))
        list.primaryOrder = HabitList.Order.BY_COLOR_ASC
        list.secondaryOrder = HabitList.Order.BY_NAME_ASC
        assertThat(list.getByPosition(0), equalTo(hgr3))
        assertThat(list.getByPosition(1), equalTo(hgr4))
        assertThat(list.getByPosition(2), equalTo(hgr1))
        assertThat(list.getByPosition(3), equalTo(hgr2))
        list.primaryOrder = HabitList.Order.BY_COLOR_DESC
        list.secondaryOrder = HabitList.Order.BY_NAME_ASC
        assertThat(list.getByPosition(0), equalTo(hgr1))
        assertThat(list.getByPosition(1), equalTo(hgr2))
        assertThat(list.getByPosition(2), equalTo(hgr4))
        assertThat(list.getByPosition(3), equalTo(hgr3))
        list.primaryOrder = HabitList.Order.BY_POSITION
        assertThat(list.getByPosition(0), equalTo(hgr3))
        assertThat(list.getByPosition(1), equalTo(hgr1))
        assertThat(list.getByPosition(2), equalTo(hgr4))
        assertThat(list.getByPosition(3), equalTo(hgr2))
    }

    @Test
    fun testReorder() {
        val operations =
            arrayOf(intArrayOf(5, 2), intArrayOf(3, 7), intArrayOf(4, 4), intArrayOf(8, 3))
        val expectedSequence = arrayOf(
            intArrayOf(0, 1, 5, 2, 3, 4, 6, 7, 8, 9),
            intArrayOf(0, 1, 5, 2, 4, 6, 7, 3, 8, 9),
            intArrayOf(0, 1, 5, 2, 4, 6, 7, 3, 8, 9),
            intArrayOf(0, 1, 5, 2, 4, 6, 7, 8, 3, 9)
        )
        for (i in operations.indices) {
            val fromHabitGroup = habitGroupArray[operations[i][0]]
            val toHabitGroup = habitGroupArray[operations[i][1]]
            habitGroupList.reorder(fromHabitGroup, toHabitGroup)
            val actualSequence = IntArray(10)
            for (j in 0..9) {
                val hgr = habitGroupList.getByPosition(j)
                assertThat(hgr.position, equalTo(j))
                actualSequence[j] = Math.toIntExact(hgr.id!!)
            }
            assertThat(actualSequence, equalTo(expectedSequence[i]))
        }
        assertThat(activeHabitGroups.indexOf(habitGroupArray[5]), equalTo(0))
        assertThat(activeHabitGroups.indexOf(habitGroupArray[2]), equalTo(1))
    }

    @Test
    @Throws(Exception::class)
    fun testReorder_withInvalidArguments() {
        val hgr1 = habitGroupArray[0]
        val hgr2 = groupFixtures.createEmptyHabitGroup()
        assertThrows(IllegalArgumentException::class.java) {
            habitGroupList.reorder(hgr1, hgr2)
        }
    }

    @Test
    fun testOrder_inherit() {
        habitGroupList.primaryOrder = HabitList.Order.BY_COLOR_ASC
        val filteredList = habitGroupList.getFiltered(
            HabitMatcher(
                isArchivedAllowed = false,
                isCompletedAllowed = false
            )
        )
        assertEquals(filteredList.primaryOrder, HabitList.Order.BY_COLOR_ASC)
    }

    @Test
    @Throws(IOException::class)
    fun testWriteCSV() {
        val list = modelFactory.buildHabitGroupList()
        val hgr1 = groupFixtures.createEmptyHabitGroup()
        hgr1.name = "Meditate"
        hgr1.question = "Did you meditate this morning?"
        hgr1.description = "this is a test description"
        hgr1.color = PaletteColor(3)
        val hgr2 = groupFixtures.createEmptyHabitGroup()
        hgr2.name = "Wake up early"
        hgr2.question = "Did you wake up before 6am?"
        hgr2.description = ""
        hgr2.color = PaletteColor(5)
        list.add(hgr1)
        list.add(hgr2)
        val expectedCSV =
            """
            Position,Name,Question,Description,Color
            001,Meditate,Did you meditate this morning?,this is a test description,#FF8F00
            002,Wake up early,Did you wake up before 6am?,,#AFB42B
            
            """.trimIndent()
        val writer = StringWriter()
        list.writeCSV(writer)
        assertThat(writer.toString(), equalTo(expectedCSV))
    }

    @Test
    @Throws(Exception::class)
    fun testAdd() {
        val hgr1 = groupFixtures.createEmptyHabitGroup()
        assertFalse(hgr1.isArchived)
        assertNull(hgr1.id)
        assertThat(habitGroupList.indexOf(hgr1), equalTo(-1))
        habitGroupList.add(hgr1)
        hgr1.id!!
        assertThat(habitGroupList.indexOf(hgr1), not(equalTo(-1)))
        assertThat(activeHabitGroups.indexOf(hgr1), not(equalTo(-1)))
    }

    @Test
    @Throws(Exception::class)
    fun testAdd_withFilteredList() {
        assertThrows(IllegalStateException::class.java) {
            activeHabitGroups.add(groupFixtures.createEmptyHabitGroup())
        }
    }

    @Test
    @Throws(Exception::class)
    fun testRemove_onFilteredList() {
        assertThrows(IllegalStateException::class.java) {
            activeHabitGroups.remove(groupFixtures.createEmptyHabitGroup())
        }
    }

    @Test
    @Throws(Exception::class)
    fun testReorder_onFilteredList() {
        val hgr1 = groupFixtures.createEmptyHabitGroup()
        val hgr2 = groupFixtures.createEmptyHabitGroup()
        assertThrows(IllegalStateException::class.java) {
            activeHabitGroups.reorder(hgr1, hgr2)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testReorder_onSortedList() {
        habitGroupList.primaryOrder = HabitList.Order.BY_SCORE_DESC
        val hgr1 = habitGroupArray[1]
        val hgr2 = habitGroupArray[2]
        assertThrows(IllegalStateException::class.java) {
            habitGroupList.reorder(hgr1, hgr2)
        }
    }
}
