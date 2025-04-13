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

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.register
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists

/**
 * Represents a Git repository that can be cloned and updated as part of the build process.
 *
 * This class creates and manages Gradle tasks for cloning and pulling from a Git repository.
 * It automatically sets up the necessary task dependencies and conditions to ensure the
 * repository is available when needed.
 *
 * @property project The Gradle project this repository is associated with
 * @property name The name of the repository (used for the local directory name)
 * @property address The Git URL of the repository
 * @property tag Optional tag or branch to checkout (if null, uses the default branch)
 * @property group The Gradle task group name for the repository tasks
 */
class GitRepository internal constructor( // @formatter:off
    val project: Project,
    val name: String,
    val address: String,
    val tag: String?,
    val group: String
) { // @formatter:on
    /**
     * The local path where the repository will be cloned.
     */
    val localPath: Path = project.layout.buildDirectory / name

    /**
     * Gradle task that clones the repository if it doesn't exist locally.
     *
     * This task automatically depends on the build directory creation task and
     * will only execute if the repository hasn't been cloned yet.
     */
    val cloneTask: TaskProvider<Exec> = project.tasks.register<Exec>("clone${name.replace("-", "").capitalized()}") {
        group = this@GitRepository.group
        dependsOn(project.tasks.ensureBuildDirectory())
        workingDir = project.layout.buildDirectory.file
        commandLine(tag?.let {
            listOf("git", "clone", "--branch", tag, "--single-branch", address, this@GitRepository.name)
        } ?: listOf("git", "clone", address, this@GitRepository.name))
        onlyIf { localPath.notExists() }
    }

    /**
     * Gradle task that pulls the latest changes from the repository.
     *
     * This task automatically depends on the clone task and will only execute
     * if the repository has already been cloned.
     */
    val pullTask: TaskProvider<Exec> = project.tasks.register<Exec>("pull${name.replace("-", "").capitalized()}") {
        group = this@GitRepository.group
        dependsOn(cloneTask)
        workingDir = localPath.toFile()
        commandLine("git", "pull", "--force")
        onlyIf { localPath.exists() }
    }
}

/**
 * Creates and configures a Git repository for use in a Gradle build.
 *
 * This extension function creates a GitRepository instance that can be used to clone
 * and update external Git repositories as part of the build process. It automatically
 * sets up the necessary Gradle tasks for cloning and pulling.
 *
 * Example usage:
 * ```kotlin
 * val myRepo = project.gitRepository(
 *     name = "my-dependency",
 *     address = "https://github.com/example/repo.git",
 *     tag = "v1.0.0"
 * )
 *
 * // Now you can depend on the repository tasks
 * tasks.named("build").dependsOn(myRepo.pullTask)
 * ```
 *
 * @param name The name of the repository (used for the local directory name)
 * @param address The Git URL of the repository
 * @param tag Optional tag or branch to checkout (if null, uses the default branch)
 * @param group The Gradle task group name for the repository tasks (defaults to the repository name)
 * @return A GitRepository instance that can be used to reference the repository and its tasks
 */
fun Project.gitRepository( // @formatter:off
    name: String,
    address: String,
    tag: String? = null,
    group: String = name
): GitRepository { // @formatter:on
    return GitRepository(this, name, address, tag, group)
}
