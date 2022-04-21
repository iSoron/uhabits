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
package org.isoron.uhabits.acceptance.steps

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.Matcher
import org.isoron.uhabits.BaseUserInterfaceTest
import org.isoron.uhabits.BaseUserInterfaceTest.Companion.device
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.list.views.CheckmarkButtonView
import java.util.LinkedList

object ListHabitsSteps {
    fun clickMenu(item: MenuItem?) {
        when (item) {
            MenuItem.ABOUT -> clickTextInsideOverflowMenu(R.string.about)
            MenuItem.HELP -> clickTextInsideOverflowMenu(R.string.help)
            MenuItem.SETTINGS -> clickTextInsideOverflowMenu(R.string.settings)
            MenuItem.ADD -> clickViewWithId(R.id.actionCreateHabit)
            MenuItem.EDIT -> clickViewWithId(R.id.action_edit_habit)
            MenuItem.DELETE -> clickTextInsideOverflowMenu(R.string.delete)
            MenuItem.ARCHIVE -> clickTextInsideOverflowMenu(R.string.archive)
            MenuItem.UNARCHIVE -> clickTextInsideOverflowMenu(R.string.unarchive)
            MenuItem.TOGGLE_ARCHIVED -> {
                clickViewWithId(R.id.action_filter)
                CommonSteps.clickText(R.string.hide_archived)
            }
            MenuItem.TOGGLE_COMPLETED -> {
                clickViewWithId(R.id.action_filter)
                CommonSteps.clickText(R.string.hide_completed)
            }
        }
        device.waitForIdle()
    }

    private fun clickTextInsideOverflowMenu(id: Int) {
        Espresso.onView(
            allOf(
                ViewMatchers.withContentDescription("More options"),
                ViewMatchers.withParent(
                    ViewMatchers.withParent(
                        ViewMatchers.withClassName(
                            endsWith("Toolbar")
                        )
                    )
                )
            )
        ).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(id)).perform(ViewActions.click())
    }

    private fun clickViewWithId(id: Int) {
        Espresso.onView(ViewMatchers.withId(id)).perform(ViewActions.click())
    }

    private fun longClickDescendantWithClass(cls: Class<*>, count: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isEnabled()
            }

            override fun getDescription(): String {
                return "perform on children"
            }

            override fun perform(uiController: UiController, view: View) {
                val stack = LinkedList<ViewGroup>()
                if (view is ViewGroup) stack.push(view)
                var countRemaining = count
                while (!stack.isEmpty()) {
                    val vg = stack.pop()
                    for (i in 0 until vg.childCount) {
                        val v = vg.getChildAt(i)
                        if (v is ViewGroup) stack.push(v)
                        if (cls.isInstance(v) && countRemaining > 0) {
                            v.performLongClick()
                            uiController.loopMainThreadUntilIdle()
                            countRemaining--
                        }
                    }
                }
            }
        }
    }

    fun longPressCheckmarks(habit: String?, count: Int) {
        CommonSteps.scrollToText(habit)
        Espresso.onView(
            allOf(
                ViewMatchers.hasDescendant(ViewMatchers.withText(habit)),
                ViewMatchers.withClassName(endsWith("HabitCardView"))
            )
        ).perform(
            longClickDescendantWithClass(CheckmarkButtonView::class.java, count)
        )
        BaseUserInterfaceTest.device.waitForIdle()
    }

    fun changeSort(sortText: String) {
        clickViewWithId(R.id.action_filter)
        Espresso.onView(ViewMatchers.withText("Sort")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(sortText)).perform(ViewActions.click())
    }

    enum class MenuItem {
        ABOUT, HELP, SETTINGS, EDIT, DELETE, ARCHIVE, TOGGLE_ARCHIVED, UNARCHIVE, TOGGLE_COMPLETED, ADD
    }
}
