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

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.route
import org.isoron.uhabits.core.sync.EditConflictException
import org.isoron.uhabits.core.sync.GetDataVersionResponse
import org.isoron.uhabits.core.sync.KeyNotFoundException
import org.isoron.uhabits.core.sync.SyncData

fun Routing.storage(app: SyncApplication) {
    route("/db/{key}") {
        get {
            val key = call.parameters["key"]!!
            try {
                val data = app.server.getData(key)
                call.respond(HttpStatusCode.OK, data)
            } catch (e: KeyNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        put {
            val key = call.parameters["key"]!!
            val data = call.receive<SyncData>()
            try {
                app.server.put(key, data)
                call.respond(HttpStatusCode.OK)
            } catch (e: KeyNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            } catch (e: EditConflictException) {
                call.respond(HttpStatusCode.Conflict)
            }
        }
        get("version") {
            val key = call.parameters["key"]!!
            try {
                val version = app.server.getDataVersion(key)
                call.respond(HttpStatusCode.OK, GetDataVersionResponse(version))
            } catch (e: KeyNotFoundException) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
