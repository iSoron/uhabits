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

package org.isoron.uhabits.sync

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.ServerResponseException
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.sync.AbstractSyncServer
import org.isoron.uhabits.core.sync.EditConflictException
import org.isoron.uhabits.core.sync.GetDataVersionResponse
import org.isoron.uhabits.core.sync.KeyNotFoundException
import org.isoron.uhabits.core.sync.RegisterReponse
import org.isoron.uhabits.core.sync.ServiceUnavailable
import org.isoron.uhabits.core.sync.SyncData

class RemoteSyncServer(
    private val preferences: Preferences,
    private val httpClient: HttpClient = HttpClient(Android) {
        install(JsonFeature)
    }
) : AbstractSyncServer {

    override suspend fun register(): String = Dispatchers.IO {
        try {
            val url = "${preferences.syncBaseURL}/register"
            Log.i("RemoteSyncServer", "POST $url")
            val response: RegisterReponse = httpClient.post(url)
            return@IO response.key
        } catch (e: ServerResponseException) {
            throw ServiceUnavailable()
        }
    }

    override suspend fun put(key: String, newData: SyncData) = Dispatchers.IO {
        try {
            val url = "${preferences.syncBaseURL}/db/$key"
            Log.i("RemoteSyncServer", "PUT $url")
            val response: String = httpClient.put(url) {
                header("Content-Type", "application/json")
                body = newData
            }
        } catch (e: ServerResponseException) {
            throw ServiceUnavailable()
        } catch (e: ClientRequestException) {
            Log.w("RemoteSyncServer", "ClientRequestException", e)
            if (e.message!!.contains("409")) throw EditConflictException()
            if (e.message!!.contains("404")) throw KeyNotFoundException()
            throw e
        }
    }

    override suspend fun getData(key: String): SyncData = Dispatchers.IO {
        try {
            val url = "${preferences.syncBaseURL}/db/$key"
            Log.i("RemoteSyncServer", "GET $url")
            val data: SyncData = httpClient.get(url)
            return@IO data
        } catch (e: ServerResponseException) {
            throw ServiceUnavailable()
        } catch (e: ClientRequestException) {
            Log.w("RemoteSyncServer", "ClientRequestException", e)
            throw KeyNotFoundException()
        }
    }

    override suspend fun getDataVersion(key: String): Long = Dispatchers.IO {
        try {
            val url = "${preferences.syncBaseURL}/db/$key/version"
            Log.i("RemoteSyncServer", "GET $url")
            val response: GetDataVersionResponse = httpClient.get(url)
            return@IO response.version
        } catch (e: ServerResponseException) {
            throw ServiceUnavailable()
        } catch (e: ClientRequestException) {
            Log.w("RemoteSyncServer", "ClientRequestException", e)
            throw KeyNotFoundException()
        }
    }
}
