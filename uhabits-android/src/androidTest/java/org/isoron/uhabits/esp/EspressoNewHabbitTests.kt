package org.isoron.uhabits.esp

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import junit.framework.Assert.fail
import org.isoron.uhabits.R
import org.isoron.uhabits.R.id.button
import org.isoron.uhabits.activities.habits.edit.EditHabitActivity
import org.junit.Rule
import org.junit.Test


class EspressoNewHabbitTests {
    @get:Rule
    val activityRule = ActivityScenarioRule(EditHabitActivity::class.java)

    private fun checkAndTypeText(viewId: Int, text: String) { //чек элемента, инпут, чек инпута
        onView(withId(viewId)).check(matches(isDisplayed())).perform(typeText(text))
        onView(withId(viewId)).check(matches(withText(text)))
    }

    @Test
    fun testCheckElemntsOnCreateHabbit() {
        try {
            onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
            onView(withText("Create habit")).check(matches(isDisplayed()))
            onView(withText("Name")).check(matches(isDisplayed()))
            onView(withText("Color")).check(matches(isDisplayed()))
            onView(withId(R.id.colorButton)).check(matches(isDisplayed()))
            onView(withText("Question")).check(matches(isDisplayed()))
            onView(withId(R.id.text_frequency_picker)).check(matches(withText("Frequency")))
            onView(withId(R.id.boolean_frequency_picker)).check(matches(isDisplayed()))
                .check(matches(isClickable()))
            onView(withId(R.id.numericalFrequencyPicker)).check(matches(withText("Every day")))
            onView(withText("Reminder")).check(matches(isDisplayed()))
            onView(withId(R.id.reminderTimePicker)).check(matches(withText("Off")))
            onView(withText("Notes")).check(matches(isDisplayed()))
            onView(withId(R.id.buttonSave)).check(matches(withText("SAVE"))).perform(click())
        } catch (e: Exception) {
            Log.e(
                "Тест наличия элементов на экране создания привычки не пройден",
                "Элементы или текст который они содержат не соответствует ожидаемому"
            )
            fail("Тест наличия элементов на экране создания привычки не пройден")
        }
    }


    @Test
    fun testCheckInputCreateHabbit() {
       try {
            checkAndTypeText(R.id.nameInput, "Run")
            checkAndTypeText(R.id.questionInput,"No")
            onView(withId(R.id.text_frequency_picker)).check(matches(withText("Frequency")))
            onView(withId(R.id.boolean_frequency_picker)).check(matches(isDisplayed())).perform(click())
            onView(withText("Every day")).check(matches(isDisplayed()))
            onView(withId(R.id.everyDayRadioButton)).check(matches(isDisplayed()))
            onView(withId(R.id.everyXDaysRadioButton)).check(matches(isDisplayed())).perform(click())
            onView(withText("Every")).check(matches(isDisplayed()))
            onView(withId(R.id.everyXDaysTextView)).check(matches(isDisplayed()))
                .perform(typeText("3"))
            onView(withId(R.id.xTimesPerWeekTextView)).check(matches(isDisplayed()))
            onView(withText("times per week")).check(matches(isDisplayed()))
            onView(withId(R.id.xTimesPerMonthRadioButton)).check(matches(isDisplayed()))
            onView(withText("times per month")).check(matches(isDisplayed()))
            onView(withId(R.id.xTimesPerYDaysRadioButton)).check(matches(isDisplayed()))
            onView(withText("times in")).check(matches(isDisplayed()))
            onView(withId(R.id.xTimesPerYDaysYTextView)).check(matches(isDisplayed()))
            onView(withId(android.R.id.button1)).check(matches(isDisplayed())).perform(click())
            onView(withText("Every 33 days")).check(matches(isDisplayed()))
            onView(withText("Reminder")).check(matches(isDisplayed()))
            onView(withId(R.id.reminderTimePicker)).check(matches(withText("Off"))).perform(click())
            onView(withId(R.id.time_display_background)).check(matches(isDisplayed()))
            onView(withId(R.id.time_picker)).check(matches(isDisplayed()))
            onView(withId(R.id.clear_button)).check(matches(isDisplayed()))
            onView(withId(R.id.done_button)).check(matches(isDisplayed())).perform(click())
            onView(withId(R.id.reminderTimePicker)).check(matches(withText("8:00 AM")))
            onView(withId(R.id.reminderDatePicker)).check(matches(withText("Any day of the week")))
            checkAndTypeText(R.id.notesInput,"Some notes")
        } catch (e: Exception) {
            Log.e(
                "Тест инпутов на экране создания привычки не пройден",
                "Введенный в инпут текст не соответствует ожидаемому"
            )
            fail("Тест инпутов на экране создания привычки не пройден")
        }
    }

    @Test
    fun testElemntsCreateHabbitAfterRecreate() {
        try {
            onView(withId(R.id.nameInput)).check(matches(isDisplayed()))
                .perform(typeText("Run"))
            onView(withId(R.id.questionInput)).check(matches(isDisplayed()))
                .perform(typeText("No"))
            onView(withId(R.id.notesInput)).check(matches(isDisplayed()))
                .perform(typeText("Some notes"))
            activityRule.scenario.recreate()
            onView(withId(R.id.nameInput)).check(matches(withText("Run")))
            onView(withId(R.id.questionInput)).check(matches(withText("No")))
            onView(withId(R.id.notesInput)).check(matches(withText("Some notes")))
        } catch (e: Exception) {
            Log.e(
                "Тест recreate не пройден", "Введенный до recreate текст не сохраняется"
            )
            fail("Тест recreate не пройден, введенный до recreate текст не сохраняется")
        }
    }
}