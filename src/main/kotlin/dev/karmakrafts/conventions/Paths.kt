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

inline val DirectoryProperty.file: File
    get() = get().asFile

inline val DirectoryProperty.path: Path
    get() = get().asFile.toPath()

operator fun DirectoryProperty.div(other: String): Path = path / other

operator fun DirectoryProperty.div(other: Path): Path = path / other