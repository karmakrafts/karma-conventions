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

package dev.karmakrafts.conventions

import org.gradle.api.Project
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.gradle.kotlin.dsl.register

/**
 * Configures default dependency locking for a Gradle project.
 *
 * This function enables dependency locking for all configurations in the project,
 * which helps ensure reproducible builds by fixing dependency versions.
 * It also creates a "dependenciesForAll" task that can be used to generate
 * a comprehensive dependency report.
 *
 * Dependency locking is particularly useful in multi-module projects to ensure
 * consistent dependency versions across all modules.
 *
 * Example usage:
 * ```kotlin
 * project.defaultDependencyLocking()
 * ```
 */
fun Project.defaultDependencyLocking() {
    dependencyLocking {
        lockAllConfigurations()
    }
    tasks.register<DependencyReportTask>("dependenciesForAll")
}
