package org.isoron.uhabits.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.qameta.allure.Step
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.isoron.uhabits.BaseUserInterfaceTest.Companion.device
import org.isoron.uhabits.R
import org.isoron.uhabits.acceptance.steps.CommonSteps.scrollToText

object MainPage {

    private val addHabitButton by lazy { onView(withId(R.id.actionCreateHabit)) }
    private val sortMenuButton by lazy { onView(withId(R.id.action_filter)) }
    private val moreMenuButton by lazy {
        onView(
            CoreMatchers.allOf(
                ViewMatchers.withContentDescription("More options"),
                ViewMatchers.withParent(
                    ViewMatchers.withParent(
                        ViewMatchers.withClassName(
                            endsWith("Toolbar")
                        )
                    )
                )
            )
        )
    }

    // More menu window
    private val deleteMenuButton by lazy { onView(withText(R.string.delete)) }
    private val yesDeleteMenuButton by lazy { onView(withText("Yes")) }

    // Sort window
    private val hideCompleted by lazy { onView(withText(R.string.hide_completed)) }
    private val hideArchived by lazy { onView(withText(R.string.hide_archived)) }
    private val archive by lazy { onView(withText(R.string.archive)) }
    private val sort by lazy { onView(withText("Sort")) }

    private val habitsScrollerRow =
        Getter<String, ViewInteraction> { habitName ->
            onView(
                allOf(
                    ViewMatchers.hasDescendant(withText(habitName)),
                    ViewMatchers.withClassName(endsWith("HabitCardView"))
                )
            )
        }

    fun interface Getter<V, R> {
        operator fun get(value: V): R
    }

    private val habitTypeWindow by lazy { onView(withText(R.string.yes_or_no_example)) }
    private val habitTypeYesOnNo by lazy { onView(withText("Yes or No")) }

    @Step("Open New Habit Page")
    fun openNewHabitPage() {
        addHabitButton.perform(ViewActions.click())
        habitTypeWindow.check(matches(isDisplayed()))
        habitTypeYesOnNo.perform(ViewActions.click())
    }

    @Step("Hide completed")
    fun hideCompleted() {
        sortMenuButton.perform(ViewActions.click())
        hideCompleted.perform(ViewActions.click())
    }

    @Step("Hide archived")
    fun hideArchived() {
        sortMenuButton.perform(ViewActions.click())
        hideArchived.perform(ViewActions.click())
    }

    @Step("Change sort on {sortText}")
    fun changeSort(sortText: String) {
        sortMenuButton.perform(ViewActions.click())
        sort.perform(ViewActions.click())
        onView(withText(sortText)).perform(ViewActions.click())
    }

    @Step("Delete habit with name {habitName}")
    fun deleteHabit(habitName: String) {
        scrollToText(habitName)
        onView(withText(habitName)).perform(ViewActions.longClick())
        device.waitForIdle()
        moreMenuButton.perform(ViewActions.click())
        deleteMenuButton.perform(ViewActions.click())
        yesDeleteMenuButton.perform(ViewActions.click())
    }

    @Step("Check habit with name {habitName} present on scroller")
    fun checkHabitPresentOnScroller(habitName: String) {
        scrollToText(habitName)
        habitsScrollerRow[habitName].check(matches(isDisplayed()))
    }

    @Step("Check habit with name {habitName} is not present on scroller")
    fun checkHabitIsNotPresentOnScroller(habitName: String) {
        onView(withText(habitName)).check(doesNotExist())
    }

    @Step("Check habit with name {habitName} is not present on scroller")
    fun makeArchived(habitName: String) {
        scrollToText(habitName)
        onView(withText(habitName)).perform(ViewActions.longClick())
        device.waitForIdle()
        moreMenuButton.perform(ViewActions.click())
        archive.perform(ViewActions.click())
    }

}