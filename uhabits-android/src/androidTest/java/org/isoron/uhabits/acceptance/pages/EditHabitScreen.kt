package org.isoron.uhabits.acceptance.pages

import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import org.isoron.uhabits.R

class EditHabitScreen : Screen<EditHabitScreen>() {
    val nameInput = KEditText { withId(R.id.nameInput) }
    val questionInput = KEditText { withId(R.id.questionInput) }
    val notesInput = KEditText { withId(R.id.notesInput) }
    val frequencyPicker = KView { withId(R.id.boolean_frequency_picker) }
    val colorPickerButton = KView { withId(R.id.colorButton) }
    val saveButton = KButton { withId(R.id.buttonSave) }
}
