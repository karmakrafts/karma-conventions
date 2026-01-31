/*
 * Copyright 2026 Karma Krafts
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

package dev.karmakrafts.conventions.kotlin

import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests
import org.jetbrains.kotlin.konan.target.Family

/**
 * Gets a binary suffix string based on the target's family and type.
 *
 * This property determines an appropriate suffix for Apple family targets:
 * - Returns "simulator" for simulator targets
 * - Returns "device" for device targets
 * - Returns an empty string for non-Apple targets or macOS targets
 *
 * @return A string representing the binary type ("simulator", "device", or empty string)
 */
inline val KotlinNativeTarget.binarySuffix: String
    get() = if (konanTarget.family.isAppleFamily && konanTarget.family != Family.OSX) {
        if (this is KotlinNativeTargetWithSimulatorTests) "simulator" else "device"
    }
    else ""

/**
 * Gets the target pair string for this Kotlin Native target.
 *
 * This function delegates to the underlying KonanTarget's toTargetPair() function,
 * returning a string in the format "family-architecture" (e.g., "macos-x64", "linux-arm64").
 *
 * @return A string in the format "family-architecture"
 * @see org.jetbrains.kotlin.konan.target.KonanTarget.toTargetPair
 */
fun KotlinNativeTarget.getTargetPair(): String = konanTarget.toTargetPair()

/**
 * Gets the task suffix string for this Kotlin Native target.
 *
 * This function delegates to the underlying KonanTarget's toTaskSuffix() function,
 * returning a string in the format "FamilyArchitecture" (e.g., "MacosX64", "LinuxArm64").
 *
 * @return A string in the format "FamilyArchitecture" with capitalized components
 * @see org.jetbrains.kotlin.konan.target.KonanTarget.toTaskSuffix
 */
fun KotlinNativeTarget.getTaskSuffix(): String = konanTarget.toTaskSuffix()

/**
 * Gets a combined task suffix that includes both the target and binary type.
 *
 * This function combines the task suffix from getTaskSuffix() with the capitalized binary suffix,
 * creating a comprehensive identifier suitable for Gradle task names that need to distinguish
 * between simulator and device builds for Apple platforms.
 *
 * @return A string combining the task suffix and capitalized binary suffix
 */
fun KotlinNativeTarget.getBinaryTaskSuffix(): String = "${getTaskSuffix()}${binarySuffix.capitalized()}"

/**
 * Creates a complete base name for binaries that includes target and binary type information.
 *
 * This function generates a string that combines:
 * - The target pair (e.g., "ios-arm64")
 * - The binary suffix (e.g., "-simulator" or "-device") if applicable
 *
 * The resulting string is suitable for use as a base name for binary artifacts.
 *
 * @return A string in the format "family-architecture[-binarysuffix]"
 */
fun KotlinNativeTarget.getBinaryBaseName(): String {
    val rawBinarySuffix = binarySuffix
    val actualSuffix = if (rawBinarySuffix.isEmpty()) "" else "-$rawBinarySuffix"
    return "${getTargetPair()}$actualSuffix"
}
