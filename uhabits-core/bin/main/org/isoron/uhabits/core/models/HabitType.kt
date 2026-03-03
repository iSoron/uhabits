package org.isoron.uhabits.core.models

import java.lang.IllegalStateException

enum class HabitType(val value: Int) {
    YES_NO(0), NUMERICAL(1);

    companion object {
        fun fromInt(value: Int): HabitType {
            return when (value) {
                YES_NO.value -> YES_NO
                NUMERICAL.value -> NUMERICAL
                else -> throw IllegalStateException()
            }
        }
    }
}
