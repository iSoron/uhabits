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

package org.isoron.habits

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.*
import org.isoron.uhabits.*


class CoreModule(private val context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    private var backend = Backend(AndroidDatabaseOpener(),
                                  AndroidFileOpener(context),
                                  AndroidLog())

    private lateinit var emitter: RCTDeviceEventEmitter

    override fun initialize() {
        super.initialize()
        emitter = context.getJSModule(RCTDeviceEventEmitter::class.java)
    }

    override fun getName(): String {
        return "CoreModule"
    }

    @ReactMethod
    fun requestHabitList() {
        val result = backend.getHabitList()
        val data = Arguments.createArray()
        for (r in result) {
            data.pushMap(Arguments.createMap().apply {
                for ((key, value) in r) {
                    if (value is String) putString(key, value)
                    else if (value is Int) putInt(key, value)
                }
            })
        }
        emitter.emit("onHabitList", data)
    }

    @ReactMethod
    fun createHabit(name: String) {
        backend.createHabit(name)
    }

    @ReactMethod
    fun deletHabit(id: Int) {
        backend.deleteHabit(id)
    }

    @ReactMethod
    fun updateHabit(id: Int, name: String) {
        backend.updateHabit(id, name)
    }
}