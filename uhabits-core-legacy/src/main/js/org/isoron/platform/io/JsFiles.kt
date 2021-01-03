/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.platform.io

import kotlinx.coroutines.*
import org.isoron.platform.gui.*
import org.isoron.platform.gui.Image
import org.w3c.dom.*
import org.w3c.xhr.*
import kotlin.browser.*
import kotlin.js.*

class JsFileStorage {
    private val TAG = "JsFileStorage"
    private val log = StandardLog()

    private val indexedDB = eval("indexedDB")
    private var db: dynamic = null

    private val DB_NAME = "Main"
    private val OS_NAME = "Files"

    suspend fun init() {
        log.info(TAG, "Initializing")
        Promise<Int> { resolve, reject ->
            val req = indexedDB.open(DB_NAME, 2)
            req.onerror = { reject(Exception("could not open IndexedDB")) }
            req.onupgradeneeded = {
                log.info(TAG, "Creating document store")
                req.result.createObjectStore(OS_NAME)
            }
            req.onsuccess = {
                log.info(TAG, "Ready")
                db = req.result
                resolve(0)
            }
        }.await()
    }

    suspend fun delete(path: String) {
        Promise<Int> { resolve, reject ->
            val transaction = db.transaction(OS_NAME, "readwrite")
            val os = transaction.objectStore(OS_NAME)
            val req = os.delete(path)
            req.onerror = { reject(Exception("could not delete $path")) }
            req.onsuccess = { resolve(0) }
        }.await()
    }

    suspend fun put(path: String, content: String) {
        Promise<Int> { resolve, reject ->
            val transaction = db.transaction(OS_NAME, "readwrite")
            val os = transaction.objectStore(OS_NAME)
            val req = os.put(content, path)
            req.onerror = { reject(Exception("could not put $path")) }
            req.onsuccess = { resolve(0) }
        }.await()
    }

    suspend fun get(path: String): String {
        return Promise<String> { resolve, reject ->
            val transaction = db.transaction(OS_NAME, "readonly")
            val os = transaction.objectStore(OS_NAME)
            val req = os.get(path)
            req.onerror = { reject(Exception("could not get $path")) }
            req.onsuccess = { resolve(req.result) }
        }.await()
    }

    suspend fun exists(path: String): Boolean {
        return Promise<Boolean> { resolve, reject ->
            val transaction = db.transaction(OS_NAME, "readonly")
            val os = transaction.objectStore(OS_NAME)
            val req = os.count(path)
            req.onerror = { reject(Exception("could not count $path")) }
            req.onsuccess = { resolve(req.result > 0) }
        }.await()
    }
}

class JsFileOpener(val fileStorage: JsFileStorage) : FileOpener {

    override fun openUserFile(path: String): UserFile {
        return JsUserFile(fileStorage, path)
    }

    override fun openResourceFile(path: String): ResourceFile {
        return JsResourceFile(path)
    }
}

class JsUserFile(val fs: JsFileStorage,
                 val filename: String) : UserFile {
    override suspend fun lines(): List<String> {
        return fs.get(filename).lines()
    }

    override suspend fun delete() {
        fs.delete(filename)
    }

    override suspend fun exists(): Boolean {
        return fs.exists(filename)
    }
}

class JsResourceFile(val filename: String) : ResourceFile {
    override suspend fun exists(): Boolean {
        return Promise<Boolean> { resolve, reject ->
            val xhr = XMLHttpRequest()
            xhr.open("GET", "/assets/$filename", true)
            xhr.onload = { resolve(xhr.status.toInt() != 404) }
            xhr.onerror = { reject(Exception()) }
            xhr.send()
        }.await()
    }

    override suspend fun lines(): List<String> {
        return Promise<List<String>> { resolve, reject ->
            val xhr = XMLHttpRequest()
            xhr.open("GET", "/assets/$filename", true)
            xhr.onload = { resolve(xhr.responseText.lines()) }
            xhr.onerror = { reject(Exception()) }
            xhr.send()
        }.await()
    }

    override suspend fun copyTo(dest: UserFile) {
        val fs = (dest as JsUserFile).fs
        fs.put(dest.filename, lines().joinToString("\n"))
    }

    override suspend fun toImage(): Image {
        return Promise<Image> { resolve, reject ->
            val img = org.w3c.dom.Image()
            img.onload = {
                val canvas = JsCanvas(document.createElement("canvas") as HTMLCanvasElement, 1.0)
                canvas.element.width = img.naturalWidth
                canvas.element.height = img.naturalHeight
                canvas.setColor(Color(0xffffff))
                canvas.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight())
                canvas.ctx.drawImage(img, 0.0, 0.0)
                resolve(canvas.toImage())
            }
            img.src = "/assets/$filename"
        }.await()
    }
}
