package org.isoron.platform.core

import org.isoron.platform.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class BaseUnitTest {
    @BeforeTest
    @Throws(Exception::class)
    open fun setUp() {
        LocalDate.fixedLocalTime = FIXED_LOCAL_TIME
        LocalDate.setStartDayOffset(0, 0)
    }

    @AfterTest
    @Throws(Exception::class)
    open fun tearDown() {
        LocalDate.fixedLocalTime = null
        LocalDate.setStartDayOffset(0, 0)
    }

    companion object {
        // 8:00am, January 25th, 2015 (UTC)
        const val FIXED_LOCAL_TIME = 1422172800000L
    }
}
