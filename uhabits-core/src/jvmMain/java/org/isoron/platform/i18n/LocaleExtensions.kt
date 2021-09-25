package org.isoron.platform.i18n

import io.fluidsonic.locale.Locale

actual fun Locale.Companion.getDefault() =
    forLanguageTagOrNull(java.util.Locale.getDefault().toLanguageTag())
        ?: forLanguageTag("en-us")
