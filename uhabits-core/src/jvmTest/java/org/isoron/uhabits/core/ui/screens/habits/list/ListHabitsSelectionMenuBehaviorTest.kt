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
package org.isoron.uhabits.core.ui.screens.habits.list

import junit.framework.TestCase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.callbacks.OnColorPickedCallback
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito

class ListHabitsSelectionMenuBehaviorTest : BaseUnitTest() {
    @Mock
    private val screen: ListHabitsSelectionMenuBehavior.Screen? = null

    @Mock
    private val adapter: ListHabitsSelectionMenuBehavior.Adapter? = null
    private var behavior: ListHabitsSelectionMenuBehavior? = null
    private var habit1: Habit? = null
    private var habit2: Habit? = null
    private var habit3: Habit? = null

    @Captor
    private val colorPickerCallback: ArgumentCaptor<OnColorPickedCallback>? = null

    @Captor
    private val deleteCallback: ArgumentCaptor<OnConfirmedCallback>? = null

    @Test
    @Throws(Exception::class)
    fun canArchive() {
        Mockito.`when`(adapter!!.selected).thenReturn(listOf(habit1, habit2))
        TestCase.assertFalse(behavior!!.canArchive())
        Mockito.`when`(adapter.selected).thenReturn(listOf(habit2, habit3))
        TestCase.assertTrue(behavior!!.canArchive())
    }

    @Test
    @Throws(Exception::class)
    fun canEdit() {
        Mockito.`when`(adapter!!.selected).thenReturn(listOf(habit1))
        TestCase.assertTrue(behavior!!.canEdit())
        Mockito.`when`(adapter.selected).thenReturn(listOf(habit1, habit2))
        TestCase.assertFalse(behavior!!.canEdit())
    }

    @Test
    @Throws(Exception::class)
    fun canUnarchive() {
        Mockito.`when`(adapter!!.selected).thenReturn(listOf(habit1, habit2))
        TestCase.assertFalse(behavior!!.canUnarchive())
        Mockito.`when`(adapter.selected).thenReturn(listOf(habit1))
        TestCase.assertTrue(behavior!!.canUnarchive())
    }

    @Test
    @Throws(Exception::class)
    fun onArchiveHabits() {
        TestCase.assertFalse(habit2!!.isArchived)
        Mockito.`when`(adapter!!.selected).thenReturn(listOf(habit2))
        behavior!!.onArchiveHabits()
        TestCase.assertTrue(habit2!!.isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun onChangeColor() {
        assertThat(habit1!!.color, equalTo(PaletteColor(8)))
        assertThat(habit2!!.color, equalTo(PaletteColor(8)))
        Mockito.`when`(adapter!!.selected).thenReturn(listOf(habit1, habit2))
        behavior!!.onChangeColor()
        Mockito.verify(screen)!!
            .showColorPicker(ArgumentMatchers.eq(PaletteColor(8)), colorPickerCallback!!.capture())
        colorPickerCallback.value.onColorPicked(PaletteColor(30))
        assertThat(habit1!!.color, equalTo(PaletteColor(30)))
    }

    @Test
    @Throws(Exception::class)
    fun onDeleteHabits() {
        val id = habit1!!.id
        TestCase.assertNotNull(id)
        TestCase.assertNotNull(habitList.getById(id!!))
        Mockito.`when`(adapter!!.selected).thenReturn(listOf(habit1))
        behavior!!.onDeleteHabits()
        Mockito.verify(screen)!!.showDeleteConfirmationScreen(
            deleteCallback!!.capture(),
            ArgumentMatchers.eq(1)
        )
        deleteCallback.value.onConfirmed()
        TestCase.assertNull(habitList.getById(id))
    }

    @Test
    @Throws(Exception::class)
    fun onEditHabits() {
        val selected: List<Habit> = listOf(habit1!!, habit2!!)
        Mockito.`when`(adapter!!.selected).thenReturn(selected)
        behavior!!.onEditHabits()
        Mockito.verify(screen)!!.showEditHabitsScreen(selected)
    }

    @Test
    @Throws(Exception::class)
    fun onUnarchiveHabits() {
        TestCase.assertTrue(habit1!!.isArchived)
        Mockito.`when`(adapter!!.selected).thenReturn(listOf(habit1))
        behavior!!.onUnarchiveHabits()
        TestCase.assertFalse(habit1!!.isArchived)
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit1 = fixtures.createShortHabit()
        habit1!!.isArchived = true
        habit2 = fixtures.createShortHabit()
        habit3 = fixtures.createShortHabit()
        habitList.add(habit1!!)
        habitList.add(habit2!!)
        habitList.add(habit3!!)
        behavior = ListHabitsSelectionMenuBehavior(
            habitList,
            screen!!,
            adapter!!,
            commandRunner
        )
    }
}
