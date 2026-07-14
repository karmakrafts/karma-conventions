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

package dev.karmakrafts.conventions.dokka

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

@ConsistentCopyVisibility
data class DocumentationConfig @PublishedApi internal constructor(
    internal val project: Project,
    internal val defaultBaseUrl: String,
    @PublishedApi internal val dependencies: HashSet<Pair<String, DocumentationDependency>> = HashSet()
) {
    fun dependsOn(url: String, project: ProjectDependency) {
        dependencies += url to DocumentationDependency.Project(project.path)
    }

    fun dependsOn(project: ProjectDependency) =
        dependsOn("${defaultBaseUrl.removeSuffix("/")}/${project.name}", project)

    fun dependsOn( // @formatter:off
        url: String,
        packageListName: String = "package-list",
        packageList: String = "${url.removeSuffix("/")}/$packageListName"
    ) { // @formatter:on
        dependencies += url to DocumentationDependency.Website(packageList)
    }

    @PublishedApi
    internal fun resolveProjects(): List<Pair<String, Project>> =
        dependencies.filter { (_, dep) -> dep is DocumentationDependency.Project }
            .map { (url, dep) -> url to project.project((dep as DocumentationDependency.Project).path) }

    @PublishedApi
    internal fun getWebsites(): List<Pair<String, DocumentationDependency.Website>> =
        dependencies.filter { (_, dep) -> dep is DocumentationDependency.Website }
            .map { (url, dep) -> url to (dep as DocumentationDependency.Website) }
}