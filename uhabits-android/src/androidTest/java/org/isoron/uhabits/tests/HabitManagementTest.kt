package org.isoron.uhabits.tests

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.qameta.allure.kotlin.*
import org.junit.Rule
import org.junit.Test
import org.isoron.uhabits.MainActivity
import org.isoron.uhabits.screens.MainScreen
import org.isoron.uhabits.screens.HabitEditScreen

@Epic("Управление привычками")
@Feature("Редактирование и удаление")
class HabitManagementTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Test
    @Story("Пользователь архивирует привычку")
    @Severity(SeverityLevel.MINOR)
    fun testArchiveHabit() = run {

        step("Открыть контекстное меню привычки") {
            MainScreen.recyclerHabits {
                childWith<MainScreen.HabitItem> {
                    withName("Старая привычка")
                }.menuButton.click()
            }
        }

        step("Выбрать опцию 'Archive'") {
            KButton { withText("Archive") }.click()
        }

        step("Проверить, что привычка исчезла из основного списка") {
            MainScreen.recyclerHabits {
                childWith<MainScreen.HabitItem> {
                    withName("Старая привычка")
                }.isNotDisplayed()
            }
        }
    }
}