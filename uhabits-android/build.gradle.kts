/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    id("com.github.triplet.play") version "3.9.0"
    id("com.android.application") version "8.1.4"
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks.compileLint {
    dependsOn("updateTranslators")
}

/*
Added on top of kotlinOptions to work around this issue:
https://youtrack.jetbrains.com/issue/KTIJ-24311/task-current-target-is-17-and-kaptGenerateStubsProductionDebugKotlin-task-current-target-is-1.8-jvm-target-compatibility-should#focus=Comments-27-6798448.0-0
Updating gradle might fix this, so try again in the future to remove this and run:
./gradlew --rerun-tasks :uhabits-android:kaptGenerateStubsReleaseKotlin
If this doesn't produce any warning, try to remove it.
 */
kotlin {
    jvmToolchain(11)
}

android {

    namespace = "org.isoron.uhabits"
    compileSdk = 34

    defaultConfig {
        versionCode = 20200
        versionName = "2.2.0"
        minSdk = 28
        targetSdk = 34
        applicationId = "org.isoron.uhabits"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (System.getenv("LOOP_KEY_ALIAS") != null) {
            create("release") {
                keyAlias = System.getenv("LOOP_KEY_ALIAS")
                keyPassword = System.getenv("LOOP_KEY_PASSWORD")
                storeFile = file(System.getenv("LOOP_KEY_STORE"))
                storePassword = System.getenv("LOOP_STORE_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
            if (signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }

        getByName("debug") {
            isTestCoverageEnabled = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        targetCompatibility(JavaVersion.VERSION_11)
        sourceCompatibility(JavaVersion.VERSION_11)
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    val daggerVersion = "2.50"
    val kotlinVersion = "1.9.22"
    val kxCoroutinesVersion = "1.7.3"
    val ktorVersion = "1.6.8"
    val espressoVersion = "3.5.1"

    androidTestImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation("com.google.dagger:dagger:$daggerVersion")
    androidTestImplementation("com.linkedin.dexmaker:dexmaker-mockito:2.28.3")
    androidTestImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    androidTestImplementation("io.ktor:ktor-jackson:$ktorVersion")
    androidTestImplementation("androidx.annotation:annotation:1.7.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    compileOnly("javax.annotation:jsr250-api:1.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("com.github.AppIntro:AppIntro:6.3.1")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.google.dagger:dagger:$daggerVersion")
    implementation("com.google.guava:guava:33.0.0-android")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kxCoroutinesVersion")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.legacy:legacy-preference-v14:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.opencsv:opencsv:5.9")
    implementation(project(":uhabits-core"))
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kaptAndroidTest("com.google.dagger:dagger-compiler:$daggerVersion")
    testImplementation("com.google.dagger:dagger:$daggerVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}

kapt {
    correctErrorTypes = true
}

play {
    serviceAccountCredentials.set(file("../.secret/gcp-key.json"))
    track.set("alpha")
}
