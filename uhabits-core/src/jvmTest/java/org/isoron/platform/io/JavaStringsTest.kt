package org.isoron.platform.io

import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaStringsTest {

    private val original: Locale = Locale.getDefault()

    @AfterTest
    fun restoreLocale() {
        Locale.setDefault(original)
    }

    private fun withLocale(locale: Locale, body: () -> Unit) {
        Locale.setDefault(locale)
        body()
    }

    @Test
    fun migrationFilenameStaysAsciiUnderNonAsciiDigitLocales() {
        for (tag in listOf("ar-EG", "ne-NP", "hi-IN-u-nu-deva", "bn-IN-u-nu-beng")) {
            withLocale(Locale.forLanguageTag(tag)) {
                assertEquals("09.sql", format("%02d.sql", 9), "locale $tag")
                assertEquals("12.sql", format("%02d.sql", 12), "locale $tag")
            }
        }
    }

    @Test
    fun widgetPreferenceKeysStayAsciiUnderNonAsciiDigitLocales() {
        for (tag in listOf("ar-EG", "ne-NP", "hi-IN-u-nu-deva")) {
            withLocale(Locale.forLanguageTag(tag)) {
                assertEquals("widget-000005-habit", format("widget-%06d-habit", 5), "locale $tag")
                assertEquals("snooze-000005", format("snooze-%06d", 5), "locale $tag")
            }
        }
    }

    @Test
    fun csvDecimalsUseAsciiDotUnderCommaDecimalLocales() {
        for (tag in listOf("de-DE", "fr-FR", "pt-BR", "ar-EG")) {
            withLocale(Locale.forLanguageTag(tag)) {
                assertEquals("0.5000", format("%.4f", 0.5), "locale $tag")
                assertEquals("1.5", format("%.1f", 1.5), "locale $tag")
            }
        }
    }

    @Test
    fun csvHabitIndexStaysAsciiUnderNonAsciiDigitLocales() {
        withLocale(Locale.forLanguageTag("ar-EG")) {
            assertEquals("003", format("%03d", 3))
        }
    }
}
