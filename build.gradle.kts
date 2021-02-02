plugins {
    val kotlinVersion = "1.4.21"
    id("com.android.application") version ("4.1.0") apply (false)
    id("org.jetbrains.kotlin.android") version kotlinVersion apply (false)
    id("org.jetbrains.kotlin.kapt") version kotlinVersion apply (false)
    id("org.jetbrains.kotlin.android.extensions") version kotlinVersion apply (false)
    id("org.jetbrains.kotlin.multiplatform") version kotlinVersion apply (false)
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

apply {
    from("translators.gradle.kts")
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://kotlin.bintray.com/ktor")
        maven(url = "https://kotlin.bintray.com/kotlin-js-wrappers")
    }
}
