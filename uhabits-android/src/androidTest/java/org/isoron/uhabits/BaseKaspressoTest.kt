package org.isoron.uhabits

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.kaspersky.components.alluresupport.withForcedAllureSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.kotlin.Allure.step
import org.isoron.platform.time.getToday
import org.isoron.uhabits.activities.habits.list.ListHabitsActivity
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.list.HabitCardListCache
import org.isoron.uhabits.inject.HabitsApplicationComponent
import org.junit.After
import org.junit.Before

abstract class BaseKaspressoTest : TestCase(
    kaspressoBuilder = Kaspresso.Builder.withForcedAllureSupport()
) {
    protected lateinit var uiDevice: UiDevice
    private lateinit var component: HabitsApplicationComponent
    protected lateinit var habitList: HabitList
    private lateinit var prefs: Preferences
    protected lateinit var fixtures: HabitFixtures
    private lateinit var cache: HabitCardListCache

    companion object {
        private const val PKG = "org.isoron.uhabits"
    }

    @Before
    open fun setUp() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Wake up and unlock device
        uiDevice.wakeUp()
        uiDevice.executeShellCommand("input keyevent 82") // KEYCODE_MENU to unlock

        // Disable animations for stable tests
        uiDevice.executeShellCommand("settings put global window_animation_scale 0")
        uiDevice.executeShellCommand("settings put global transition_animation_scale 0")
        uiDevice.executeShellCommand("settings put global animator_duration_scale 0")

        val app = ApplicationProvider.getApplicationContext<Context>()
            .applicationContext as HabitsApplication
        component = app.component
        habitList = component.habitList
        prefs = component.preferences
        cache = component.habitCardListCache
        fixtures = HabitFixtures(component.modelFactory, habitList)
        resetState()
    }

    @After
    open fun tearDown() {
        uiDevice.pressBack()
        uiDevice.pressBack()
    }

    private fun resetState() {
        prefs.clear()
        prefs.isFirstRun = false
        prefs.updateLastHint(100, getToday())
        habitList.removeAll()
        cache.refreshAllHabits()
        Thread.sleep(1000)
        createTestHabits()
    }

    private fun createTestHabits() {
        fixtures.createEmptyHabit().apply {
            name = "Wake up early"
            question = "Did you wake up early today?"
            description = "test description 1"
            color = PaletteColor(5)
            habitList.update(this)
        }

        fixtures.createShortHabit().apply {
            name = "Track time"
            question = "Did you track your time?"
            description = "test description 2"
            color = PaletteColor(5)
            habitList.update(this)
        }

        fixtures.createLongHabit().apply {
            name = "Meditate"
            question = "Did meditate today?"
            description = "test description 3"
            color = PaletteColor(10)
            habitList.update(this)
        }

        fixtures.createEmptyHabit().apply {
            name = "Read books"
            question = "Did you read books today?"
            description = ""
            color = PaletteColor(2)
            habitList.update(this)
        }
    }

    protected fun launchApp() {
        step<Unit>("Запуск приложения") {
            val intent = Intent().apply {
                component = ComponentName(PKG, ListHabitsActivity::class.java.canonicalName!!)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ApplicationProvider.getApplicationContext<Context>().startActivity(intent)
            uiDevice.waitForIdle()
            Thread.sleep(2000)
        }
    }

    protected fun takeScreenshot(name: String) {
        allureScreenshot(name)
    }
}
