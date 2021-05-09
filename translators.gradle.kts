import com.opencsv.CSVReaderHeaderAware
import groovy.util.IndentPrinter
import groovy.xml.MarkupBuilder
import java.io.FileReader
import java.io.StringWriter

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        "classpath"(group = "com.opencsv", name = "opencsv", version = "5.4")
    }
}

task("updateTranslators") {
    doLast {
        fun updateTranslatorLayouts() {
            val translators = mutableMapOf<String, MutableList<String>>()

            // Classic
            var csv = CSVReaderHeaderAware(FileReader("translators-classic.csv"))
            while (true) {
                val entry = csv.readMap() ?: break
                val lang = entry["Language"]!!
                val name = entry["Name"]!!
                if (!translators.containsKey(lang)) {
                    translators[lang] = mutableListOf()
                }
                translators[lang]!!.add(name)
                translators[lang]!!.sort()
            }

            // Crowdin
            val languageMap = mapOf(
                "Arabic" to "العَرَبِية\u200E",
                "Basque" to "Euskara",
                "Bulgarian" to "Български",
                "Catalan" to "Català",
                "Chinese Simplified" to "中文",
                "Chinese Traditional" to "中文",
                "Croatian" to "Hrvatski",
                "Czech" to "Čeština",
                "Danish" to "Dansk",
                "Dutch" to "Nederlands",
                "Finnish" to "Suomen kieli",
                "French" to "Français",
                "German" to "Deutsch",
                "Greek" to "Ελληνικά",
                "Hebrew" to "עברית\u200E",
                "Hungarian" to "Magyar",
                "Indonesian" to "Bahasa Indonesia",
                "Italian" to "Italiano",
                "Japanese" to "日本語",
                "Korean" to "한국어",
                "Persian" to "العَرَبِية\u200E",
                "Polish" to "Polski",
                "Portuguese" to "Português",
                "Portuguese, Brazilian" to "Português",
                "Romanian" to "Română",
                "Russian" to "Русский",
                "Serbian (Cyrillic)" to "српски",
                "Serbian (Latin)" to "српски",
                "Spanish" to "Español",
                "Swedish" to "Svenska",
                "Tamil" to "தமிழ்\u200E",
                "Telegu" to "తెలుగు",
                "Turkish" to "Türkçe",
                "Ukrainian" to "Українська",
                "Vietnamese" to "Tiếng Việt"
            )
            csv = CSVReaderHeaderAware(FileReader("translators-crowdin.csv"))
            while (true) {
                val entry = csv.readMap() ?: break
                var lang = entry["Languages"]!!.split(";")[0]
                if (languageMap.containsKey(lang)) {
                    lang = languageMap[lang]!!
                }
                val name = entry["Name"]!!.replace(Regex(" *\\(.*\\) *"), "")
                if (name.contains("REMOVED")) continue
                if (entry["Winning (Words)"]!!.toInt() < 10 &&
                    entry["Translated (Words)"]!!.toInt() < 100 &&
                    entry["Approved (Words)"]!!.toInt() <= 0
                ) {
                    continue
                }
                if (!translators.containsKey(lang)) {
                    translators[lang] = mutableListOf()
                }
                if (translators[lang]!!.contains(name)) continue
                translators[lang]!!.add(name)
                translators[lang]!!.sort()
            }

            val writer = StringWriter()
            val indent = "    "
            val xml = MarkupBuilder(IndentPrinter(writer, indent))
            xml.doubleQuotes = true
            xml.withGroovyBuilder {
                "LinearLayout"(
                    "style" to "@style/Card",
                    "android:gravity" to "center",
                    "xmlns:android" to "http://schemas.android.com/apk/res/android"
                ) {
                    "TextView"(
                        "style" to "@style/CardHeader",
                        "android:text" to "@string/translators",
                        "android:textColor" to "?aboutScreenColor"
                    )

                    xml.mkp.yield('\n' + indent)
                    xml.mkp.comment("This list is automatically generated, do not edit manually.")

                    for ((lang, lang_translators) in translators.toSortedMap()) {
                        "TextView"(
                            "style" to "@style/About.Item.Language",
                            "android:text" to lang
                        )

                        for (t in lang_translators) {
                            "TextView"(
                                "style" to "@style/About.Item",
                                "android:text" to t
                            )
                        }
                    }
                }
            }
            val newContent = writer.toString()
            val path = "uhabits-android/src/main/res/layout/about_translators.xml"
            val currentContent = file(path).readText()
            if (currentContent != newContent) {
                file(path).writeText(newContent)
            }
        }
        updateTranslatorLayouts()
    }
}
