package org.isoron.uhabits.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KTextView

class SelectHabitTypeScreen : KScreen<SelectHabitTypeScreen>() {

    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val yesOrNoOption = KTextView { withText("Yes or No") }
    val measurableOption = KTextView { withText("Measurable") }

    fun selectYesOrNo() {
        yesOrNoOption.click()
    }

    fun selectMeasurable() {
        measurableOption.click()
    }
}
