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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("kotlin")
    alias(libs.plugins.shadow)
}

kotlin {
    jvmToolchain(17)
}


application {
    group = "org.isoron.uhabits"
    version = "0.0.1"
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("uhabits-server")
    archiveClassifier.set("")
    archiveVersion.set("")
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.html.builder)
    implementation(libs.ktor.jackson)
    implementation(libs.kotlin.css.jvm)
    implementation(libs.simpleclient)
    implementation(libs.simpleclient.httpserver)
    implementation(libs.simpleclient.hotspot)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.mockito.kotlin)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}
