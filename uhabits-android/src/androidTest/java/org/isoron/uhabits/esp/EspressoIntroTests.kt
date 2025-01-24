package org.isoron.uhabits.esp
import android.util.Log
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import junit.framework.Assert.fail
import org.hamcrest.CoreMatchers.not
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.intro.IntroActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith



@RunWith(AndroidJUnit4::class)

class EspressoIntroTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(IntroActivity::class.java)

    private fun checkIntroText(title: String, text: String) {  //проверка текста
        onView(withText(title)).check(matches(isDisplayed()))
        onView(withText(text)).check(matches(isDisplayed()))
    }

    private fun checkVisibility(viewId: Int) { //проверка видимости
        onView(withId(viewId)).check(matches(isDisplayed()))
    }

    private fun clickIntroStep(viewId: Int) { //клик
        onView(withId(viewId)).perform(click())
    }

    @Test
    fun testCheckFirstIntroElements() { //чек 1 интро
        try {
            checkIntroText("Welcome", "Loop Habit Tracker helps you create and maintain good habits.")
            checkVisibility(R.id.bottom)
            onView(withId(R.id.skip)).check(matches(isDisplayed())).check(matches(isClickable()))
            onView(withId(R.id.next)).check(matches(isDisplayed())).check(matches(isClickable()))
            onView(withId(R.id.back)).check(matches(not(isDisplayed())))
            onView(withId(R.id.done)).check(matches(not(isDisplayed())))
        } catch (e: Exception) {
            Log.e("Тест первого интро не пройден", "Текст не совпадает или элементы не отображаются")
            fail("Тест первого интро не пройден, текст не совпадает или элементы не отображаются")
        }
    }

    @Test
    fun testCheckSecondIntroElements() { //чек 2 интро
        try {
            clickIntroStep(R.id.next)
            checkIntroText("Create some new habits", "Every day, after performing your habit, put a checkmark on the app.")
            checkVisibility(R.id.bottom)
            checkVisibility(R.id.skip)
        } catch (e: Exception) {
            Log.e("Тест второго интро не пройден", "Текст не совпадает или элементы не отображаются")
            fail("Тест второго интро не пройден, текст не совпадает или элементы не отображаются")
        }
    }

    @Test
    fun testCheckThirdIntroElements() { //чек 3 интро
        try {
            clickIntroStep(R.id.next)
            clickIntroStep(R.id.next)
            checkIntroText("Track your progress", "Detailed graphs show you how your habits improved over time.")
            clickIntroStep(R.id.done)
        } catch (e: Exception) {
            Log.e("Тест третьего интро не пройден", "Текст не совпадает или элементы не отображаются")
            fail("Тест третьего интро не пройден, текст не совпадает или элементы не отображаются")
        }
    }
    @Test
    fun testFirstIntroRecreate() { //рекреейт 1 интро
        try {
            checkIntroText("Welcome", "Loop Habit Tracker helps you create and maintain good habits.")
            checkVisibility(R.id.bottom)
            onView(withId(R.id.skip)).check(matches(isDisplayed())).check(matches(isClickable()))
            onView(withId(R.id.next)).check(matches(isDisplayed())).check(matches(isClickable()))
            onView(withId(R.id.back)).check(matches(not(isDisplayed())))
            onView(withId(R.id.done)).check(matches(not(isDisplayed())))
            activityRule.scenario.recreate()
            checkIntroText("Welcome", "Loop Habit Tracker helps you create and maintain good habits.")
            checkVisibility(R.id.bottom)
            onView(withId(R.id.skip)).check(matches(isDisplayed())).check(matches(isClickable()))
            onView(withId(R.id.next)).check(matches(isDisplayed())).check(matches(isClickable()))
            onView(withId(R.id.back)).check(matches(not(isDisplayed())))
            onView(withId(R.id.done)).check(matches(not(isDisplayed())))
        } catch (e: Exception) {
            Log.e("Тест recreate не пройден", "Тест recreate первого интро не пройден ")
            fail("Тест recreate первого интро не пройден")
        }
    }
    @Test
    fun testSecondIntroRecreate() { //рекреейт 2 интро
        try {
            clickIntroStep(R.id.next)
            checkIntroText("Create some new habits", "Every day, after performing your habit, put a checkmark on the app.")
            checkVisibility(R.id.bottom)
            checkVisibility(R.id.skip)
            activityRule.scenario.recreate()
            checkIntroText("Create some new habits", "Every day, after performing your habit, put a checkmark on the app.")
            checkVisibility(R.id.bottom)
            checkVisibility(R.id.skip)
        } catch (e: Exception) {
            Log.e("Тест recreate не пройден", "Тест recreate второго интро не пройден")
            fail("Тест recreate второго интро не пройден")
        }
    }
    @Test
    fun testThirdIntroRecreate() { //рекреейт 3 интро
        try {
            clickIntroStep(R.id.next)
            clickIntroStep(R.id.next)
            checkIntroText("Track your progress", "Detailed graphs show you how your habits improved over time.")
            checkVisibility(R.id.done)
            activityRule.scenario.recreate()
            checkIntroText("Track your progress", "Detailed graphs show you how your habits improved over time.")
            clickIntroStep(R.id.done)
        } catch (e: Exception) {
            Log.e("Тест recreate не пройден", "Тест recreate второго интро не пройден")
            fail("Тест recreate второго интро не пройден")
        }
    }
}