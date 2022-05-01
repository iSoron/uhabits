package org.isoron.platform.models

data class PaletteColor(val paletteIndex: Int) {
    fun toCsvColor(): String {
        return arrayOf(
            "#D32F2F", //  0 red
            "#E64A19", //  1 deep orange
            "#F57C00", //  2 orange
            "#FF8F00", //  3 amber
            "#F9A825", //  4 yellow
            "#AFB42B", //  5 lime
            "#7CB342", //  6 light green
            "#388E3C", //  7 green
            "#00897B", //  8 teal
            "#00ACC1", //  9 cyan
            "#039BE5", // 10 light blue
            "#1976D2", // 11 blue
            "#303F9F", // 12 indigo
            "#5E35B1", // 13 deep purple
            "#8E24AA", // 14 purple
            "#D81B60", // 15 pink
            "#5D4037", // 16 brown
            "#303030", // 17 dark grey
            "#757575", // 18 grey
            "#aaaaaa" // 19 light grey
        )[paletteIndex]
    }

    fun compareTo(other: PaletteColor): Int {
        return paletteIndex.compareTo(other.paletteIndex)
    }
}
