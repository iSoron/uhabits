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

package org.isoron.uhabits.activities.common.dialogs

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.PopupWindow
import org.isoron.platform.gui.ScreenLocation
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.databinding.CheckmarkPopupBinding
import org.isoron.uhabits.utils.InterfaceUtils.getFontAwesome
import org.isoron.uhabits.utils.dimBehind
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.sres

const val POPUP_WIDTH = 4 * 48f + 16f
const val POPUP_HEIGHT = 48f * 2.5f + 8f

class CheckmarkPopup(
    private val context: Context,
    private val color: Int,
    private var notes: String,
    private var value: Int,
    private val prefs: Preferences,
    private val anchor: View,
) {
    var onToggle: (Int, String) -> Unit = { _, _ -> }

    private val view = CheckmarkPopupBinding.inflate(LayoutInflater.from(context)).apply {
        // Required for round corners
        container.clipToOutline = true
    }

    init {
        initColors()
        initTypefaces()
        hideDisabledButtons()
        populate()
    }

    private fun initColors() {
        arrayOf(view.yesBtn, view.skipBtn).forEach {
            it.setTextColor(color)
        }
        arrayOf(view.noBtn, view.unknownBtn).forEach {
            it.setTextColor(view.root.sres.getColor(R.attr.contrast60))
        }
    }

    private fun initTypefaces() {
        arrayOf(view.yesBtn, view.noBtn, view.skipBtn, view.unknownBtn).forEach {
            it.typeface = getFontAwesome(context)
        }
    }

    private fun hideDisabledButtons() {
        if (!prefs.isSkipEnabled) view.skipBtn.visibility = GONE
        if (!prefs.areQuestionMarksEnabled) view.unknownBtn.visibility = GONE
    }

    private fun populate() {
        val selectedBtn = when (value) {
            YES_MANUAL -> view.yesBtn
            YES_AUTO -> view.noBtn
            NO -> view.noBtn
            UNKNOWN -> if (prefs.areQuestionMarksEnabled) view.unknownBtn else view.noBtn
            SKIP -> if (prefs.isSkipEnabled) view.skipBtn else view.noBtn
            else -> null
        }
        selectedBtn?.background = ColorDrawable(view.root.sres.getColor(R.attr.contrast40))
        view.notes.setText(notes)
    }

    fun show(location: ScreenLocation) {
        val popup = PopupWindow()
        popup.contentView = view.root
        popup.width = view.root.dp(POPUP_WIDTH).toInt()
        popup.height = view.root.dp(POPUP_HEIGHT).toInt()
        popup.isFocusable = true
        popup.elevation = view.root.dp(24f)
        fun onClick(v: Int) {
            this.value = v
            popup.dismiss()
        }
        view.yesBtn.setOnClickListener { onClick(YES_MANUAL) }
        view.noBtn.setOnClickListener { onClick(NO) }
        view.skipBtn.setOnClickListener { onClick(SKIP) }
        view.unknownBtn.setOnClickListener { onClick(UNKNOWN) }
        popup.setOnDismissListener {
            onToggle(value, view.notes.text.toString())
        }
        popup.showAtLocation(
            anchor,
            Gravity.NO_GRAVITY,
            view.root.dp(location.x.toFloat()).toInt(),
            view.root.dp(location.y.toFloat()).toInt(),
        )
        popup.dimBehind()
    }
}
