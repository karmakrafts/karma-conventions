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

import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

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

val KonanTarget.architectureName: String
    get() = architecture.name.lowercase()

fun KonanTarget.toTargetPair(): String = "${familyName}-${architectureName}"