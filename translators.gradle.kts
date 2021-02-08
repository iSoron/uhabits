import com.crowdin.client.Client
import com.crowdin.client.core.model.Credentials
import groovy.xml.MarkupBuilder

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        "classpath"("com.github.crowdin:crowdin-api-client-java:1.3.4")
    }
}

task("updateTranslators") {
    doLast {

        fun fetchTranslators(): Map<String, Set<String>> {
            val baseUrl = "https://translate.loophabits.org"

            // Replacing real values with my own test values
            var organization = "loop-habit-tracker"
            organization = "loop-habit-tracker-api-test"
            var projectId = 162051L
            projectId = 440668L

            val reader = file("token_crowdin").bufferedReader()
            val t = reader.readLine()

            val client = Client(Credentials(t, organization, baseUrl))
            val projectMembers = client.usersApi.listProjectMembers(projectId, null, 5, null).data

            val languageToTranslators = HashMap<String, HashSet<String>>()
            for (member in projectMembers) {
                client.usersApi.getMemberInfo(projectId, member.data.id).data.permissions?.let {
                    for ((k, v) in it.entries) {
                        // TODO: other roles to consider
                        if (v.equals("translator")) {
                            languageToTranslators.getOrPut(k) { HashSet() }
                                .add(member.data.fullName)
                        }
                    }
                }
            }
            return languageToTranslators
        }

        fun prettyPrint(fullName: String, lang: String): String {
            // TODO: map lang to full language name
            return fullName + " (" + lang + ")"
        }

        fun updateTranslatorLayouts(languageToTranslators: Map<String, Set<String>>) {
            val writer =
                file("uhabits-android/src/main/res/layout/about_translators.xml").bufferedWriter()
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

                    // Using a file
                    /*
                    val reader = file("translators").bufferedReader()
                    for (l in reader.lines()) {
                        "TextView"(
                            "style" to "@style/About.Item",
                            "android:text" to l
                        )
                    }
                     */

                    // Using a map (generated via the crowdin API)
                    for ((lang, translators) in languageToTranslators.entries) {
                        for (name in translators) {
                            "TextView"(
                                "style" to "@style/About.Item",
                                "android:text" to prettyPrint(name, lang)
                            )
                        }
                    }
                }
            }
        }

        val allL = fetchTranslators()
        updateTranslatorLayouts(allL)
    }
}
