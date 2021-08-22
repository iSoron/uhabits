pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    resolutionStrategy.eachPlugin {
        if (requested.id.id == "com.android.application") {
            useModule("com.android.tools.build:gradle:${requested.version}")
        }
    }
}

include(":uhabits-android", ":uhabits-core", ":uhabits-server")
