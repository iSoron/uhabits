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

package org.isoron.uhabits.regression

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.Matcher
import org.isoron.uhabits.BaseUserInterfaceTest
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.acceptance.steps.CommonSteps
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.EDIT_HABIT
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.LIST_HABITS
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.SELECT_HABIT_TYPE
import org.isoron.uhabits.acceptance.steps.CommonSteps.changeFrequencyToDaily
import org.isoron.uhabits.acceptance.steps.CommonSteps.changeFrequencyToMonthly
import org.isoron.uhabits.acceptance.steps.CommonSteps.clickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.createHabit
import org.isoron.uhabits.acceptance.steps.CommonSteps.launchApp
import org.isoron.uhabits.acceptance.steps.CommonSteps.longClickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.offsetHeaders
import org.isoron.uhabits.acceptance.steps.CommonSteps.scrollToText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysCheckmarks
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyShowsScreen
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.clickSave
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeName
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.ADD
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.DELETE
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.changeSort
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.clickMenu
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.longPressCheckmarks
import org.isoron.uhabits.activities.habits.list.views.CheckmarkButtonView
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@LargeTest
class ListHabitsRegressionTest : BaseUserInterfaceTest() {
    /**
     * https://github.com/iSoron/uhabits/issues/539
     */
    @Test
    @Throws(Exception::class)
    fun should_not_crash_after_deleting_then_adding_a_habit() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Track time")
        clickMenu(DELETE)
        clickText("Yes")
        clickMenu(ADD)
        verifyShowsScreen(SELECT_HABIT_TYPE)
        clickText("Yes or No")
        verifyShowsScreen(EDIT_HABIT)
        typeName("Hello world")
        clickSave()
        verifyDisplaysText("Hello world")
        longPressCheckmarks("Hello world", count = 3)
    }

    /**
     * https://github.com/iSoron/uhabits/issues/713
     */
    @Test
    @Throws(Exception::class)
    fun should_update_out_of_screen_checkmarks_when_scrolling_horizontally() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longPressCheckmarks("Wake up early", count = 1)
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysCheckmarks("Wake up early", listOf(YES_MANUAL, UNKNOWN, UNKNOWN, UNKNOWN))
        for (i in 1..10) createHabit("Habit $i")
        createHabit("Last Habit")
        scrollToText("Last Habit")
        offsetHeaders()
        verifyDisplaysCheckmarks("Wake up early", listOf(UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN))
    }

    /**
     * https://github.com/iSoron/uhabits/issues/1131
     */
    @Test
    @Throws(Exception::class)
    fun should_refresh_sort_after_habit_edit() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        changeSort("By score")
        changeSort("By status")
        longPressCheckmarks("Meditate", count = 1)
        changeFrequencyToMonthly("Read books")
        longPressCheckmarks("Read books", count = 2)
        longPressCheckmarks("Read books", count = 1)
        verifyDisplaysCheckmarks("Meditate", listOf(YES_AUTO, YES_MANUAL, YES_AUTO, YES_MANUAL))
        CommonSteps.verifyDisplaysTextInSequence(
            "Wake up early",
            "Read books",
            "Meditate",
            "Track time"
        )

        changeFrequencyToDaily("Meditate")

        verifyDisplaysCheckmarks("Meditate", listOf(NO, YES_MANUAL, UNKNOWN, YES_MANUAL))
        CommonSteps.verifyDisplaysTextInSequence(
            "Wake up early",
            "Meditate",
            "Read books",
            "Track time"
        )
    }

    /**
     * https://github.com/iSoron/uhabits/issues/1683
     * Completing the first habit while sorted by status must not follow that habit down the list.
     */
    @Test
    @Throws(Exception::class)
    fun should_preserve_viewport_when_completing_top_status_sorted_habit() {
        val app = ApplicationProvider.getApplicationContext<Context>().applicationContext as HabitsApplication
        app.component.preferences.areQuestionMarksEnabled = true
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        repeat(10) { index -> createHabit("Habit ${index + 1}") }
        changeSort("By status")
        device.waitForIdle()

        var completedHabit = ""
        Espresso.onView(ViewMatchers.withClassName(endsWith("HabitCardListView")))
            .perform(object : ViewAction {
                override fun getConstraints(): Matcher<View> = ViewMatchers.isDisplayed()

                override fun getDescription() = "complete the top habit without following it"

                override fun perform(uiController: UiController, view: View) {
                    val list = view as RecyclerView
                    val layoutManager = list.layoutManager as LinearLayoutManager
                    awaitUntil(uiController, list) {
                        it.position == 0 && it.topName == "Wake up early" && it.nextName == "Read books"
                    }
                    val before = viewport(layoutManager)
                    assertTrue(list.adapter!!.itemCount > list.childCount)
                    assertTrue(list.childCount > 1)
                    completedHabit = before.topName
                    longPressTopCheckmark(layoutManager)
                    awaitUntil(uiController, list) {
                        it.position == before.position &&
                            it.offset == before.offset &&
                            it.topName == before.nextName
                    }
                    uiController.loopMainThreadUntilIdle()
                    layoutManager.scrollToPositionWithOffset(3, before.offset)
                    uiController.loopMainThreadUntilIdle()
                    assertEquals(3, layoutManager.findFirstVisibleItemPosition())
                }
            })
        assertNotEquals("", completedHabit)
        verifyDisplaysCheckmarks(completedHabit, listOf(YES_MANUAL))
    }

    private fun awaitUntil(
        uiController: UiController,
        list: RecyclerView,
        ready: (Viewport) -> Boolean
    ) {
        val layoutManager = list.layoutManager as LinearLayoutManager
        val deadline = SystemClock.uptimeMillis() + 5000
        var stable = 0
        var last = Viewport(RecyclerView.NO_POSITION, Int.MIN_VALUE, "", "")
        while (SystemClock.uptimeMillis() < deadline) {
            uiController.loopMainThreadForAtLeast(50)
            if (list.isAnimating || list.isComputingLayout) {
                stable = 0
                continue
            }
            val snapshot = viewport(layoutManager)
            if (snapshot == last && ready(snapshot) && snapshot.position != RecyclerView.NO_POSITION) {
                stable += 1
                if (stable >= 3) return
            } else {
                stable = 0
            }
            last = snapshot
        }
        throw AssertionError("timed out waiting for the habit list to settle at ${viewport(layoutManager)}")
    }

    private fun longPressTopCheckmark(layoutManager: LinearLayoutManager) {
        val card = layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition())
        val button = card?.let { findCheckmark(it) }
        assertTrue(button?.performLongClick() == true)
    }

    private fun viewport(layoutManager: LinearLayoutManager): Viewport {
        val position = layoutManager.findFirstVisibleItemPosition()
        val child = layoutManager.findViewByPosition(position)
        val next = if (position == RecyclerView.NO_POSITION) {
            null
        } else {
            layoutManager.findViewByPosition(position + 1)
        }
        val offset = if (child == null) 0 else layoutManager.getDecoratedTop(child)
        return Viewport(
            position,
            offset,
            if (child == null) "" else habitName(child),
            if (next == null) "" else habitName(next)
        )
    }

    private fun habitName(view: View): String {
        if (view is TextView && view.text.isNotEmpty()) return view.text.toString()
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                val name = habitName(view.getChildAt(index))
                if (name.isNotEmpty()) return name
            }
        }
        return ""
    }

    private fun findCheckmark(view: View): CheckmarkButtonView? {
        if (view is CheckmarkButtonView) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findCheckmark(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private data class Viewport(
        val position: Int,
        val offset: Int,
        val topName: String,
        val nextName: String
    )
}
