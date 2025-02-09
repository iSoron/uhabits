plugins {
    val kotlinVersion = "2.1.10"
    id("com.android.application") version "8.8.0" apply (false)
    id("org.jetbrains.kotlin.android") version kotlinVersion apply (false)
    id("org.jetbrains.kotlin.kapt") version kotlinVersion apply (false)
    id("org.jetbrains.kotlin.multiplatform") version kotlinVersion apply (false)
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

apply {
    from("translators.gradle.kts")
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://jitpack.io")
    }
}
