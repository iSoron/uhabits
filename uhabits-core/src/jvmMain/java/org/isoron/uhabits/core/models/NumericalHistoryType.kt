package org.isoron.uhabits.core.models

import java.lang.IllegalStateException

enum class NumericalHistoryType(val value: Int) {
    TOTAL(0), AVERAGE(1);

    companion object {
        fun fromInt(value: Int): NumericalHistoryType {
            return when (value) {
                TOTAL.value -> TOTAL
                AVERAGE.value -> AVERAGE
                else -> throw IllegalStateException()
            }
        }
    }
}
