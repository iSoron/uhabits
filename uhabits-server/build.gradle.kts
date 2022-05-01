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
    id("com.github.johnrengelman.shadow") version "7.1.2"
}


application {
    group = "org.isoron.uhabits"
    version = "0.0.1"
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    val ktorVersion = "1.6.8"
    val kotlinVersion = "1.6.21"
    val logbackVersion = "1.2.11"
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("org.jetbrains:kotlin-css-jvm:1.0.0-pre.148-kotlin-1.4.30")
    implementation("io.prometheus:simpleclient:0.15.0")
    implementation("io.prometheus:simpleclient_httpserver:0.15.0")
    implementation("io.prometheus:simpleclient_hotspot:0.15.0")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("uhabits-server")
    archiveClassifier.set("")
    archiveVersion.set("")
}
