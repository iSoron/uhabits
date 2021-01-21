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

import android.os.Build.VERSION
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.PositionAssertions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers
import org.isoron.uhabits.BaseUserInterfaceTest
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.list.ListHabitsActivity

object CommonSteps : BaseUserInterfaceTest() {
    fun pressBack() {
        device.pressBack()
    }

    fun clickText(text: String?) {
        scrollToText(text)
        Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.click())
    }

    fun clickText(@StringRes id: Int) {
        Espresso.onView(ViewMatchers.withText(id)).perform(ViewActions.click())
    }

    fun launchApp() {
        startActivity(ListHabitsActivity::class.java)
        assertTrue(
            device.wait(Until.hasObject(By.pkg("org.isoron.uhabits")), 5000)
        )
        device.waitForIdle()
    }

    fun longClickText(text: String?) {
        scrollToText(text)
        Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.longClick())
    }

    fun pressHome() {
        device.pressHome()
        device.waitForIdle()
    }

    fun scrollToText(text: String?) {
        try {
            if (device
                .findObject(UiSelector().className(RecyclerView::class.java))
                .exists()
            ) {
                Espresso.onView(CoreMatchers.instanceOf(RecyclerView::class.java)).perform(
                    RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                        ViewMatchers.hasDescendant(ViewMatchers.withText(text))
                    )
                )
            } else {
                Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.scrollTo())
            }
        } catch (e: PerformException) {
            // ignored
        }
    }

    fun verifyDisplayGraphs() {
        verifyDisplaysView("HistoryCard")
        verifyDisplaysView("ScoreCard")
    }

    fun verifyDisplaysText(text: String?) {
        scrollToText(text)
        Espresso.onView(ViewMatchers.withText(text))
            .check(ViewAssertions.matches(ViewMatchers.isEnabled()))
    }

    fun verifyDisplaysTextInSequence(vararg text: String?) {
        verifyDisplaysText(text[0])
        for (i in 1 until text.size) {
            verifyDisplaysText(text[i])
            Espresso.onView(ViewMatchers.withText(text[i])).check(
                PositionAssertions.isCompletelyBelow(
                    ViewMatchers.withText(
                        text[i - 1]
                    )
                )
            )
        }
    }

    private fun verifyDisplaysView(className: String) {
        Espresso.onView(ViewMatchers.withClassName(CoreMatchers.endsWith(className)))
            .check(ViewAssertions.matches(ViewMatchers.isEnabled()))
    }

    fun verifyDoesNotDisplayText(text: String?) {
        Espresso.onView(ViewMatchers.withText(text)).check(ViewAssertions.doesNotExist())
    }

    @Throws(Exception::class)
    fun verifyOpensWebsite(url: String?) {
        var browserPkg = "org.chromium.webview_shell"
        if (VERSION.SDK_INT <= 23) {
            browserPkg = "com.android.browser"
        }
        assertTrue(device.wait(Until.hasObject(By.pkg(browserPkg)), 5000))
        device.waitForIdle()
        assertTrue(device.findObject(UiSelector().textContains(url)).exists())
    }

    fun verifyShowsScreen(screen: Screen?) {
        when (screen) {
            Screen.LIST_HABITS ->
                Espresso.onView(ViewMatchers.withClassName(CoreMatchers.endsWith("ListHabitsRootView")))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Screen.SHOW_HABIT ->
                Espresso.onView(ViewMatchers.withId(R.id.subtitleCard))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Screen.EDIT_HABIT ->
                Espresso.onView(ViewMatchers.withId(R.id.questionInput))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Screen.SELECT_HABIT_TYPE ->
                Espresso.onView(ViewMatchers.withText(R.string.yes_or_no_example))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            else -> throw IllegalStateException()
        }
    }

    enum class Screen {
        LIST_HABITS, SHOW_HABIT, EDIT_HABIT, SELECT_HABIT_TYPE
    }
}
