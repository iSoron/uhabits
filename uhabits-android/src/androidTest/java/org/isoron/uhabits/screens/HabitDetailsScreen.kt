package org.isoron.uhabits.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.isoron.uhabits.R

object HabitDetailsScreen : KScreen<HabitDetailsScreen>() {

    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val subtitleCard = KTextView { withId(R.id.subtitleCard) }
    val historyCard = KTextView { withId(R.id.historyCard) }
    val scoreCard = KTextView { withId(R.id.scoreCard) }
    val frequencyCard = KTextView { withId(R.id.frequencyCard) }
    val streakCard = KTextView { withId(R.id.streakCard) }
    val overviewCard = KTextView { withId(R.id.overviewCard) }
    val notesCard = KTextView { withId(R.id.notesCard) }
    val editButton = KButton { withId(R.id.action_edit_habit) }

    fun clickEdit() {
        editButton.click()
    }

    fun checkGraphsVisible() {
        historyCard.isDisplayed()
        scoreCard.isDisplayed()
    }
}
