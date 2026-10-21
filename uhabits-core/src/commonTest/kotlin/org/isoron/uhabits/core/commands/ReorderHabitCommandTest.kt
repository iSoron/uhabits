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
package org.isoron.uhabits.core.commands

import dev.mokkery.matcher.any
import dev.mokkery.spy
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import dev.mokkery.verify.VerifyMode.Companion.not as notCalled

class ReorderHabitCommandTest : BaseUnitTest() {
    private lateinit var habits: List<Habit>

    @BeforeTest
    override fun setUp() {
        super.setUp()
        habits = (0..4).map {
            val h = fixtures.createEmptyHabit()
            h.name = "habit $it"
            habitList.add(h)
            h
        }
    }

    private fun actualOrder(): List<Habit> {
        return (0 until habitList.size()).map { habitList.getByPosition(it) }
    }

    private fun before(refIdx: Int, habitIdx: Int): List<Habit> {
        val item = habits[habitIdx]
        val list = habits.toMutableList()
        list.remove(item)
        list.add(list.indexOf(habits[refIdx]), item)
        return list
    }

    private fun after(refIdx: Int, habitIdx: Int): List<Habit> {
        val item = habits[habitIdx]
        val list = habits.toMutableList()
        list.remove(item)
        list.add(list.indexOf(habits[refIdx]) + 1, item)
        return list
    }

    @Test
    fun testExecute_moveUp_before() {
        ReorderHabitCommand(habitList, habits[3], ReorderTarget.Before(habits[1])).run()
        assertContentEquals(before(1, 3), actualOrder())
    }

    @Test
    fun testExecute_moveDown_after() {
        ReorderHabitCommand(habitList, habits[1], ReorderTarget.After(habits[3])).run()
        assertContentEquals(after(3, 1), actualOrder())
    }

    @Test
    fun testExecute_toEnd_after() {
        ReorderHabitCommand(habitList, habits[0], ReorderTarget.After(habits[4])).run()
        assertContentEquals(after(4, 0), actualOrder())
    }

    @Test
    fun testExecute_toStart_before() {
        ReorderHabitCommand(habitList, habits[4], ReorderTarget.Before(habits[0])).run()
        assertContentEquals(before(0, 4), actualOrder())
    }

    @Test
    fun testExecute_renumbersPositions() {
        ReorderHabitCommand(habitList, habits[4], ReorderTarget.Before(habits[0])).run()
        (0 until habitList.size()).forEach { pos ->
            assertEquals(pos, habitList.getByPosition(pos).position)
        }
        assertContentEquals(listOf(4, 0, 1, 2, 3), actualOrder().map { it.id!!.toInt() })
    }

    @Test
    fun testExecute_performsSingleModelWrite() {
        val spiedBefore = spy(habitList)
        ReorderHabitCommand(spiedBefore, habits[3], ReorderTarget.Before(habits[1])).run()
        verify(exactly(1)) { spiedBefore.moveBefore(habits[3], habits[1]) }
        verify(notCalled) { spiedBefore.moveAfter(any(), any()) }

        val spiedAfter = spy(habitList)
        ReorderHabitCommand(spiedAfter, habits[0], ReorderTarget.After(habits[4])).run()
        verify(exactly(1)) { spiedAfter.moveAfter(habits[0], habits[4]) }
        verify(notCalled) { spiedAfter.moveBefore(any(), any()) }
    }

    @Test
    fun testNoJumpInvariant() {
        val working = habits.toMutableList()
        working.add(1, working.removeAt(4))
        assertEquals(listOf(0, 4, 1, 2, 3), working.map { it.id!!.toInt() })
        val draggedId = habits[4].id!!
        val k = working.indexOfFirst { it.id == draggedId }
        assertEquals(1, k)
        val target = ReorderTarget.After(working[k - 1])

        ReorderHabitCommand(habitList, habits[4], target).run()

        assertContentEquals(working, actualOrder())
        assertEquals(HabitList.Order.BY_POSITION, habitList.primaryOrder)
    }

    @Test
    fun testFilteredDrag_referenceBasedTarget() {
        val list = modelFactory.buildHabitList()
        val full = (0..9).map { idx ->
            val h = fixtures.createEmptyHabit()
            h.name = "h$idx"
            list.add(h)
            h
        }
        val hidden = setOf(0, 1, 4, 7)
        hidden.forEach { full[it].isArchived = true }

        fun fullOrder(): List<Int> = (0 until list.size()).map { list.getByPosition(it).id!!.toInt() }
        fun visibleIds(): List<Int> = list.getFiltered(HabitMatcher()).toList().map { it.id!!.toInt() }

        assertEquals(listOf(2, 3, 5, 6, 8, 9), visibleIds())

        val draggedId = 5
        val v = visibleIds().toMutableList()
        v.add(4, v.removeAt(v.indexOf(draggedId)))
        assertEquals(listOf(2, 3, 6, 8, 5, 9), v)

        val k = v.indexOf(draggedId)
        assertEquals(4, k)
        val target: ReorderTarget = if (k > 0) {
            ReorderTarget.After(full[v[k - 1]])
        } else {
            ReorderTarget.Before(full[v[1]])
        }
        assertTrue(target is ReorderTarget.After)
        assertEquals(8, target.ref.id!!.toInt())

        ReorderHabitCommand(list, full[draggedId], target).run()

        assertEquals(listOf(2, 3, 6, 8, 5, 9), visibleIds())
        assertEquals(listOf(0, 1, 2, 3, 4, 6, 7, 8, 5, 9), fullOrder())
        assertEquals(listOf(0, 1, 4, 7), fullOrder().filter { it in hidden })
    }
}
