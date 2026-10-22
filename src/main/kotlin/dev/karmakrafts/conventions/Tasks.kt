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

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

/**
 * Lazily register the task if it doesn't exist yet, otherwise
 * return the existing task matching the given name and type.
 * [configureAction] is only applied when registering a new task.
 */
inline fun <reified T : Task> TaskContainer.maybeRegister(
    name: String, crossinline configureAction: T.() -> Unit = {}
): TaskProvider<T> = when {
    name in names -> named(name, T::class.java)
    else -> register(name, T::class.java) {
        configureAction()
    }
}

/**
 * Creates a task that ensures the build directory exists.
 *
 * This function either finds an existing task named "ensureBuildDirectory" or creates a new one.
 * The task will create the build directory if it doesn't exist. This function realizes the task;
 * use [ensureBuildDirectoryProvider] for lazy task configuration.
 *
 * @return The task that ensures the build directory exists
 */
fun TaskContainer.ensureBuildDirectory(): Task {
    return ensureBuildDirectoryProvider().get()
}

/**
 * Lazily registers or returns the task that ensures the build directory exists.
 * The build directory is resolved when the task runs, so later layout changes are respected.
 */
fun TaskContainer.ensureBuildDirectoryProvider(): TaskProvider<Task> {
    return maybeRegister("ensureBuildDirectory") {
        val directory = project.layout.buildDirectory
        doLast { directory.get().asFile.toPath().createDirectories() }
        onlyIf { directory.get().asFile.toPath().notExists() }
    }
}
