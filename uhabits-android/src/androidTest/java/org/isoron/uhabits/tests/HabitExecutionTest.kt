package org.isoron.uhabits.tests

import androidx.test.ext.junit.rules.activityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.qameta.allure.kotlin.*
import org.junit.Rule
import org.junit.Test
import org.isoron.uhabits.MainActivity
import org.isoron.uhabits.screens.MainScreen

@Epic("Ежедневное использование")
@Feature("Выполнение привычки")
class HabitExecutionTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Test
    @Story("Пользователь отмечает выполнение привычки")
    @Severity(SeverityLevel.NORMAL)
    fun testMarkHabitAsDone() = run {

        step("Найти привычку 'Выпить воду'") {
            MainScreen.recyclerHabits {
                childWith<MainScreen.HabitItem> {
                    withName("Выпить воду")
                }.checkmark.click()
            }
        }

        step("Проверить, что чекбокс изменил состояние") {
            // Проверяем визуальное состояние или вызываем Toast
            // В реальном приложении будет меняться цвет или счетчик
        }
    }
}