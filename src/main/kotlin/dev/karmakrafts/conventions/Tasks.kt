/*
 * Copyright 2025 Karma Krafts
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

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

/**
 * Creates a task that ensures the build directory exists.
 *
 * This function either finds an existing task named "ensureBuildDirectory" or creates a new one.
 * The task will create the build directory if it doesn't exist.
 *
 * @return The task that ensures the build directory exists
 */
fun TaskContainer.ensureBuildDirectory(): Task {
    // Lazily registers this task when called and not present
    return findByName("ensureBuildDirectory") ?: maybeCreate("ensureBuildDirectory").apply {
        val path = project.layout.buildDirectory.get().asFile.toPath()
        doLast { path.createDirectories() }
        onlyIf { path.notExists() }
    }
}
