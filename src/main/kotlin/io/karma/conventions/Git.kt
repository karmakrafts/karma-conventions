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

package io.karma.conventions

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.maybeCreate
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists

class GitRepository internal constructor( // @formatter:off
    val project: Project,
    val name: String,
    val address: String,
    val tag: String?,
    val group: String
) { // @formatter:on
    val localPath: Path = project.layout.buildDirectory / name

    val cloneTask: Exec = project.tasks.maybeCreate("clone${name.replace("-", "").capitalized()}", Exec::class).apply {
        group = this@GitRepository.group
        dependsOn(project.tasks.ensureBuildDirectory())
        workingDir = project.layout.buildDirectory.file
        commandLine(tag?.let {
            listOf("git", "clone", "--branch", tag, "--single-branch", address, this@GitRepository.name)
        } ?: listOf("git", "clone", address, this@GitRepository.name))
        onlyIf { localPath.notExists() }
    }

    val pullTask: Exec = project.tasks.maybeCreate("pull${name.replace("-", "").capitalized()}", Exec::class).apply {
        group = this@GitRepository.group
        dependsOn(cloneTask)
        workingDir = localPath.toFile()
        commandLine("git", "pull", "--force")
        onlyIf { localPath.exists() }
    }
}

fun Project.gitRepository( // @formatter:off
    name: String,
    address: String,
    tag: String? = null,
    group: String = name
): GitRepository { // @formatter:on
    return GitRepository(this, name, address, tag, group)
}
