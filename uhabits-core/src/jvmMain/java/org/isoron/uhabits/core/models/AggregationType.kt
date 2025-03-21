package org.isoron.uhabits.core.models

enum class AggregationType(val value: Int) {
    SUM(0), AVERAGE(1);

    companion object {
        fun fromInt(value: Int): AggregationType {
            return when (value) {
                SUM.value -> SUM
                AVERAGE.value -> AVERAGE
                else -> throw IllegalStateException()
            }
        }
    }
}
