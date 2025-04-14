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

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

/**
 * Converts a dependency provider to an Android Archive (AAR) format.
 *
 * This extension function takes a Provider of MinimalExternalModuleDependency and
 * transforms it to include the AAR suffix, which is required when referencing
 * Android library dependencies.
 *
 * Example usage:
 * ```kotlin
 * dependencies {
 *     implementation(libs.androidx.appcompat.asAAR())
 * }
 * ```
 *
 * @return A Provider of String representing the dependency with the AAR suffix
 */
fun Provider<MinimalExternalModuleDependency>.asAAR(): Provider<String> {
    return map { "$it@aar" }
}

/**
 * Converts a plugin dependency provider to a library dependency format.
 *
 * This extension function takes a Provider of PluginDependency and
 * transforms it into a standard Maven coordinate format that can be used
 * as a regular library dependency in Gradle.
 *
 * Example usage:
 * ```kotlin
 * dependencies {
 *     implementation(libs.plugins.kotlin.jvm.asLibrary())
 * }
 * ```
 *
 * @return A Provider of String representing the dependency in the format "pluginId:pluginId.gradle.plugin:version"
 */
fun Provider<PluginDependency>.asLibrary(): Provider<String> {
    return map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
}
