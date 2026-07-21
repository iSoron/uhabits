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
package org.isoron.uhabits.core.models

import org.isoron.uhabits.core.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class HabitListTest : BaseUnitTest() {
    private lateinit var habitsArray: ArrayList<Habit>
    private lateinit var activeHabits: HabitList
    private lateinit var reminderHabits: HabitList

    @BeforeTest
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
                isReminderRequired = true
            )
        )
    }

    @Test
    fun testSize() {
        assertEquals(10, habitList.size())
        assertEquals(6, activeHabits.size())
        assertEquals(4, reminderHabits.size())
    }

    @Test
    fun testGetByPosition() {
        assertEquals(habitsArray[0], habitList.getByPosition(0))
        assertEquals(habitsArray[3], habitList.getByPosition(3))
        assertEquals(habitsArray[9], habitList.getByPosition(9))
        assertEquals(habitsArray[2], activeHabits.getByPosition(0))
        assertEquals(habitsArray[3], reminderHabits.getByPosition(1))
    }

    @Test
    fun testGetById() {
        val habit1 = habitsArray[0]
        val habit2 = habitList.getById(habit1.id!!)
        assertEquals(habit1, habit2)
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
        assertEquals(h3, list.getByPosition(0))
        assertEquals(h1, list.getByPosition(1))
        assertEquals(h4, list.getByPosition(2))
        assertEquals(h2, list.getByPosition(3))
        list.primaryOrder = HabitList.Order.BY_NAME_DESC
        assertEquals(h4, list.getByPosition(0))
        assertEquals(h3, list.getByPosition(1))
        assertEquals(h2, list.getByPosition(2))
        assertEquals(h1, list.getByPosition(3))
        list.primaryOrder = HabitList.Order.BY_NAME_ASC
        assertEquals(h1, list.getByPosition(0))
        assertEquals(h2, list.getByPosition(1))
        assertEquals(h3, list.getByPosition(2))
        assertEquals(h4, list.getByPosition(3))
        list.primaryOrder = HabitList.Order.BY_NAME_ASC
        list.remove(h1)
        list.add(h1)
        assertEquals(h1, list.getByPosition(0))
        list.primaryOrder = HabitList.Order.BY_COLOR_ASC
        list.secondaryOrder = HabitList.Order.BY_NAME_ASC
        assertEquals(h3, list.getByPosition(0))
        assertEquals(h4, list.getByPosition(1))
        assertEquals(h1, list.getByPosition(2))
        assertEquals(h2, list.getByPosition(3))
        list.primaryOrder = HabitList.Order.BY_COLOR_DESC
        list.secondaryOrder = HabitList.Order.BY_NAME_ASC
        assertEquals(h1, list.getByPosition(0))
        assertEquals(h2, list.getByPosition(1))
        assertEquals(h4, list.getByPosition(2))
        assertEquals(h3, list.getByPosition(3))
        list.primaryOrder = HabitList.Order.BY_POSITION
        assertEquals(h3, list.getByPosition(0))
        assertEquals(h1, list.getByPosition(1))
        assertEquals(h4, list.getByPosition(2))
        assertEquals(h2, list.getByPosition(3))
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
                assertEquals(j, habit.position)
                actualSequence[j] = habit.id!!.toInt()
            }
            assertContentEquals(expectedSequence[i], actualSequence)
        }
        assertEquals(0, activeHabits.indexOf(habitsArray[5]))
        assertEquals(1, activeHabits.indexOf(habitsArray[2]))
    }

    @Test
    fun testReorder_withInvalidArguments() {
        val h1 = habitsArray[0]
        val h2 = fixtures.createEmptyHabit()
        assertFailsWith<IllegalArgumentException> {
            habitList.reorder(h1, h2)
        }
    }

    @Test
    fun testOrder_inherit() {
        habitList.primaryOrder = HabitList.Order.BY_COLOR_ASC
        val filteredList = habitList.getFiltered(
            HabitMatcher(
                isArchivedAllowed = false,
                isCompletedAllowed = false
            )
        )
        assertEquals(filteredList.primaryOrder, HabitList.Order.BY_COLOR_ASC)
    }

    @Test
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
        val h3 = fixtures.createNumericalHabit()
        list.add(h1)
        list.add(h2)
        list.add(h3)
        val expectedCSV =
            """
            Position,Name,Type,Question,Description,FrequencyNumerator,FrequencyDenominator,FrequencyMode,Color,Unit,Target Type,Target Value,Archived?
            001,Meditate,YES_NO,Did you meditate this morning?,this is a test description,1,1,DAYS,#FF8F00,,,,false
            002,Run,NUMERICAL,How many miles did you run today?,,1,1,DAYS,#E64A19,miles,AT_LEAST,2.0,false
            003,Wake up early,YES_NO,Did you wake up before 6am?,,2,3,DAYS,#AFB42B,,,,false

            """.trimIndent()
        assertEquals(expectedCSV, list.writeCSV())
    }

    @Test
    fun testAdd() {
        val h1 = fixtures.createEmptyHabit()
        assertFalse(h1.isArchived)
        assertNull(h1.id)
        assertEquals(-1, habitList.indexOf(h1))
        habitList.add(h1)
        h1.id!!
        assertNotEquals(-1, habitList.indexOf(h1))
        assertNotEquals(-1, activeHabits.indexOf(h1))
    }

    @Test
    fun testAdd_withFilteredList() {
        assertFailsWith<IllegalStateException> {
            activeHabits.add(fixtures.createEmptyHabit())
        }
    }

    @Test
    fun testRemove_onFilteredList() {
        assertFailsWith<IllegalStateException> {
            activeHabits.remove(fixtures.createEmptyHabit())
        }
    }

    @Test
    fun testReorder_onFilteredList() {
        val h1 = fixtures.createEmptyHabit()
        val h2 = fixtures.createEmptyHabit()
        assertFailsWith<IllegalStateException> {
            activeHabits.reorder(h1, h2)
        }
    }

    @Test
    fun testReorder_onSortedList() {
        habitList.primaryOrder = HabitList.Order.BY_SCORE_DESC
        val h1 = habitsArray[1]
        val h2 = habitsArray[2]
        assertFailsWith<IllegalStateException> {
            habitList.reorder(h1, h2)
        }
    }
}
