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

package org.isoron.uhabits.core.utils

import org.apache.commons.lang3.builder.StandardToStringStyle
import org.apache.commons.lang3.builder.ToStringStyle
import java.math.BigInteger
import java.util.Random

class StringUtils {

    companion object {
        private lateinit var toStringStyle: StandardToStringStyle

        @JvmStatic
        fun getRandomId(): String {
            return BigInteger(260, Random()).toString(32).subSequence(0, 32).toString()
        }

        @JvmStatic
        fun defaultToStringStyle(): ToStringStyle {
            toStringStyle = StandardToStringStyle()
            toStringStyle.apply {
                fieldSeparator = ", "
                isUseClassName = false
                isUseIdentityHashCode = false
                contentStart = "{"
                contentEnd = "}"
                fieldNameValueSeparator = ": "
                arrayStart = "["
                arrayEnd = "]"
            }

            return toStringStyle
        }

        @JvmStatic
        fun joinLongs(values: LongArray): String {
            return org.apache.commons.lang3.StringUtils.join(values, ',')
        }

        @JvmStatic
        fun splitLongs(str: String): LongArray {
            val parts: Array<String> = org.apache.commons.lang3.StringUtils.split(str, ',')
            return LongArray(parts.size) {
                i ->
                parts[i].toLong()
            }
        }
    }
}
