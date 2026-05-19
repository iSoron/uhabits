package org.isoron.uhabits.acceptance

import androidx.test.filters.LargeTest
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Story
import org.hamcrest.CoreMatchers.endsWith
import org.isoron.uhabits.BaseUserInterfaceTest
import org.isoron.uhabits.R
import org.isoron.uhabits.acceptance.pages.EditHabitScreen
import org.isoron.uhabits.acceptance.pages.ListHabitsScreen
import org.isoron.uhabits.acceptance.pages.SelectHabitTypeScreen
import org.isoron.uhabits.acceptance.pages.ShowHabitScreen
import org.isoron.uhabits.acceptance.steps.CommonSteps
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@LargeTest
class AllureHabitsTest : BaseUserInterfaceTest() {

    @Test
    @Epic("Habit Tracking")
    @Feature("Создание привычки")
    @Description("Проверка, что пользователь может создать новую привычку, заполнив все необходимые поля, и она отображается в списке привычек после сохранения.")
    fun shouldCreateNewHabit() {
        CommonSteps.launchApp()
        val listScreen = ListHabitsScreen()
        listScreen.createHabitButton.click()

        val selectTypeScreen = SelectHabitTypeScreen()
        selectTypeScreen.yesOrNoButton.click()

        val editScreen = EditHabitScreen()
        editScreen.nameInput.replaceText("Exercise")
        editScreen.questionInput.replaceText("Did you exercise today?")
        editScreen.notesInput.replaceText("30 minutes of cardio")
        editScreen.saveButton.click()

        listScreen.habitsRecyclerView.childWith<ListHabitsScreen.HabitCardItem> {
            withDescendant { withText("Exercise") }
        }.isDisplayed()
    }

    @Test
    @Epic("Habit Tracking")
    @Feature("Статистика привычки")
    @Description("Проверка, что при нажатии на привычку открывается подробное представление со статистикой и карточками истории.")
    fun shouldViewHabitStatistics() {
        CommonSteps.launchApp()
        val listScreen = ListHabitsScreen()
        listScreen.habitsRecyclerView.childWith<ListHabitsScreen.HabitCardItem> {
            withDescendant { withText("Track time") }
        }.title.click()

        val showScreen = ShowHabitScreen()
        showScreen.subtitleCard.isDisplayed()
        showScreen.historyCard.isDisplayed()
        showScreen.scoreCard.isDisplayed()
    }

    @Test
    @Epic("Habit Tracking")
    @Feature("Удаление привычки")
    @Description("Проверка, что пользователь может удалить существующую привычку и она полностью удалена из списка привычек.")
    fun shouldDeleteHabit() {
        CommonSteps.launchApp()
        val listScreen = ListHabitsScreen()
        
        listScreen.habitsRecyclerView.childWith<ListHabitsScreen.HabitCardItem> {
            withDescendant { withText("Track time") }
        }.title.longClick()

        val overflowMenu = KView {
            withContentDescription("More options")
            withParent {
                withParent {
                    withClassName(endsWith("Toolbar"))
                }
            }
        }
        overflowMenu.click()

        val deleteButton = KButton { withText(R.string.delete) }
        deleteButton.click()

        val yesButton = KButton { withText("Yes") }
        yesButton.click()

        val deletedHabitText = KTextView { withText("Track time") }
        deletedHabitText.doesNotExist()
    }
}
