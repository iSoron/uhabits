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

import Foundation

class IosLocaleHelper : NSObject, LocaleHelper {
    var preferredLanguages: [String]
    
    init(_ preferredLanguages: [String]) {
        self.preferredLanguages = preferredLanguages
    }
    
    func getStringsForCurrentLocale() -> Strings {
        let lang = preferredLanguages.first ?? "en-US"
        StandardLog().debug(tag: "IosLocale", msg: lang)
        if lang.hasPrefix("ar") { return StringsArabic() }
        if lang.hasPrefix("fr") { return StringsFrench() }
        if lang.hasPrefix("es") { return StringsSpanish() }
        if lang.hasPrefix("pt-BR") { return StringsPortugueseBR() }
        if lang.hasPrefix("pt") { return StringsPortuguesePT() }
        if lang.hasPrefix("it") { return StringsItalian() }
        if lang.hasPrefix("de") { return StringsGerman() }
        if lang.hasPrefix("zh-Hans") { return StringsChineseCN() }
        if lang.hasPrefix("zh-Hant") { return StringsChineseTW() }
        if lang.hasPrefix("nl") { return StringsDutch() }
        if lang.hasPrefix("ja") { return StringsJapanese() }
        if lang.hasPrefix("ko") { return StringsKorean() }
        if lang.hasPrefix("vi") { return StringsVietnamese() }
        if lang.hasPrefix("ru") { return StringsRussian() }
        if lang.hasPrefix("sv") { return StringsSwedish() }
        if lang.hasPrefix("da") { return StringsDanish() }
        if lang.hasPrefix("fi") { return StringsFinnish() }
        if lang.hasPrefix("nb") { return StringsNorwegian() }
        if lang.hasPrefix("tr") { return StringsTurkish() }
        if lang.hasPrefix("el") { return StringsGreek() }
        if lang.hasPrefix("id") { return StringsIndonesian() }
        if lang.hasPrefix("hi") { return StringsHindi() }
        if lang.hasPrefix("hu") { return StringsHungarian() }
        if lang.hasPrefix("pl") { return StringsPolish() }
        if lang.hasPrefix("cs") { return StringsCzech() }
        if lang.hasPrefix("sk") { return StringsSlovak() }
        if lang.hasPrefix("ca") { return StringsCatalan() }
        if lang.hasPrefix("ro") { return StringsRomanian() }
        if lang.hasPrefix("hr") { return StringsCroatian() }
        if lang.hasPrefix("he") { return StringsHebrew() }
        return Strings()
    }
}
