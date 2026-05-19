package org.isoron.uhabits.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.isoron.uhabits.R

class EditHabitScreen : KScreen<EditHabitScreen>() {

    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val nameInput = KEditText { withId(R.id.nameInput) }
    val questionInput = KEditText { withId(R.id.questionInput) }
    val notesInput = KEditText { withId(R.id.notesInput) }
    val frequencyPicker = KTextView { withId(R.id.boolean_frequency_picker) }
    val colorButton = KButton { withId(R.id.colorButton) }
    val saveButton = KButton { withId(R.id.buttonSave) }
    val everyDayRadioButton = KButton { withId(R.id.everyDayRadioButton) }
    val dialogSaveButton = KButton { withText("SAVE") }

    fun typeName(name: String) {
        nameInput {
            clearText()
            typeText(name)
        }
        closeSoftKeyboard()
    }

    fun typeQuestion(question: String) {
        questionInput {
            clearText()
            typeText(question)
        }
        closeSoftKeyboard()
    }

    fun typeDescription(desc: String) {
        notesInput {
            clearText()
            typeText(desc)
        }
        closeSoftKeyboard()
    }

    fun pickFrequency() {
        frequencyPicker.click()
        dialogSaveButton.click()
    }

    fun pickDailyFrequency() {
        frequencyPicker.click()
        everyDayRadioButton.click()
        dialogSaveButton.click()
    }

    fun clickSave() {
        saveButton.click()
    }
}
