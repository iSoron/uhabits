package org.isoron.uhabits.otus

import com.kaspersky.kaspresso.screens.KScreen
import org.isoron.uhabits.R
import io.github.kakaocup.kakao.text.KTextView



object AboutScreen : KScreen<AboutScreen>()  {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null
    val tvVersion = KTextView { withId(R.id.tvVersion) }
    val tvRate = KTextView { withId(R.id.tvRate) }
    val tvFeedback = KTextView { withId(R.id.tvFeedback) }
    val tvTranslate = KTextView { withId(R.id.tvTranslate) }
    val tvSource = KTextView { withId(R.id.tvSource) }
    val tvPrivacy = KTextView { withId(R.id.tvPrivacy) }
}


