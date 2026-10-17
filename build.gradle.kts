plugins {
    alias(libs.plugins.agp) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint.plugin) apply false
    alias(libs.plugins.shadow) apply false
}

apply {
    from("gradle/translators.gradle.kts")
}
