package org.isoron.uhabits.otus

import com.kaspersky.kaspresso.screens.KScreen
import org.isoron.uhabits.R
import io.github.kakaocup.kakao.text.KTextView
import io.github.kakaocup.kakao.bottomnav.KBottomNavigationView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.text.KButton


object IntroScreen : KScreen<IntroScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null
    val bottom = KBottomNavigationView { withId(R.id.bottom) }
    val back = KButton { withId(R.id.back) }
    val done = KButton { withId(R.id.done) }
    val next = KButton { withId(R.id.next) }
    val skip = KButton { withId(R.id.skip) }
}











