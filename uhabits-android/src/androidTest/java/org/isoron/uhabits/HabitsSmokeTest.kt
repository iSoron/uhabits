package org.isoron.uhabits

import io.qameta.allure.Feature
import io.qameta.allure.junit4.DisplayName
import org.isoron.uhabits.acceptance.steps.CommonSteps.launchApp
import org.isoron.uhabits.pages.MainPage
import org.isoron.uhabits.pages.NewHabitPage
import org.junit.Test

@Feature("Habits smoke test")
//@RunWith(AllureAndroidJUnit4::class)
//@RunWith(AndroidJUnit4::class)
class HabitsSmokeTest : BaseUserInterfaceTest() {

    @Test
    @DisplayName("Create new habit")
    fun createNewHabit() {
        launchApp()
        MainPage.openNewHabitPage()
        NewHabitPage.fillDefaultHabit("Test Name", "Test Question")
        MainPage.checkHabitPresentOnScroller("Test Name")
    }

    @Test
    @DisplayName("Delete habit")
    fun deleteHabit() {
        launchApp()
        with(MainPage) {
            checkHabitPresentOnScroller("Wake up early")
            deleteHabit("Wake up early")
            checkHabitIsNotPresentOnScroller("Wake up early")
        }
    }

    @Test
    @DisplayName("Hide completed habits")
    fun hideCompleted() {
        launchApp()
        with(MainPage) {
            hideCompleted()
            checkHabitIsNotPresentOnScroller("Track time")
        }
    }

    @Test
    @DisplayName("Hide archived habits")
    fun hideArchived() {
        launchApp()
        with(MainPage) {
            makeArchived("Track time")
            checkHabitIsNotPresentOnScroller("Track time")
        }
    }
}