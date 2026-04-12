package org.isoron.uhabits.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.isoron.uhabits.R

object HabitEditScreen : KScreen<HabitEditScreen>() {
    override val layoutId: Int = R.layout.dialog_edit_habit
    override val viewClass: Class<*>? = null

    // Поле ввода названия привычки
    val habitNameInput = KEditText { withId(R.id.nameEditText) }

    // Поле ввода описания (если есть)
    val habitDescriptionInput = KEditText { withId(R.id.descriptionEditText) }

    // Кнопка выбора частоты выполнения
    val frequencyButton = KButton { withId(R.id.frequencyButton) }

    // Кнопка выбора напоминания
    val reminderButton = KButton { withId(R.id.reminderButton) }

    // Кнопка сохранения
    val saveButton = KButton { withId(R.id.saveButton) }

    // Кнопка отмены
    val cancelButton = KButton { withId(R.id.cancelButton) }

    // Заголовок диалога
    val dialogTitle = KTextView { withId(android.R.id.title) }
}