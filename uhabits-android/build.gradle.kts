

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
    id("com.github.triplet.play") version "3.2.0"
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.android.extensions")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks.compileLint {
    dependsOn("updateTranslators")
}

android {

    compileSdkVersion(30)

    defaultConfig {
        versionCode(20001)
        versionName("2.0.1-alpha")
        minSdkVersion(23)
        targetSdkVersion(30)
        applicationId("org.isoron.uhabits")
        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(true)
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }

        getByName("debug") {
            isTestCoverageEnabled = true
        }
    }

    lintOptions {
        isCheckReleaseBuilds = false
        isAbortOnError = false
        disable("GoogleAppIndexingWarning")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        targetCompatibility(JavaVersion.VERSION_1_8)
        sourceCompatibility(JavaVersion.VERSION_1_8)
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    val daggerVersion = "2.33"
    val kotlinVersion = "1.4.31"
    val kxCoroutinesVersion = "1.4.2"
    val ktorVersion = "1.5.1"
    val espressoVersion = "3.3.0"

    androidTestImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation("com.google.dagger:dagger:$daggerVersion")
    androidTestImplementation("com.linkedin.dexmaker:dexmaker-mockito:2.28.1")
    androidTestImplementation("com.linkedin.testbutler:test-butler-library:2.2.1")
    androidTestImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    androidTestImplementation("io.ktor:ktor-jackson:$ktorVersion")
    androidTestImplementation("androidx.annotation:annotation:1.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    compileOnly("javax.annotation:jsr250-api:1.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    implementation("com.github.paolorotolo:appintro:3.4.0")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.google.dagger:dagger:$daggerVersion")
    implementation("com.google.guava:guava:30.1-android")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kxCoroutinesVersion")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.legacy:legacy-preference-v14:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.opencsv:opencsv:5.3")
    implementation(project(":uhabits-core"))
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kaptAndroidTest("com.google.dagger:dagger-compiler:$daggerVersion")
    testImplementation("com.google.dagger:dagger:$daggerVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

kapt {
    correctErrorTypes = true
}

play {
    serviceAccountCredentials.set(file("../.secret/gcp-key.json"))
    track.set("alpha")
}
