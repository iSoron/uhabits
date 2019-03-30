/*
 * Copyright (C) 2017 Álinson Santos Xavier
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
 * with this program. If not, see .
 */

package org.isoron.uhabits.activities.habits.list.controllers

import dagger.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.habits.list.*
import org.isoron.uhabits.activities.habits.list.views.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import org.junit.*
import org.mockito.*
import org.mockito.Mockito.*
import java.util.*

class HabitCardListControllerTest : BaseAndroidJVMTest() {
    private val habits = LinkedList<Habit>()
    private lateinit var controller: HabitCardListController

    @Mock lateinit var adapter: HabitCardListAdapter
    @Mock lateinit var behavior: ListHabitsBehavior
    @Mock lateinit var selectionMenu: ListHabitsSelectionMenu

    @Before
    override fun setUp() {
        super.setUp()
        `when`(adapter.observable).thenReturn(ModelObservable())
        controller = HabitCardListController(adapter,
                                             behavior,
                                             Lazy { selectionMenu })

        repeat(10) { habits.add(fixtures.createEmptyHabit()) }
        for(i in 0..9) `when`(adapter.getItem(i)).thenReturn(habits[i])

    }

    @Test
    fun testClick_withSelection() {
        controller.onItemLongClick(0)
        verify(adapter).toggleSelection(0)
        verify(selectionMenu).onSelectionStart()
        reset(adapter, selectionMenu)

        controller.onItemClick(1)
        verify(adapter).toggleSelection(1)
        verify(selectionMenu).onSelectionChange()
        reset(adapter, selectionMenu)

        controller.onItemClick(1)
        verify(adapter).toggleSelection(1)
        verify(selectionMenu).onSelectionChange()
        reset(adapter, selectionMenu)

        doReturn(true).`when`(adapter).isSelectionEmpty
        controller.onItemClick(0)
        verify(adapter).toggleSelection(0)
        verify(selectionMenu).onSelectionFinish()
    }

    @Test
    fun testClick_withoutSelection() {
        controller.onItemClick(0)
        verify(behavior).onClickHabit(habits[0])
    }

    @Test
    fun testDragAndDrop_withSelection() {
        controller.onItemLongClick(0)
        verify(adapter).toggleSelection(0)
        verify(selectionMenu).onSelectionStart()

        controller.startDrag(1)
        verify(selectionMenu).onSelectionChange()
        verify(adapter).toggleSelection(1)

        controller.drop(1, 3)
        verify(behavior).onReorderHabit(habits[1], habits[3])
        verify(selectionMenu).onSelectionFinish()
        verify(adapter).performReorder(1, 3)
    }

    @Test
    fun testDragAndDrop_withoutSelection_distinctPlace() {
        controller.startDrag(0)
        verify(selectionMenu).onSelectionStart()
        verify(adapter).toggleSelection(0)

        controller.drop(0, 3)
        verify(behavior).onReorderHabit(habits[0], habits[3])
        verify(selectionMenu).onSelectionFinish()
        verify(adapter).performReorder(0, 3)
        verify(adapter).clearSelection()
    }

    @Test
    fun testLongClick_withSelection() {
        controller.onItemLongClick(0)
        verify(adapter).toggleSelection(0)
        verify(selectionMenu).onSelectionStart()

        controller.onItemLongClick(1)
        verify(adapter).toggleSelection(1)
        verify(selectionMenu).onSelectionChange()
    }
}