package org.isoron.uhabits.core.utils

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.assertEquals

class MidnightTimerTest : BaseUnitTest() {

    @Test
    fun testMidnightTimer_notifyListener_atMidnight() = runBlocking {
        // Given
        val executor = Executors.newSingleThreadScheduledExecutor()
        val dispatcher = executor.asCoroutineDispatcher()

        withContext(dispatcher) {
            DateUtils.setFixedTimeZone(TimeZone.getTimeZone("GMT"))
            DateUtils.setFixedLocalTime(
                unixTime(
                    2017,
                    Calendar.JANUARY,
                    1,
                    23,
                    59,
                    DateUtils.MINUTE_LENGTH - 1
                )
            )

            val suspendedListener = suspendCoroutine<Boolean> { continuation ->
                MidnightTimer().apply {
                    addListener { continuation.resume(true) }
                    // When
                    onResume(1, executor)
                }
            }

            // Then
            assertEquals(true, suspendedListener)
        }
    }
}
