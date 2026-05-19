package org.isoron.uhabits.acceptance

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Story
import org.isoron.uhabits.BaseKaspressoTest
import org.isoron.uhabits.screens.EditHabitScreen
import org.isoron.uhabits.screens.ListHabitsScreen
import org.isoron.uhabits.screens.SelectHabitTypeScreen
import org.isoron.uhabits.screens.ToolbarMenuScreen
import org.junit.Test
import org.junit.runner.RunWith

@Epic("Habits Management")
@Feature("CRUD Operations")
@RunWith(AndroidJUnit4::class)
@LargeTest
class HabitsKaspressoTest : BaseKaspressoTest() {

    @Test
    @Story("Create Habit")
    @Description("Пользователь может создать новую привычку")
    fun shouldCreateHabit() = run {
        val habitName = "New Kaspresso Habit"
        val habitQuestion = "Did you complete this habit today?"
        val habitDesc = "Test habit created with Kaspresso"

        val listScreen = ListHabitsScreen()
        val selectTypeScreen = SelectHabitTypeScreen()
        val editScreen = EditHabitScreen()

        step("Запускаем приложение") {
            launchApp()
        }

        step("Проверяем, что экран со списком отображается") {
            listScreen {
                addHabitButton.isDisplayed()
            }
        }

        step("Нажимаем на кнопку добавления привычки") {
            listScreen.clickAddHabit()
        }

        step("Выбираем тип Yes or No") {
            selectTypeScreen {
                yesOrNoOption.click()
            }
        }

        step("Заполняем данные привычки") {
            editScreen {
                typeName(habitName)
                typeQuestion(habitQuestion)
                typeDescription(habitDesc)
                pickFrequency()
            }
        }

        step("Сохраняем") {
            editScreen.clickSave()
        }

        step("Проверяем, что привычка появилась в списке") {
            listScreen.isHabitDisplayed(habitName)
            takeScreenshot("habit_created")
        }
    }

    @Test
    @Story("Delete Habit")
    @Description("Пользователь может удалить привычку")
    fun shouldDeleteHabit() = run {
        val habitToDelete = "Track time"

        val listScreen = ListHabitsScreen()
        val toolbarScreen = ToolbarMenuScreen()

        step("Запускаем приложение") {
            launchApp()
        }

        step("Проверяем, что привычка есть в списке") {
            listScreen.isHabitDisplayed(habitToDelete)
        }

        step("Долгий клик для выбора привычки") {
            listScreen.longClickOnHabit(habitToDelete)
        }

        step("Нажимаем Delete в меню") {
            toolbarScreen.clickDelete()
        }

        step("Подтверждаем удаление") {
            toolbarScreen.confirmYes()
        }

        step("Проверяем, что привычка удалена") {
            listScreen.isHabitNotDisplayed(habitToDelete)
            takeScreenshot("habit_deleted")
        }
    }

    @Test
    @Story("Archive Habit")
    @Description("Пользователь может архивировать привычку")
    fun shouldArchiveHabit() = run {
        val habit = "Track time"

        val listScreen = ListHabitsScreen()
        val toolbarScreen = ToolbarMenuScreen()

        step("Запускаем приложение") {
            launchApp()
        }

        step("Проверяем, что привычка отображается") {
            listScreen.isHabitDisplayed(habit)
        }

        step("Долгий клик для выбора") {
            listScreen.longClickOnHabit(habit)
        }

        step("Архивируем") {
            toolbarScreen.clickArchive()
        }

        step("Проверяем, что привычка скрыта") {
            listScreen.isHabitNotDisplayed(habit)
            takeScreenshot("habit_archived")
        }

        step("Включаем показ архивных") {
            toolbarScreen.toggleShowArchived()
        }

        step("Проверяем, что архивная привычка видна") {
            listScreen.isHabitDisplayed(habit)
            takeScreenshot("archived_visible")
        }
    }
}
