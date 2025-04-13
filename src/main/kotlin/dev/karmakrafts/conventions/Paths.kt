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

import org.gradle.api.file.DirectoryProperty
import java.io.File
import java.nio.file.Path
import kotlin.io.path.div

/**
 * Provides direct access to the File object of a DirectoryProperty.
 * 
 * This extension property allows convenient access to the underlying File object
 * of a Gradle DirectoryProperty without having to call get().asFile.
 * 
 * @return The File object representing this DirectoryProperty
 */
inline val DirectoryProperty.file: File
    get() = get().asFile

/**
 * Provides direct access to the Path object of a DirectoryProperty.
 * 
 * This extension property allows convenient access to the underlying Path object
 * of a Gradle DirectoryProperty without having to call get().asFile.toPath().
 * 
 * @return The Path object representing this DirectoryProperty
 */
inline val DirectoryProperty.path: Path
    get() = get().asFile.toPath()

/**
 * Allows using the division operator to append a string path component to a DirectoryProperty.
 * 
 * This operator function enables a more concise and readable syntax for creating paths
 * by combining a DirectoryProperty with a string path component.
 * 
 * Example:
 * ```kotlin
 * val newPath = project.layout.buildDirectory / "generated"
 * ```
 * 
 * @param other The string path component to append
 * @return A new Path that combines this DirectoryProperty with the given string
 */
operator fun DirectoryProperty.div(other: String): Path = path / other

/**
 * Allows using the division operator to append a Path to a DirectoryProperty.
 * 
 * This operator function enables a more concise and readable syntax for creating paths
 * by combining a DirectoryProperty with another Path.
 * 
 * Example:
 * ```kotlin
 * val subPath = Paths.get("subdir")
 * val newPath = project.layout.buildDirectory / subPath
 * ```
 * 
 * @param other The Path to append
 * @return A new Path that combines this DirectoryProperty with the given Path
 */
operator fun DirectoryProperty.div(other: Path): Path = path / other
