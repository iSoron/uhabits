package org.isoron.uhabits.screens

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher
import org.isoron.uhabits.R

class ListHabitsScreen : KScreen<ListHabitsScreen>() {

    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val addHabitButton = KButton { withContentDescription(R.string.add_habit) }
    val filterButton = KButton { withContentDescription(R.string.filter) }

    val habitsList = KRecyclerView(
        builder = { isAssignableFrom(RecyclerView::class.java) },
        itemTypeBuilder = { itemType(::HabitItem) }
    )

    class HabitItem(parent: Matcher<View>) : KRecyclerItem<HabitItem>(parent) {
        val habitName = KTextView(parent) { withId(R.id.label) }
    }

    fun clickAddHabit() {
        addHabitButton.click()
    }

    fun clickOnHabit(name: String) {
        habitsList.childWith<HabitItem> {
            withDescendant { withText(name) }
        }.click()
    }

    fun longClickOnHabit(name: String) {
        habitsList.childWith<HabitItem> {
            withDescendant { withText(name) }
        }.longClick()
    }

    fun isHabitDisplayed(name: String) {
        habitsList.childWith<HabitItem> {
            withDescendant { withText(name) }
        }.isDisplayed()
    }

    fun isHabitNotDisplayed(name: String) {
        habitsList.childWith<HabitItem> {
            withDescendant { withText(name) }
        }.doesNotExist()
    }
}
