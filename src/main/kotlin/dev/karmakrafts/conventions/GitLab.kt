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

import kotlinx.serialization.Serializable
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.maybeCreate
import org.gradle.kotlin.dsl.withType
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.exists

class GitLabServer internal constructor( // @formatter:off
    val project: Project,
    val address: String
) { // @formatter:on
    private val projects: HashMap<String, GitLabProject> = HashMap()
    val apiUrl: String = "https://$address/api/v4"

    fun project(path: String, name: String = path.substringAfterLast('/')): GitLabProject {
        val endpoint = "$apiUrl/projects/${path.percentEncode()}"
        return projects.getOrPut(endpoint) { GitLabProject(this, endpoint, name, null) }
    }

    fun project(id: Long, name: String = id.toString()): GitLabProject {
        val endpoint = "$apiUrl/projects/$id"
        return projects.getOrPut(endpoint) { GitLabProject(this, endpoint, name, id) }
    }
}

@Serializable
data class GitLabProjectInfo(
    val id: Long
)

class GitLabProject internal constructor( // @formatter:off
    val server: GitLabServer,
    val endpoint: String,
    val name: String,
    givenId: Long?
) { // @formatter:on
    val projectInfo: GitLabProjectInfo = requireNotNull(fetch(endpoint)) { "Could not fetch project info" }
    val id: Long = givenId ?: projectInfo.id
    val endpointWithId: String = "${server.apiUrl}/projects/$id"
    val packageRegistry: GitLabPackageRegistry = GitLabPackageRegistry(this, "$endpointWithId/packages")
}

class GitLabPackageRegistry internal constructor( // @formatter:off
    val project: GitLabProject,
    val endpoint: String
) { // @formatter:on
    private val packages: HashMap<String, GitLabPackage> = HashMap()

    operator fun get(path: String): GitLabPackage {
        val url = "$endpoint/$path"
        return packages.getOrPut(url) { GitLabPackage(this, url) }
    }

    operator fun get(path: String, version: String): GitLabPackage {
        val url = "$endpoint/$path/$version"
        return packages.getOrPut(url) { GitLabPackage(this, url) }
    }

    operator fun get(path: String, version: Provider<in String>): GitLabPackage {
        val url = "$endpoint/$path/${version.get()}"
        return packages.getOrPut(url) { GitLabPackage(this, url) }
    }
}

fun RepositoryHandler.packageRegistry(registry: GitLabPackageRegistry) {
    maven("${registry.endpoint}/maven")
}

fun RepositoryHandler.packageRegistry(project: GitLabProject) {
    maven("${project.packageRegistry.endpoint}/maven")
}

class GitLabPackage internal constructor( // @formatter:off
    val packageRegistry: GitLabPackageRegistry,
    val url: String
) { // @formatter:on
    private val artifacts: HashMap<String, GitLabPackageArtifact> = HashMap()

    private fun getArtifactKey(fileName: String, suffix: String, directoryName: String): String {
        return if (suffix.isEmpty()) fileName
        else "$fileName:$suffix@$directoryName"
    }

    operator fun get(
        fileName: String, suffix: String = "", directoryName: String = packageRegistry.project.name
    ): GitLabPackageArtifact {
        return artifacts.getOrPut(getArtifactKey(fileName, suffix, directoryName)) {
            GitLabPackageArtifact(
                this, url, fileName, suffix, directoryName
            )
        }
    }

    operator fun get(
        fileName: Provider<String>, suffix: String = "", directoryName: String = packageRegistry.project.name
    ): GitLabPackageArtifact {
        val actualFileName = fileName.get()
        return artifacts.getOrPut(getArtifactKey(actualFileName, suffix, directoryName)) {
            GitLabPackageArtifact(
                this, url, actualFileName, suffix, directoryName
            )
        }
    }
}

class GitLabPackageArtifact internal constructor(
    val packageInstance: GitLabPackage,
    val packageUrl: String,
    val fileName: String,
    val suffix: String,
    val directoryName: String
) {
    private val projectName: String = packageInstance.packageRegistry.project.name
    private val project: Project = packageInstance.packageRegistry.project.server.project
    val localDirectoryPath: Path by lazy { project.layout.buildDirectory / directoryName }
    val localPath: Path by lazy { localDirectoryPath / fileName }

    val outputDirectoryPath: Path by lazy {
        if (suffix.isBlank()) localDirectoryPath
        else localDirectoryPath / suffix
    }

    val downloadTask: Task =
        project.tasks.maybeCreate("download${projectName.capitalized()}${suffix.capitalized()}").apply {
            group = projectName
            doLast {
                val url = "$packageUrl/$fileName"
                project.logger.lifecycle("Downloading $url..")
                localPath.createDirectories()
                fetchRaw(url)?.use {
                    Files.copy(it, localPath, StandardCopyOption.REPLACE_EXISTING)
                }
                project.logger.lifecycle("Downloaded $localPath")
            }
            onlyIf { !localPath.exists() }
        }

    val extractTask: Copy = project.tasks.maybeCreate(
        "extract${projectName.capitalized()}${suffix.capitalized()}", Copy::class
    ).apply {
        group = projectName
        dependsOn(downloadTask)
        mustRunAfter(downloadTask)
        from(project.zipTree(localPath.toFile()))
        into(outputDirectoryPath.toFile())
        doLast { project.logger.lifecycle("Extracted $localPath") }
    }

    val cleanTask: Task = project.tasks.maybeCreate("clean${projectName.capitalized()}${suffix.capitalized()}").apply {
        doLast {
            localPath.deleteIfExists()
            project.logger.lifecycle("Removed $localPath")
        }
    }
}

private val gitlabServers: HashMap<String, GitLabServer> = HashMap()

fun Project.gitlab(
    address: String = "git.karmakrafts.dev"
): GitLabServer = gitlabServers.getOrPut(address) { GitLabServer(this, address) }

object GitLabCI {
    val isCI: Boolean
        get() = System.getenv("CI_PROJECT_ID") != null

    fun getDefaultVersion(baseVersion: Provider<String>): String {
        return System.getenv("CI_COMMIT_TAG")?.let { baseVersion.get() }
            ?: "${baseVersion.get()}.${System.getenv("CI_PIPELINE_IID") ?: 0}-SNAPSHOT"
    }

    /**
     * Adds GitLab Package Registry as a publishing repository for the current project using
     * environment provided CI_API_V4_URL, CI_PROJECT_ID and CI_JOB_TOKEN variables.
     */
    fun RepositoryHandler.authenticatedPackageRegistry() {
        System.getenv("CI_API_V4_URL")?.let { apiUrl ->
            maven {
                url = URI.create("$apiUrl/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
                name = "GitLab"
                credentials(HttpHeaderCredentials::class) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN")
                }
                authentication {
                    create("header", HttpHeaderAuthentication::class)
                }
            }
        }
    }

    fun MavenPom.defaultIssueManagement() {
        System.getenv("CI_PROJECT_URL")?.let { projectUrl ->
            issueManagement {
                system.set("GitLab")
                url.set("$projectUrl/-/issues")
            }
        }
    }

    fun MavenPom.defaultCiManagement() {
        System.getenv("CI_PROJECT_URL")?.let { projectUrl ->
            ciManagement {
                system.set("GitLab")
                url.set("$projectUrl/-/pipelines")
            }
        }
    }

    fun PublishingExtension.gitlabDefaults() {
        repositories {
            authenticatedPackageRegistry()
        }
        publications.withType<MavenPublication>().configureEach {
            pom {
                defaultIssueManagement()
                defaultCiManagement()
            }
        }
    }

    fun PublishingExtension.karmaKraftsDefaults() {
        gitlabDefaults()
        publications.withType<MavenPublication>().configureEach {
            pom {
                karmaKraftsOrganization()
            }
        }
    }

    fun PublishingExtension.karmaStudiosDefaults() {
        gitlabDefaults()
        publications.withType<MavenPublication>().configureEach {
            pom {
                karmaStudiosOrganization()
            }
        }
    }

    fun Project.defaultDependencyLocking() {
        if (isCI) {
            dependencyLocking {
                lockAllConfigurations()
            }
            tasks.maybeCreate("dependenciesForAll", DependencyReportTask::class)
        }
    }
}