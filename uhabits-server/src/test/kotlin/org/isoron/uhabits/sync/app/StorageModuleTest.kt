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

package org.isoron.uhabits.sync.app

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import org.isoron.uhabits.sync.*
import org.junit.Test
import kotlin.test.*

class StorageModuleTest : BaseApplicationTest() {
    private val data1 = SyncData(1, "Hello world")
    private val data2 = SyncData(2, "Hello new world")

    @Test
    fun `when get succeeds should return data`(): Unit = runBlocking {
        whenever(server.getData("k1")).thenReturn(data1)
        withTestApplication(app()) {
            handleGet("/db/k1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(data1.toJson(), response.content)
            }
        }
    }

    @Test
    fun `when get version succeeds should return version`(): Unit = runBlocking {
        whenever(server.getDataVersion("k1")).thenReturn(30)
        withTestApplication(app()) {
            handleGet("/db/k1/version").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(GetDataVersionResponse(30).toJson(), response.content)
            }
        }
    }

    @Test
    fun `when get with invalid key should return 404`(): Unit = runBlocking {
        whenever(server.getData("k1")).thenThrow(KeyNotFoundException())
        withTestApplication(app()) {
            handleGet("/db/k1").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }


    @Test
    fun `when put succeeds should return OK`(): Unit = runBlocking {
        withTestApplication(app()) {
            handlePut("/db/k1", data1).apply {
                runBlocking {
                    assertEquals(HttpStatusCode.OK, response.status())
                    verify(server).put("k1", data1)
                }
            }
        }
    }

    @Test
    fun `when put with invalid key should return 404`(): Unit = runBlocking {
        whenever(server.put("k1", data1)).thenThrow(KeyNotFoundException())
        withTestApplication(app()) {
            handlePut("/db/k1", data1).apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun `when put with invalid version should return 409 and current data`(): Unit = runBlocking {
        whenever(server.put("k1", data1)).thenThrow(EditConflictException())
        whenever(server.getData("k1")).thenReturn(data2)
        withTestApplication(app()) {
            handlePut("/db/k1", data1).apply {
                assertEquals(HttpStatusCode.Conflict, response.status())
            }
        }
    }

    private fun TestApplicationEngine.handlePut(url: String, data: SyncData): TestApplicationCall {
        return handleRequest(HttpMethod.Put, url) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(data.toJson())
        }
    }

    private fun TestApplicationEngine.handleGet(url: String): TestApplicationCall {
        return handleRequest(HttpMethod.Get, url)
    }
}
