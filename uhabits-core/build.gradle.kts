/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ktlint.plugin)
    alias(libs.plugins.mokkery)
}

ktlint {
    version.set(libs.versions.ktlint.engine.get())
}

kotlin {
    jvm()
    jvmToolchain(17)

    js(IR) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.kotlinx.coroutines.core)
                compileOnly(libs.kotlin.inject.runtime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(libs.guava)
                implementation(libs.kotlinx.coroutines.core.jvm)
                implementation(libs.annotation)
                implementation(libs.jsr305)
                implementation(libs.opencsv)
                implementation(libs.commons.codec)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(libs.sqlite.jdbc)
                implementation(libs.hamcrest)
                implementation(libs.commons.io)
                implementation(libs.junit.jupiter)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(npm("sql.js", "1.11.0"))
                implementation(npm("sprintf-js", "1.1.3"))
                implementation(npm("jszip", "3.10.1"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

mokkery {
    defaultMockMode.set(dev.mokkery.MockMode.autofill)
    stubs.allowConcreteClassInstantiation.set(true)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile> {
    compilerOptions {
        freeCompilerArgs.add("-jvm-default=enable")
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
