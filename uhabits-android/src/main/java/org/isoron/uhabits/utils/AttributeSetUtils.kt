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
package org.isoron.uhabits.utils

import android.content.Context
import android.util.AttributeSet
import org.jetbrains.annotations.Contract

object AttributeSetUtils {
    const val ISORON_NAMESPACE = "http://isoron.org/android"
    @JvmStatic
    fun getAttribute(
        context: Context,
        attrs: AttributeSet,
        name: String,
        defaultValue: String?
    ): String? {
        val resId = attrs.getAttributeResourceValue(ISORON_NAMESPACE, name, 0)
        if (resId != 0) return context.resources.getString(resId)
        val value = attrs.getAttributeValue(ISORON_NAMESPACE, name)
        return value ?: defaultValue
    }

    @JvmStatic
    fun getBooleanAttribute(
        context: Context,
        attrs: AttributeSet,
        name: String,
        defaultValue: Boolean
    ): Boolean {
        val boolText = getAttribute(context, attrs, name, null)
        return if (boolText != null) java.lang.Boolean.parseBoolean(boolText) else defaultValue
    }

    @JvmStatic
    @Contract("_,_,_,!null -> !null")
    fun getColorAttribute(
        context: Context,
        attrs: AttributeSet,
        name: String,
        defaultValue: Int?
    ): Int? {
        val resId = attrs.getAttributeResourceValue(ISORON_NAMESPACE, name, 0)
        return if (resId != 0) context.resources.getColor(resId) else defaultValue
    }

    @JvmStatic
    fun getFloatAttribute(
        context: Context,
        attrs: AttributeSet,
        name: String,
        defaultValue: Float
    ): Float {
        return try {
            val number = getAttribute(context, attrs, name, null)
            number?.toFloat() ?: defaultValue
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    fun getIntAttribute(
        context: Context,
        attrs: AttributeSet,
        name: String,
        defaultValue: Int
    ): Int {
        val number = getAttribute(context, attrs, name, null)
        return number?.toInt() ?: defaultValue
    }
}
