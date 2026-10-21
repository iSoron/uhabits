package org.isoron.uhabits.core.ui.screens.habits.list

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.preferences.Preferences
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import dev.mokkery.verify.VerifyMode.Companion.not as notCalled

class HintPresenterTest : BaseUnitTest() {
    private lateinit var presenter: HintPresenter
    private lateinit var today: LocalDate
    private lateinit var yesterday: LocalDate

    private val prefs: Preferences = mock()

    @BeforeTest
    override fun setUp() {
        super.setUp()
        today = getToday()
        yesterday = today.minus(1)
        presenter = HintPresenter(HintList(prefs, arrayOf("hint1", "hint2")))
    }

    @Test
    @Throws(Exception::class)
    fun initialWhenHintShouldNotShow() {
        every { prefs.lastHintDate } returns today
        assertEquals(HintViewState(false, null), presenter.initial())
        verify(notCalled) { prefs.updateLastHint(any(), any()) }
    }

    @Test
    @Throws(Exception::class)
    fun initialPopsNextHint() {
        every { prefs.lastHintDate } returns yesterday
        every { prefs.lastHintNumber } returns -1
        assertEquals(HintViewState(true, "hint1"), presenter.initial())
        verify(exactly(1)) { prefs.updateLastHint(0, today) }
    }

    @Test
    @Throws(Exception::class)
    fun initialWhenNoHintLeft() {
        every { prefs.lastHintDate } returns yesterday
        every { prefs.lastHintNumber } returns 2
        assertEquals(HintViewState(false, null), presenter.initial())
        verify(notCalled) { prefs.updateLastHint(any(), any()) }
    }

    @Test
    @Throws(Exception::class)
    fun dismissed() {
        assertEquals(HintViewState(false, null), presenter.dismissed())
        verify(notCalled) { prefs.updateLastHint(any(), any()) }
    }
}
