/*
 * Copyright 2025 (C) Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.conventions

import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * Gets the lowercase string representation of the target's family name.
 *
 * This property converts the Kotlin/Native target family (e.g., OSX, IOS, LINUX)
 * to a standardized lowercase string format used in naming conventions.
 *
 * @return A lowercase string representing the target's family (e.g., "macos", "ios", "linux")
 */
val KonanTarget.familyName: String
    get() = when (family) {
        Family.ANDROID -> "android"
        Family.IOS -> "ios"
        Family.OSX -> "macos"
        Family.LINUX -> "linux"
        Family.MINGW -> "windows"
        Family.TVOS -> "tvos"
        Family.WATCHOS -> "watchos"
    }

/**
 * Gets the lowercase string representation of the target's architecture name.
 *
 * This property converts the Kotlin/Native target architecture to a lowercase string
 * format used in naming conventions.
 *
 * @return A lowercase string representing the target's architecture (e.g., "x64", "arm64")
 */
val KonanTarget.architectureName: String
    get() = architecture.name.lowercase()

/**
 * Creates a standardized target identifier string by combining family and architecture.
 *
 * This function generates a string in the format "family-architecture" (e.g., "macos-x64",
 * "linux-arm64") that uniquely identifies a Kotlin/Native target platform.
 *
 * @return A string in the format "family-architecture"
 */
fun KonanTarget.toTargetPair(): String = "${familyName}-${architectureName}"

/**
 * Creates a capitalized task suffix string by combining family and architecture.
 *
 * This function generates a string in the format "FamilyArchitecture" (e.g., "MacosX64",
 * "LinuxArm64") that is suitable for use in Gradle task names.
 *
 * @return A string in the format "FamilyArchitecture" with capitalized components
 */
fun KonanTarget.toTaskSuffix(): String = "${familyName.capitalized()}${architectureName.capitalized()}"
