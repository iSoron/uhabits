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

package org.isoron.uhabits.utils

import android.graphics.*
import android.support.annotation.*
import android.support.design.widget.*
import android.support.v7.widget.Toolbar
import android.view.*
import android.view.ViewGroup.LayoutParams.*
import android.widget.*
import android.widget.RelativeLayout.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*

fun RelativeLayout.addBelow(view: View,
                            subject: View,
                            width: Int = MATCH_PARENT,
                            height: Int = WRAP_CONTENT,
                            applyCustomRules: (params: RelativeLayout.LayoutParams) -> Unit = {}) {

    view.layoutParams = RelativeLayout.LayoutParams(width, height).apply {
        addRule(BELOW, subject.id)
        applyCustomRules(this)
    }
    view.id = View.generateViewId()
    this.addView(view)
}

fun RelativeLayout.addAtBottom(view: View,
                               width: Int = MATCH_PARENT,
                               height: Int = WRAP_CONTENT) {

    view.layoutParams = RelativeLayout.LayoutParams(width, height).apply {
        addRule(ALIGN_PARENT_BOTTOM)
    }
    view.id = View.generateViewId()
    this.addView(view)
}

fun RelativeLayout.addAtTop(view: View,
                            width: Int = MATCH_PARENT,
                            height: Int = WRAP_CONTENT) {

    view.layoutParams = RelativeLayout.LayoutParams(width, height).apply {
        addRule(ALIGN_PARENT_TOP)
    }
    view.id = View.generateViewId()
    this.addView(view)
}

fun ViewGroup.buildToolbar(): Toolbar {
    val inflater = LayoutInflater.from(context)
    return inflater.inflate(R.layout.toolbar, null) as Toolbar
}

fun View.showMessage(@StringRes stringId: Int) {
    try {
        val snackbar = Snackbar.make(this, stringId, Snackbar.LENGTH_SHORT)
        val tvId = android.support.design.R.id.snackbar_text
        val tv = snackbar.view.findViewById(tvId)
        if(tv is TextView) tv.setTextColor(Color.WHITE)
        snackbar.show()
    } catch (e: IllegalArgumentException) {
        return
    }
}

fun Int.toMeasureSpec(mode: Int) =
        View.MeasureSpec.makeMeasureSpec(this, mode)

fun Float.toMeasureSpec(mode: Int) =
        View.MeasureSpec.makeMeasureSpec(toInt(), mode)

fun View.isRTL() = InterfaceUtils.isLayoutRtl(this)
fun View.getFontAwesome() = InterfaceUtils.getFontAwesome(context)!!
fun View.dim(id: Int) = InterfaceUtils.getDimension(context, id)
fun View.sp(value: Float) = InterfaceUtils.spToPixels(context, value)
fun View.dp(value: Float) = InterfaceUtils.dpToPixels(context, value)
fun View.str(id: Int) = resources.getString(id)
val View.sres: StyledResources
    get() = StyledResources(context)
