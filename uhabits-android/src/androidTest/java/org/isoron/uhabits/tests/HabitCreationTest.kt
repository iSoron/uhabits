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
@Feature("Создание привычки")
class HabitCreationTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Test
    @Story("Пользователь создает новую привычку")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Тест проверяет возможность создания новой привычки через форму")
    fun testCreateNewHabit() = run {

        step("Открыть экран создания привычки") {
            MainScreen.fabAddHabit.click()
        }

        step("Заполнить название привычки") {
            HabitEditScreen.habitNameInput.replaceText("Утренняя зарядка")
        }

        step("Сохранить привычку") {
            HabitEditScreen.saveButton.click()
        }

        step("Проверить, что привычка появилась в списке") {
            MainScreen.recyclerHabits {
                childWith<MainScreen.HabitItem> {
                    withName("Утренняя зарядка")
                }.isDisplayed()
            }
        }
    }
}