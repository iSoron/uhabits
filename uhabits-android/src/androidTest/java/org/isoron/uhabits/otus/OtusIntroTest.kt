package org.isoron.uhabits.otus

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.kaspersky.components.alluresupport.interceptors.step.AllureMapperStepInterceptor
import com.kaspersky.components.alluresupport.interceptors.testrun.DumpLogcatTestInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.isoron.uhabits.activities.intro.IntroActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtusIntroTest: TestCase(kaspressoBuilder = Kaspresso.Builder.simple(
    customize = {
        if (isAndroidRuntime) {
            UiDevice
                .getInstance(instrumentation)
                .executeShellCommand("appops set --uid ${InstrumentationRegistry.getInstrumentation().targetContext.packageName} MANAGE_EXTERNAL_STORAGE allow")
        }
    }
).apply {
    stepWatcherInterceptors.addAll(
        listOf(
            AllureMapperStepInterceptor()
        )
    )
    testRunWatcherInterceptors.addAll (
        listOf(
            DumpLogcatTestInterceptor(logcatDumper)
        )
    )
}
) {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(IntroActivity::class.java)

    @Test
    fun test_checkElementsOnAllIntroScreens() = run {
        step("Проверка 1 интро и переход на 2") {
            IntroScreen {
                bottom.isVisible()
                skip.isVisible()
                back.isInvisible()
                done.isInvisible()
                next.isVisible()
                next.click()
            }
        }
        step("Проверка 2 интро и переход на 3") {
            IntroScreen {
                bottom.isVisible()
                skip.isVisible()
                back.isInvisible()
                done.isInvisible()
                next.isVisible()
                next.click()
            }
        }
        step("Проверка 3 интро") {
            IntroScreen {
                bottom.isVisible()
                skip.isInvisible()
                back.isInvisible()
                next.isInvisible()
                done.isVisible()
                done.click()
            }
        }
    }
    @Test
    fun test_skipFromFirstIntro() = run {
        step("Проверка отображения элементов на 1 интро и скип") {
            IntroScreen {
                bottom.isVisible()
                skip.isVisible()
                back.isInvisible()
                done.isInvisible()
                next.isVisible()
                skip.click()
            }
        }
    }
    @Test
    fun test_skipFromSecondIntro() = run {
        step("Проверка отображения элеметов на 1 интро и переход ко 2") {
            IntroScreen {
                bottom.isVisible()
                skip.isVisible()
                back.isInvisible()
                done.isInvisible()
                next.isVisible()
                next.click()
            }
        }
        step("Проверка отображения элементов на 2 интро и скип") {
            IntroScreen {
                bottom.isVisible()
                skip.isVisible()
                back.isInvisible()
                done.isInvisible()
                next.isVisible()
                skip.click()
            }
        }
    }
}
