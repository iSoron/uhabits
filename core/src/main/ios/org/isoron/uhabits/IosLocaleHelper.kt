/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

@file:Suppress("UNCHECKED_CAST")

package org.isoron.uhabits

import org.isoron.platform.io.*
import org.isoron.uhabits.i18n.*
import platform.Foundation.*

class IosLocaleHelper(private val log: Log) : LocaleHelper {
    override fun getStringsForCurrentLocale(): Strings {
        val pref = NSLocale.preferredLanguages as List<String>
        val lang = if (pref.isEmpty()) "en-US" else pref[0]
        log.info("IosLocaleHelper", lang)
        return when {
            lang.startsWith("ar") -> StringsArabic()
            lang.startsWith("ca") -> StringsCatalan()
            lang.startsWith("cs") -> StringsCzech()
            lang.startsWith("da") -> StringsDanish()
            lang.startsWith("de") -> StringsGerman()
            lang.startsWith("el") -> StringsGreek()
            lang.startsWith("es") -> StringsSpanish()
            lang.startsWith("fi") -> StringsFinnish()
            lang.startsWith("fr") -> StringsFrench()
            lang.startsWith("he") -> StringsHebrew()
            lang.startsWith("hi") -> StringsHindi()
            lang.startsWith("hr") -> StringsCroatian()
            lang.startsWith("hu") -> StringsHungarian()
            lang.startsWith("id") -> StringsIndonesian()
            lang.startsWith("it") -> StringsItalian()
            lang.startsWith("ja") -> StringsJapanese()
            lang.startsWith("ko") -> StringsKorean()
            lang.startsWith("nb") -> StringsNorwegian()
            lang.startsWith("nl") -> StringsDutch()
            lang.startsWith("pl") -> StringsPolish()
            lang.startsWith("pt-BR") -> StringsPortugueseBR()
            lang.startsWith("pt") -> StringsPortuguesePT()
            lang.startsWith("ro") -> StringsRomanian()
            lang.startsWith("ru") -> StringsRussian()
            lang.startsWith("sk") -> StringsSlovak()
            lang.startsWith("sv") -> StringsSwedish()
            lang.startsWith("tr") -> StringsTurkish()
            lang.startsWith("vi") -> StringsVietnamese()
            lang.startsWith("zh-Hans") -> StringsChineseCN()
            lang.startsWith("zh-Hant") -> StringsChineseTW()
            else -> Strings()
        }
    }
}