package org.isoron.uhabits.acceptance.pages

import android.view.View
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.Matcher
import org.isoron.uhabits.R

class ListHabitsScreen : Screen<ListHabitsScreen>() {
    val createHabitButton = KView { withId(R.id.actionCreateHabit) }
    val filterButton = KView { withId(R.id.action_filter) }
    val editHabitButton = KView { withId(R.id.action_edit_habit) }

    val habitsRecyclerView = KRecyclerView(
        builder = { withClassName(endsWith("HabitCardListView")) },
        itemTypeBuilder = {
            itemType(::HabitCardItem)
        }
    )

    class HabitCardItem(parent: Matcher<View>) : KRecyclerItem<HabitCardItem>(parent) {
        val title = KTextView(parent) {
            withClassName(endsWith("TextView"))
        }
    }
}
