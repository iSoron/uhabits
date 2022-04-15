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

package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import org.isoron.platform.gui.ScreenLocation
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.inject.ActivityContext
import javax.inject.Inject

class CheckmarkPanelViewFactory
@Inject constructor(
    @ActivityContext val context: Context,
    val preferences: Preferences,
    private val buttonFactory: CheckmarkButtonViewFactory
) {
    fun create() = CheckmarkPanelView(context, preferences, buttonFactory)
}

class CheckmarkPanelView(
    context: Context,
    preferences: Preferences,
    private val buttonFactory: CheckmarkButtonViewFactory
) : ButtonPanelView<CheckmarkButtonView>(context, preferences) {

    var values = IntArray(0)
        set(values) {
            field = values
            setupButtons()
        }

    var color = 0
        set(value) {
            field = value
            setupButtons()
        }

    var notes = arrayOf<String>()
        set(values) {
            field = values
            setupButtons()
        }

    var onToggle: (Timestamp, Int, String, Long) -> Unit = { _, _, _, _ -> }
        set(value) {
            field = value
            setupButtons()
        }

    var onEdit: (ScreenLocation, Timestamp) -> Unit = { _, _ -> }
        set(value) {
            field = value
            setupButtons()
        }

    override fun createButton(): CheckmarkButtonView = buttonFactory.create()

    @Synchronized
    override fun setupButtons() {
        val today = DateUtils.getTodayWithOffset()

        buttons.forEachIndexed { index, button ->
            val timestamp = today.minus(index + dataOffset)
            button.value = when {
                index + dataOffset < values.size -> values[index + dataOffset]
                else -> UNKNOWN
            }
            button.notes = when {
                index + dataOffset < notes.size -> notes[index + dataOffset]
                else -> ""
            }
            button.color = color
            button.onToggle = { value, notes, delay -> onToggle(timestamp, value, notes, delay) }
            button.onEdit = { location -> onEdit(location, timestamp) }
        }
    }
}
