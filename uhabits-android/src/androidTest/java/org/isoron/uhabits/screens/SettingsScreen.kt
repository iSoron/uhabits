package org.isoron.uhabits.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.isoron.uhabits.R

object SettingsScreen : KScreen<SettingsScreen>() {
    override val layoutId: Int = R.layout.preferences_activity
    override val viewClass: Class<*>? = null

    // Заголовок настроек
    val settingsTitle = KTextView { withText("Settings") }

    // Настройка темы
    val themePreference = KView { withText("Theme") }

    // Настройка напоминаний
    val remindersPreference = KView { withText("Reminders") }

    // Настройка виджета
    val widgetPreference = KView { withText("Widget") }

    // Экспорт данных
    val exportPreference = KView { withText("Export") }

    // Импорт данных
    val importPreference = KView { withText("Import") }

    // Кнопка "О приложении"
    val aboutButton = KButton { withText("About") }

    // Кнопка возврата
    val backButton = KView { withContentDescription("Navigate up") }
}