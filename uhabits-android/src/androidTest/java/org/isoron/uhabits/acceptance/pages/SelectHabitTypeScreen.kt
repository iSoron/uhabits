package org.isoron.uhabits.acceptance.pages

import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton

class SelectHabitTypeScreen : Screen<SelectHabitTypeScreen>() {
    val yesOrNoButton = KButton { withText("Yes or No") }
    val numericButton = KButton { withText("Number") }
}
