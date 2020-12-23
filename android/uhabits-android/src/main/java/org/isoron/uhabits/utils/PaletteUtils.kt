package org.isoron.uhabits.utils

import android.content.Context
import android.graphics.Color
import android.util.Log
import org.isoron.uhabits.core.models.*

object PaletteUtils {
    @JvmStatic
    fun getAndroidTestColor(index: Int) = PaletteColor(index).toFixedAndroidColor()
}

fun PaletteColor.toThemedAndroidColor(context: Context): Int {
    val palette = StyledResources(context).getPalette()
    return if (paletteIndex in palette.indices) {
        palette[paletteIndex]
    } else {
        Log.w("ColorHelper", "Invalid color: $paletteIndex. Returning default.")
        palette[0]
    }
}

fun PaletteColor.toFixedAndroidColor(): Int {
    return intArrayOf(
            Color.parseColor("#D32F2F"), //  0 red
            Color.parseColor("#E64A19"), //  1 deep orange
            Color.parseColor("#F57C00"), //  2 orange
            Color.parseColor("#FF8F00"), //  3 amber
            Color.parseColor("#F9A825"), //  4 yellow
            Color.parseColor("#AFB42B"), //  5 lime
            Color.parseColor("#7CB342"), //  6 light green
            Color.parseColor("#388E3C"), //  7 green
            Color.parseColor("#00897B"), //  8 teal
            Color.parseColor("#00ACC1"), //  9 cyan
            Color.parseColor("#039BE5"), // 10 light blue
            Color.parseColor("#1976D2"), // 11 blue
            Color.parseColor("#303F9F"), // 12 indigo
            Color.parseColor("#5E35B1"), // 13 deep purple
            Color.parseColor("#8E24AA"), // 14 purple
            Color.parseColor("#D81B60"), // 15 pink
            Color.parseColor("#5D4037"), // 16 brown
            Color.parseColor("#303030"), // 17 dark grey
            Color.parseColor("#757575"), // 18 grey
            Color.parseColor("#aaaaaa")  // 19 light grey
    )[paletteIndex]
}

fun Int.toPaletteColor(context: Context): PaletteColor {
    val palette = StyledResources(context).getPalette()
    return PaletteColor(palette.indexOf(this))
}
