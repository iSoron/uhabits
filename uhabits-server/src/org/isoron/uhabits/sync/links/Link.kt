/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.sync.links

import org.isoron.uhabits.sync.defaultMapper

/**
 * A Link maps a public URL (such as https://sync.loophabits.org/links/B752A6)
 * to a synchronization key. They are used to transfer sync keys between devices
 * without ever exposing the original sync key. Unlike sync keys, links expire
 * after a few minutes.
 */
data class Link(
    val id: String,
    val syncKey: String,
    val createdAt: Long,
)

fun Link.toJson(): String = defaultMapper.writeValueAsString(this)
