/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

import android.animation.*
import android.content.*
import android.graphics.*
import android.graphics.Color.*
import android.view.*
import android.view.ViewGroup.LayoutParams.*
import android.widget.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import org.isoron.uhabits.utils.*

class HintView(
        context: Context,
        private val hintList: HintList
) : LinearLayout(context) {

    val hintContent: TextView

    init {
        isClickable = true
        visibility = GONE
        orientation = VERTICAL
        val p1 = dp(16.0f).toInt()
        val p2 = dp(4.0f).toInt()
        setPadding(p1, p1, p2, p1)
        setBackgroundColor(resources.getColor(R.color.indigo_500))

        val hintTitle = TextView(context).apply {
            setTextColor(WHITE)
            setTypeface(null, Typeface.BOLD)
            text = resources.getString(R.string.hint_title)
        }

        hintContent = TextView(context).apply {
            setTextColor(WHITE)
            setPadding(0, dp(5.0f).toInt(), 0, 0)
        }

        addView(hintTitle, WRAP_CONTENT, WRAP_CONTENT)
        addView(hintContent, WRAP_CONTENT, WRAP_CONTENT)
        setOnClickListener { dismiss() }
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showNext()
    }

    fun showNext() {
        if (!hintList.shouldShow()) return
        val hint = hintList.pop() ?: return

        hintContent.text = hint
        requestLayout()

        alpha = 0.0f
        visibility = View.VISIBLE
        animate().alpha(1f).duration = 500
    }

    private fun dismiss() {
        animate().alpha(0f).setDuration(500).setListener(DismissAnimator())
    }

    private inner class DismissAnimator : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: android.animation.Animator) {
            visibility = View.GONE
        }
    }
}
