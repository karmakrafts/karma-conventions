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

import dev.karmakrafts.conventions.PluginIds
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

@ConsistentCopyVisibility
data class DocumentationConfig @PublishedApi internal constructor(
    private val project: Project,
    private val defaultBaseUrl: String,
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

    fun withJava() {
        val pluginManager = project.pluginManager
        check(
            pluginManager.hasPlugin(PluginIds.JAVA) //
                || pluginManager.hasPlugin(PluginIds.KOTLIN_JVM) //
                || pluginManager.hasPlugin(PluginIds.KOTLIN_MP)
        ) {
            "withJava requires Java, Kotlin JVM or Kotlin Multiplatform plugin to be applied"
        }
        val version = project.extensions.getByType<JavaPluginExtension>().toolchain.languageVersion.get().asInt()
        dependsOn( // @formatter:off
            url = "https://docs.oracle.com/en/java/javase/$version/docs/api",
            packageListName = "element-list"
        ) // @formatter:on
    }

    fun withKotlin() = dependsOn("https://kotlinlang.org/api/core")
    fun withKotlinxCoroutines() = dependsOn("https://kotlinlang.org/api/kotlinx.coroutines")
    fun withKotlinxIo() = dependsOn("https://kotlinlang.org/api/kotlinx-io")
    fun withKotlinxDateTime() = dependsOn("https://kotlinlang.org/api/kotlinx-datetime")
    fun withKotlinxSerialization() = dependsOn("https://kotlinlang.org/api/kotlinx.serialization")
    fun withKtor() = dependsOn("https://api.ktor.io")

    fun withGradle() = dependsOn( // @formatter:off
        url = "https://docs.gradle.org/${project.gradle.gradleVersion}/javadoc",
        packageListName = "element-list"
    ) // @formatter:on

    fun withAndroidGradle(version: Provider<String>) {
        dependsOn("https://developer.android.com/reference/tools/gradle-api/${version.get()}")
    }

    fun withKotlinGradle() = dependsOn("https://kotlinlang.org/api/kotlin-gradle-plugin")

    @PublishedApi
    internal fun resolveProjects(): List<Pair<String, Project>> =
        dependencies.filter { (_, dep) -> dep is DocumentationDependency.Project }
            .map { (url, dep) -> url to project.project((dep as DocumentationDependency.Project).path) }

    @PublishedApi
    internal fun getWebsites(): List<Pair<String, DocumentationDependency.Website>> =
        dependencies.filter { (_, dep) -> dep is DocumentationDependency.Website }
            .map { (url, dep) -> url to (dep as DocumentationDependency.Website) }
}