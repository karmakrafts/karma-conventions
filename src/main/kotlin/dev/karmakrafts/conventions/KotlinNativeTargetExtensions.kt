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

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests
import org.jetbrains.kotlin.konan.target.Family

/**
 * A property that provides the appropriate binary suffix for the target.
 *
 * For Apple family targets, this returns either "-simulator" or "-device" depending on
 * whether the target is for simulator tests or not. For all other targets, it returns an empty string.
 *
 * This suffix is used when constructing binary names to distinguish between different
 * build variants for the same platform.
 */
inline val KotlinNativeTarget.binarySuffix: String
    get() = if (konanTarget.family.isAppleFamily && konanTarget.family != Family.OSX) {
        if (this is KotlinNativeTargetWithSimulatorTests) "-simulator" else "-device"
    }
    else ""

/**
 * Retrieves the target pair of this [KotlinNativeTarget].
 * @see [toTargetPair]
 */
fun KotlinNativeTarget.getTargetPair(): String = konanTarget.toTargetPair()

/**
 * Returns the base name for binary packages associated with this target.
 *
 * This means that for apple family targets it will return a string in the form of
 * `<platform>-<architecture>-<type>`, where type is either `simulator` or `device`.
 * For all other platforms a string in the form of `<platform>-<architecture>` is returned.
 */
fun KotlinNativeTarget.getBinaryBaseName(): String = "${getTargetPair()}$binarySuffix"
