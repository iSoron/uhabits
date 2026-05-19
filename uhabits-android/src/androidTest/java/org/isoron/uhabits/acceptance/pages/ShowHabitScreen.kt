package org.isoron.uhabits.acceptance.pages

import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.screen.Screen
import org.hamcrest.CoreMatchers.endsWith
import org.isoron.uhabits.R

class ShowHabitScreen : Screen<ShowHabitScreen>() {
    val subtitleCard = KView { withId(R.id.subtitleCard) }

    val historyCard = KView {
        withClassName(endsWith("HistoryCardView"))
    }

    val scoreCard = KView {
        withClassName(endsWith("ScoreCardView"))
    }
}
