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
    kotlin("multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvm().withJava()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.8")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                compileOnly("com.google.dagger:dagger:2.31.2")
                implementation("com.google.guava:guava:30.0-jre")
                implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.21-2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.4.2")
                implementation("androidx.annotation:annotation:1.1.0")
                implementation("com.google.code.findbugs:jsr305:3.0.2")
                implementation("com.opencsv:opencsv:3.10")
                implementation("commons-codec:commons-codec:1.15")
                implementation("org.apache.commons:commons-lang3:3.11")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.xerial:sqlite-jdbc:3.25.2")
                implementation("org.hamcrest:hamcrest:2.1")
                implementation("nl.jqno.equalsverifier:equalsverifier:2.4.8")
                implementation("org.apache.commons:commons-io:1.3.2")
                implementation("org.mockito:mockito-core:2.28.2")
                implementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
            }
        }
    }
}
