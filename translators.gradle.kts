import com.crowdin.client.Client
import com.crowdin.client.core.model.Credentials
import groovy.xml.MarkupBuilder

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        "classpath"("com.github.crowdin:crowdin-api-client-java:1.3.3")
    }
}

task("fetchTranslators") {
    doLast {
        val reader = file("token_crowdin").bufferedReader()
        val t = reader.readLine()

        // Using test values
        var organization = "loop-habit-tracker"
        organization = "loop-habit-tracker-api-test"
        var projectId = 162051L
        projectId = 440668L
        val baseUrl = "https://translate.loophabits.org"

        val client = Client(Credentials(t, organization, baseUrl))
        val res = client.usersApi.listProjectMembers(projectId, null, 5, null)
        println(res)
    }
}

task("updateTranslators") {
    doLast {
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

                val reader = file("translators").bufferedReader()
                for (l in reader.lines()) {
                    "TextView"(
                        "style" to "@style/About.Item",
                        "android:text" to l
                    )
                }
            }
        }
    }
}
