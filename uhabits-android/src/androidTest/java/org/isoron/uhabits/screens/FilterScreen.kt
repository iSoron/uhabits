package org.isoron.uhabits.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KButton
import org.isoron.uhabits.R

object FilterScreen : KScreen<FilterScreen>() {
    override val layoutId: Int = R.layout.dialog_filter_habits
    override val viewClass: Class<*>? = null

    // Чекбокс/переключатель "Показывать активные"
    val showActive = KView { withText("Show active") }

    // Чекбокс/переключатель "Показывать завершенные"
    val showCompleted = KView { withText("Show completed") }

    // Чекбокс/переключатель "Показывать архивированные"
    val showArchived = KView { withText("Show archived") }

    // Сортировка по имени
    val sortByName = KButton { withText("Sort by name") }

    // Сортировка по цвету
    val sortByColor = KButton { withText("Sort by color") }

    // Сортировка по силе привычки
    val sortByStrength = KButton { withText("Sort by strength") }

    // Кнопка применения фильтра
    val applyButton = KButton { withId(android.R.id.button1) }

    // Кнопка отмены
    val cancelButton = KButton { withId(android.R.id.button2) }
}// screens/FilterScreen.kt
package org.isoron.uhabits.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KButton
import org.isoron.uhabits.R

object FilterScreen : KScreen<FilterScreen>() {
    override val layoutId: Int = R.layout.dialog_filter_habits
    override val viewClass: Class<*>? = null

    // Чекбокс/переключатель "Показывать активные"
    val showActive = KView { withText("Show active") }

    // Чекбокс/переключатель "Показывать завершенные"
    val showCompleted = KView { withText("Show completed") }

    // Чекбокс/переключатель "Показывать архивированные"
    val showArchived = KView { withText("Show archived") }

    // Сортировка по имени
    val sortByName = KButton { withText("Sort by name") }

    // Сортировка по цвету
    val sortByColor = KButton { withText("Sort by color") }

    // Сортировка по силе привычки
    val sortByStrength = KButton { withText("Sort by strength") }

    // Кнопка применения фильтра
    val applyButton = KButton { withId(android.R.id.button1) }

    // Кнопка отмены
    val cancelButton = KButton { withId(android.R.id.button2) }
}