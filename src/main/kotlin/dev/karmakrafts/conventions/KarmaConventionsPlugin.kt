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

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The main plugin class for Karma Conventions.
 *
 * This plugin provides a set of standardized build conventions for Kotlin projects.
 * It can be applied to any Gradle project to enable these conventions.
 */
open class KarmaConventionsPlugin : Plugin<Project> {
    /**
     * Applies the Karma Conventions plugin to the target project.
     *
     * This method is called when the plugin is applied to a project. It logs the
     * current version of the plugin and sets up any necessary configurations.
     *
     * @param target The Gradle project to which this plugin is being applied
     */
    override fun apply(target: Project) {
        target.logger.info("Using Karma Conventions ${BuildInfo.version}")
    }
}
