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

/**
 * Internal object that provides build-related information for the Karma Conventions plugin.
 * 
 * This object contains properties that expose metadata about the current build,
 * such as version information.
 */
internal object BuildInfo {
    /**
     * The current version of the Karma Conventions plugin.
     * 
     * This property reads the version string from the "/karma-conventions.version" resource file.
     * The version is loaded once when the object is initialized.
     */
    val version: String =
        BuildInfo::class.java.getResourceAsStream("/karma-conventions.version")?.bufferedReader().use {
            it?.readText()
        }!!
}
