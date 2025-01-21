package org.isoron.uhabits.otus

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.kaspersky.components.alluresupport.interceptors.step.AllureMapperStepInterceptor
import com.kaspersky.components.alluresupport.interceptors.testrun.DumpLogcatTestInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.isoron.uhabits.activities.settings.SettingsActivity
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)

class OtusSettingTest: TestCase(kaspressoBuilder = Kaspresso.Builder.simple(
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
    val activityScenarioRule = ActivityScenarioRule(SettingsActivity::class.java)

    @Test
    fun test_checkToolbarAndCountElements() = run {
        step("1") {
            SettingScreen {
                Assert.assertEquals(23, recyclerView.getSize())
            }
        }
        step("2") {
            SettingScreen {
                toolbarSettings.isVisible()
            }
        }
    }
}

