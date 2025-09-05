package org.isoron.uhabits.core.models

import java.lang.IllegalStateException

enum class NumericalEmptyDaysMode(val value: Int) {
    EXCLUDE_EMPTY(0), INCLUDE_EMPTY(1);

    companion object {
        fun fromInt(value: Int): NumericalEmptyDaysMode {
            return when (value) {
                EXCLUDE_EMPTY.value -> EXCLUDE_EMPTY
                INCLUDE_EMPTY.value -> INCLUDE_EMPTY
                else -> throw IllegalStateException()
            }
        }
    }
}
