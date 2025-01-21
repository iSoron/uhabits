package org.isoron.uhabits.otus

import org.isoron.uhabits.R
import io.github.kakaocup.kakao.toolbar.KToolbar
import org.hamcrest.Matcher
import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView

object SettingScreen: KScreen<SettingScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null
    val toolbarSettings = KToolbar { withId(R.id.toolbar) }
    val recyclerView = KRecyclerView(
        builder = { withId(R.id.recycler_view) },
        itemTypeBuilder = { itemType(::NoteSettingScreen) }
    )
    class NoteSettingScreen(matcher: Matcher<View>) : KRecyclerItem<NoteSettingScreen>(matcher) {
    }
}





