package org.isoron.uhabits.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.isoron.uhabits.R

object NewHabitPage {

    private val nameInput by lazy { onView(withId(R.id.nameInput)) }
    private val questionInput by lazy { onView(withId(R.id.questionInput)) }
    private val frequencyPicker by lazy { onView(withId(R.id.boolean_frequency_picker)) }
    private val notesInput by lazy { onView(withId(R.id.notesInput)) }
    private val colorButton by lazy { onView(withId(R.id.colorButton)) }

    private val save by lazy { onView(withId(R.id.buttonSave)) }

    fun fillDefaultHabit(name: String, question: String, note: String? = null) {
        nameInput.perform(
            ViewActions.clearText(),
            ViewActions.typeText(name),
            ViewActions.closeSoftKeyboard()
        )

        questionInput.perform(
            ViewActions.clearText(),
            ViewActions.typeText(question),
            ViewActions.closeSoftKeyboard()
        )

        note?.let {
            notesInput.perform(
                ViewActions.clearText(),
                ViewActions.typeText(it),
                ViewActions.closeSoftKeyboard()
            )
        }

        save.perform(ViewActions.click())
    }
}