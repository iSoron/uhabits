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

package org.isoron.uhabits.server.app

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import org.isoron.uhabits.server.sync.Repository
import org.isoron.uhabits.core.sync.AbstractSyncServer
import org.isoron.uhabits.server.sync.RepositorySyncServer
import java.nio.file.Path
import java.nio.file.Paths

fun Application.main() = SyncApplication().apply { main() }

val REPOSITORY_PATH: Path = Paths.get(System.getenv("LOOP_REPO_PATH")!!)

class SyncApplication(
    val server: AbstractSyncServer = RepositorySyncServer(
        Repository(REPOSITORY_PATH),
    ),
) {
    fun Application.main() {
        install(DefaultHeaders)
        install(CallLogging)
        install(ContentNegotiation) {
            jackson { }
        }
        routing {
            registration(this@SyncApplication)
            storage(this@SyncApplication)
            metrics(this@SyncApplication)
        }
    }
}
