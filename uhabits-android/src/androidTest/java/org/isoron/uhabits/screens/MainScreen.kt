package org.isoron.uhabits.screens

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher
import org.isoron.uhabits.R

object MainScreen : KScreen<MainScreen>() {
    override val layoutId: Int = R.layout.activity_main
    override val viewClass: Class<*>? = null

    // Кнопка добавления новой привычки (Floating Action Button)
    val fabAddHabit = KButton { withId(R.id.fabCreateHabit) }

    // Список привычек
    val habitsList = KRecyclerView(
        builder = { withId(R.id.recyclerView) },
        itemTypeBuilder = { itemType(::HabitItem) }
    )

    // Меню (три точки вверху)
    val menuOverflow = KView { withId(R.id.menu_overflow) }

    // Кнопка фильтрации
    val filterButton = KView { withId(R.id.menu_filter) }

    // Модель элемента списка привычек
    class HabitItem(parent: Matcher<View>) : KRecyclerItem<HabitItem>(parent) {
        val name = KTextView(parent) { withId(R.id.habitName) }
        val checkmark = KButton(parent) { withId(R.id.checkmark) }
        val strengthText = KTextView(parent) { withId(R.id.strengthText) }
        val description = KTextView(parent) { withId(R.id.habitDescription) }

        // Кнопка меню для конкретной привычки (три точки)
        val habitMenu = KView(parent) { withId(R.id.menu) }
    }
}