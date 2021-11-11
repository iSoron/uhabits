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

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.io.IOException
import java.io.StringWriter
import java.util.ArrayList

class HabitListTest : BaseUnitTest() {
    @get:Rule
    var thrown = ExpectedException.none()!!
    private lateinit var habitsArray: ArrayList<Habit>
    private lateinit var activeHabits: HabitList
    private lateinit var reminderHabits: HabitList

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habitsArray = ArrayList()
        for (i in 0..9) {
            val habit = fixtures.createEmptyHabit()
            habitList.add(habit)
            habitsArray.add(habit)
            if (i % 3 == 0) habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        }
        habitsArray[0].isArchived = true
        habitsArray[1].isArchived = true
        habitsArray[4].isArchived = true
        habitsArray[7].isArchived = true
        activeHabits = habitList.getFiltered(HabitMatcher())
        reminderHabits = habitList.getFiltered(
            HabitMatcher(
                isArchivedAllowed = true,
                isReminderRequired = true,
            )
        )
    }

    @Test
    fun testSize() {
        assertThat(habitList.size(), equalTo(10))
        assertThat(activeHabits.size(), equalTo(6))
        assertThat(reminderHabits.size(), equalTo(4))
    }

    @Test
    fun testGetByPosition() {
        assertThat(habitList.getByPosition(0), equalTo(habitsArray[0]))
        assertThat(habitList.getByPosition(3), equalTo(habitsArray[3]))
        assertThat(habitList.getByPosition(9), equalTo(habitsArray[9]))
        assertThat(activeHabits.getByPosition(0), equalTo(habitsArray[2]))
        assertThat(reminderHabits.getByPosition(1), equalTo(habitsArray[3]))
    }

    @Test
    fun testGetById() {
        val habit1 = habitsArray[0]
        val habit2 = habitList.getById(habit1.id!!)
        assertThat(habit1, equalTo(habit2))
    }

    @Test
    fun testGetById_withInvalidId() {
        assertNull(habitList.getById(100L))
    }

    @Test
    fun testOrdering() {
        val h1 = fixtures.createEmptyHabit("A Habit", PaletteColor(2), 1)
        val h2 = fixtures.createEmptyHabit("B Habit", PaletteColor(2), 3)
        val h3 = fixtures.createEmptyHabit("C Habit", PaletteColor(0), 0)
        val h4 = fixtures.createEmptyHabit("D Habit", PaletteColor(1), 2)

        val list = modelFactory.buildHabitList().apply {
            add(h3)
            add(h1)
            add(h4)
            add(h2)
        }

        list.primaryOrder = HabitList.Order.BY_POSITION
        assertThat(list.getByPosition(0), equalTo(h3))
        assertThat(list.getByPosition(1), equalTo(h1))
        assertThat(list.getByPosition(2), equalTo(h4))
        assertThat(list.getByPosition(3), equalTo(h2))
        list.primaryOrder = HabitList.Order.BY_NAME_DESC
        assertThat(list.getByPosition(0), equalTo(h4))
        assertThat(list.getByPosition(1), equalTo(h3))
        assertThat(list.getByPosition(2), equalTo(h2))
        assertThat(list.getByPosition(3), equalTo(h1))
        list.primaryOrder = HabitList.Order.BY_NAME_ASC
        assertThat(list.getByPosition(0), equalTo(h1))
        assertThat(list.getByPosition(1), equalTo(h2))
        assertThat(list.getByPosition(2), equalTo(h3))
        assertThat(list.getByPosition(3), equalTo(h4))
        list.primaryOrder = HabitList.Order.BY_NAME_ASC
        list.remove(h1)
        list.add(h1)
        assertThat(list.getByPosition(0), equalTo(h1))
        list.primaryOrder = HabitList.Order.BY_COLOR_ASC
        list.secondaryOrder = HabitList.Order.BY_NAME_ASC
        assertThat(list.getByPosition(0), equalTo(h3))
        assertThat(list.getByPosition(1), equalTo(h4))
        assertThat(list.getByPosition(2), equalTo(h1))
        assertThat(list.getByPosition(3), equalTo(h2))
        list.primaryOrder = HabitList.Order.BY_COLOR_DESC
        list.secondaryOrder = HabitList.Order.BY_NAME_ASC
        assertThat(list.getByPosition(0), equalTo(h1))
        assertThat(list.getByPosition(1), equalTo(h2))
        assertThat(list.getByPosition(2), equalTo(h4))
        assertThat(list.getByPosition(3), equalTo(h3))
        list.primaryOrder = HabitList.Order.BY_POSITION
        assertThat(list.getByPosition(0), equalTo(h3))
        assertThat(list.getByPosition(1), equalTo(h1))
        assertThat(list.getByPosition(2), equalTo(h4))
        assertThat(list.getByPosition(3), equalTo(h2))
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
            val fromHabit = habitsArray[operations[i][0]]
            val toHabit = habitsArray[operations[i][1]]
            habitList.reorder(fromHabit, toHabit)
            val actualSequence = IntArray(10)
            for (j in 0..9) {
                val habit = habitList.getByPosition(j)
                assertThat(habit.position, equalTo(j))
                actualSequence[j] = Math.toIntExact(habit.id!!)
            }
            assertThat(actualSequence, equalTo(expectedSequence[i]))
        }
        assertThat(activeHabits.indexOf(habitsArray[5]), equalTo(0))
        assertThat(activeHabits.indexOf(habitsArray[2]), equalTo(1))
    }

    @Test
    @Throws(Exception::class)
    fun testReorder_withInvalidArguments() {
        val h1 = habitsArray[0]
        val h2 = fixtures.createEmptyHabit()
        thrown.expect(IllegalArgumentException::class.java)
        habitList.reorder(h1, h2)
    }

    @Test
    fun testOrder_inherit() {
        habitList.primaryOrder = HabitList.Order.BY_COLOR_ASC
        val filteredList = habitList.getFiltered(
            HabitMatcher(
                isArchivedAllowed = false,
                isCompletedAllowed = false,
            )
        )
        assertEquals(filteredList.primaryOrder, HabitList.Order.BY_COLOR_ASC)
    }

    @Test
    @Throws(IOException::class)
    fun testWriteCSV() {
        val list = modelFactory.buildHabitList()
        val h1 = fixtures.createEmptyHabit()
        h1.name = "Meditate"
        h1.question = "Did you meditate this morning?"
        h1.description = "this is a test description"
        h1.frequency = Frequency.DAILY
        h1.color = PaletteColor(3)
        val h2 = fixtures.createEmptyHabit()
        h2.name = "Wake up early"
        h2.question = "Did you wake up before 6am?"
        h2.description = ""
        h2.frequency = Frequency(2, 3)
        h2.color = PaletteColor(5)
        list.add(h1)
        list.add(h2)
        val expectedCSV =
            """
            Position,Name,Question,Description,NumRepetitions,Interval,Color
            001,Meditate,Did you meditate this morning?,this is a test description,1,1,#FF8F00
            002,Wake up early,Did you wake up before 6am?,,2,3,#AFB42B
            
            """.trimIndent()
        val writer = StringWriter()
        list.writeCSV(writer)
        assertThat(writer.toString(), equalTo(expectedCSV))
    }

    @Test
    @Throws(Exception::class)
    fun testAdd() {
        val h1 = fixtures.createEmptyHabit()
        assertFalse(h1.isArchived)
        assertNull(h1.id)
        assertThat(habitList.indexOf(h1), equalTo(-1))
        habitList.add(h1)
        h1.id!!
        assertThat(habitList.indexOf(h1), not(equalTo(-1)))
        assertThat(activeHabits.indexOf(h1), not(equalTo(-1)))
    }

    @Test
    @Throws(Exception::class)
    fun testAdd_withFilteredList() {
        thrown.expect(IllegalStateException::class.java)
        activeHabits.add(fixtures.createEmptyHabit())
    }

    @Test
    @Throws(Exception::class)
    fun testRemove_onFilteredList() {
        thrown.expect(IllegalStateException::class.java)
        activeHabits.remove(fixtures.createEmptyHabit())
    }

    @Test
    @Throws(Exception::class)
    fun testReorder_onFilteredList() {
        val h1 = fixtures.createEmptyHabit()
        val h2 = fixtures.createEmptyHabit()
        thrown.expect(IllegalStateException::class.java)
        activeHabits.reorder(h1, h2)
    }

    @Test
    @Throws(Exception::class)
    fun testReorder_onSortedList() {
        habitList.primaryOrder = HabitList.Order.BY_SCORE_DESC
        val h1 = habitsArray[1]
        val h2 = habitsArray[2]
        thrown.expect(IllegalStateException::class.java)
        habitList.reorder(h1, h2)
    }
}
