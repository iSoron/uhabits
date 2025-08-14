/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isoron.uhabits.core.utils

import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.io.Logging
import java.util.LinkedList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * A class that emits events when a new day starts.
 */
@AppScope
open class MidnightTimer @Inject constructor(logging: Logging) {
    private val listeners: MutableList<MidnightListener> = LinkedList()
    private lateinit var executor: ScheduledExecutorService
    private val logger = logging.getLogger("MidnightTimer")

    @Synchronized
    fun addListener(listener: MidnightListener) {
        this.listeners.add(listener)
    }

    @Synchronized
    fun onPause(): MutableList<Runnable>? {
        logger.info("Pausing timer")
        return executor.shutdownNow()
    }

    @Synchronized
    fun onResume(
        delayOffsetInMillis: Long = DateUtils.SECOND_LENGTH,
        testExecutor: ScheduledExecutorService? = null
    ) {
        executor = testExecutor ?: Executors.newSingleThreadScheduledExecutor()
        val initialDelay = DateUtils.millisecondsUntilTomorrowWithOffset() + delayOffsetInMillis
        logger.info("Scheduling refresh for $initialDelay ms from now")
        executor.scheduleAtFixedRate(
            { notifyListeners() },
            initialDelay,
            DateUtils.DAY_LENGTH,
            TimeUnit.MILLISECONDS
        )
    }

    @Synchronized
    fun removeListener(listener: MidnightListener) = this.listeners.remove(listener)

    @Synchronized
    private fun notifyListeners() {
        logger.info("Midnight refresh")
        for (l in listeners) {
            l.atMidnight()
        }
    }

    fun interface MidnightListener {
        fun atMidnight()
    }
}
