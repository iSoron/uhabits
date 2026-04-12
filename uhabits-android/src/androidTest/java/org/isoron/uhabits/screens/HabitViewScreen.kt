package org.isoron.uhabits.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.isoron.uhabits.R

object HabitViewScreen : KScreen<HabitViewScreen>() {
    override val layoutId: Int = R.layout.fragment_habit_show
    override val viewClass: Class<*>? = null

    // Название привычки
    val habitTitle = KTextView { withId(R.id.habitName) }

    // Описание привычки
    val habitDescription = KTextView { withId(R.id.habitDescription) }

    // Сила привычки (процент)
    val habitStrength = KTextView { withId(R.id.strengthText) }

    // Кнопка "Выполнить сегодня"
    val checkmarkButton = KButton { withId(R.id.checkmark) }

    // Кнопка редактирования (карандаш)
    val editButton = KButton { withId(R.id.menu_edit) }

    // Кнопка удаления (корзина)
    val deleteButton = KButton { withId(R.id.menu_delete) }

    // Кнопка архивации
    val archiveButton = KButton { withId(R.id.menu_archive) }

    // Календарь/график выполнения
    val timelineGraph = KView { withId(R.id.timeline) }
}