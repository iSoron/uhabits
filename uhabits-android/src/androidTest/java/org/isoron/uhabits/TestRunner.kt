package org.isoron.uhabits

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import io.qameta.allure.kotlin.Allure

class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        Allure.lifecycle.updateTestCase { testCase ->
            // Автоматически добавляем шаги в отчет
        }
        return super.newApplication(cl, className, context)
    }
}