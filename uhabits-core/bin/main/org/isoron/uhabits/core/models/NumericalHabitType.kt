package org.isoron.uhabits.core.models

import java.lang.IllegalStateException

enum class NumericalHabitType(val value: Int) {
    AT_LEAST(0), AT_MOST(1);

    companion object {
        fun fromInt(value: Int): NumericalHabitType {
            return when (value) {
                AT_LEAST.value -> AT_LEAST
                AT_MOST.value -> AT_MOST
                else -> throw IllegalStateException()
            }
        }
    }
}
