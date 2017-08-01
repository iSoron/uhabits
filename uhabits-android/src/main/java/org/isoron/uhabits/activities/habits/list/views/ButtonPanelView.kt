/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

import android.content.*
import android.view.*
import android.view.View.MeasureSpec.*
import android.widget.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.utils.*

abstract class ButtonPanelView<T : View>(
        context: Context,
        val preferences: Preferences
) : LinearLayout(context),
    Preferences.Listener {

    var buttonCount = 0
        set(value) {
            field = value
            inflateButtons()
        }

    var dataOffset = 0
        set(value) {
            field = value
            setupButtons()
        }

    var buttons = mutableListOf<T>()

    override fun onCheckmarkSequenceChanged() {
        inflateButtons()
    }

    @Synchronized
    protected fun inflateButtons() {
        val reverse = preferences.isCheckmarkSequenceReversed

        buttons.clear()
        repeat(buttonCount) { buttons.add(createButton()) }

        removeAllViews()
        if (reverse) buttons.reversed().forEach { addView(it) }
        else buttons.forEach { addView(it) }
        setupButtons()
        requestLayout()
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        preferences.addListener(this)
    }

    public override fun onDetachedFromWindow() {
        preferences.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val buttonWidth = dim(R.dimen.checkmarkWidth)
        val buttonHeight = dim(R.dimen.checkmarkHeight)
        val width = (buttonWidth * buttonCount)
        super.onMeasure(width.toMeasureSpec(EXACTLY),
                        buttonHeight.toMeasureSpec(EXACTLY))
    }

    protected abstract fun setupButtons()
    protected abstract fun createButton(): T
}