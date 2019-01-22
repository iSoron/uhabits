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


class CoreModule(
        private val context: ReactApplicationContext
) : ReactContextBaseJavaModule(context) {

    private var backend = Backend()
    private lateinit var emitter: RCTDeviceEventEmitter

    override fun initialize() {
        super.initialize()
        emitter = context.getJSModule(RCTDeviceEventEmitter::class.java)
        backend.createHabit("Wake up early")
        backend.createHabit("Wash clothes")
        backend.createHabit("Exercise")
        backend.createHabit("Meditate")
        backend.createHabit("Take vitamins")
        backend.createHabit("Write 'the quick brown fox jumps over the lazy dog' daily")
        backend.createHabit("Write journal")
        backend.createHabit("Study French")
    }

    override fun getName(): String {
        return "CoreModule"
    }

    @ReactMethod
    fun requestHabitList() {
        val params = Arguments.createArray()
        for ((id, data) in backend.getHabitList()) {
            params.pushMap(Arguments.createMap().apply {
                putString("key", id.toString())
                putString("name", data["name"] as String)
                putInt("color", data["color"] as Int)
            })
        }
        emitter.emit("onHabitList", params)
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