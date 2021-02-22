import groovy.xml.MarkupBuilder
import java.io.StringWriter

task("updateTranslators") {
    doLast {
        fun updateTranslatorLayouts() {
            val writer = StringWriter()
            val indent = "    "
            val xml = MarkupBuilder(groovy.util.IndentPrinter(writer, indent))
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

                    val reader = file("translators").bufferedReader()
                    for (l in reader.lines()) {
                        "TextView"(
                            "style" to "@style/About.Item",
                            "android:text" to l
                        )
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
