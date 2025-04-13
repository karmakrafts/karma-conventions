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

/**
 * Represents a GitLab server instance for interacting with GitLab API.
 * 
 * This class provides methods to access GitLab projects and their resources
 * through the GitLab API.
 * 
 * @property project The Gradle project this GitLab server is associated with
 * @property address The address of the GitLab server (e.g., "gitlab.com")
 */
class GitLabServer internal constructor( // @formatter:off
    val project: Project,
    val address: String
) { // @formatter:on
    private val projects: HashMap<String, GitLabProject> = HashMap()

    /**
     * The base URL for the GitLab API.
     */
    val apiUrl: String = "https://$address/api/v4"

    /**
     * Gets a GitLab project by its path.
     * 
     * @param path The path of the project (e.g., "group/project")
     * @param name A custom name for the project (defaults to the last part of the path)
     * @return A GitLabProject instance representing the specified project
     */
    fun project(path: String, name: String = path.substringAfterLast('/')): GitLabProject {
        val endpoint = "$apiUrl/projects/${path.percentEncode()}"
        return projects.getOrPut(endpoint) { GitLabProject(this, endpoint, name, null) }
    }

    /**
     * Gets a GitLab project by its ID.
     * 
     * @param id The numeric ID of the project
     * @param name A custom name for the project (defaults to the ID as a string)
     * @return A GitLabProject instance representing the specified project
     */
    fun project(id: Long, name: String = id.toString()): GitLabProject {
        val endpoint = "$apiUrl/projects/$id"
        return projects.getOrPut(endpoint) { GitLabProject(this, endpoint, name, id) }
    }
}

/**
 * Data class representing basic information about a GitLab project.
 * 
 * This class is used to deserialize project information from the GitLab API.
 * 
 * @property id The numeric ID of the GitLab project
 */
@Serializable
data class GitLabProjectInfo(
    val id: Long
)

/**
 * Represents a GitLab project and provides access to its resources.
 * 
 * This class encapsulates a GitLab project and provides access to its
 * package registry and other resources through the GitLab API.
 * 
 * @property server The GitLab server this project belongs to
 * @property endpoint The API endpoint for this project
 * @property name The name of the project
 * @param givenId The optional ID of the project (if not provided, it will be fetched from the API)
 */
class GitLabProject internal constructor( // @formatter:off
    val server: GitLabServer,
    val endpoint: String,
    val name: String,
    givenId: Long?
) { // @formatter:on
    /**
     * The project information fetched from the GitLab API.
     */
    val projectInfo: GitLabProjectInfo = requireNotNull(fetch(endpoint)) { "Could not fetch project info" }

    /**
     * The numeric ID of the project.
     */
    val id: Long = givenId ?: projectInfo.id

    /**
     * The API endpoint for this project with the ID included.
     */
    val endpointWithId: String = "${server.apiUrl}/projects/$id"

    /**
     * The package registry for this project.
     */
    val packageRegistry: GitLabPackageRegistry = GitLabPackageRegistry(this, "$endpointWithId/packages")
}

/**
 * Represents a GitLab package registry for a project.
 * 
 * This class provides access to packages in a GitLab project's package registry.
 * 
 * @property project The GitLab project this package registry belongs to
 * @property endpoint The API endpoint for this package registry
 */
class GitLabPackageRegistry internal constructor( // @formatter:off
    val project: GitLabProject,
    val endpoint: String
) { // @formatter:on
    private val packages: HashMap<String, GitLabPackage> = HashMap()

    /**
     * Gets a package by its path.
     * 
     * @param path The path of the package
     * @return A GitLabPackage instance representing the specified package
     */
    operator fun get(path: String): GitLabPackage {
        val url = "$endpoint/$path"
        return packages.getOrPut(url) { GitLabPackage(this, url) }
    }

    /**
     * Gets a package by its path and version.
     * 
     * @param path The path of the package
     * @param version The version of the package
     * @return A GitLabPackage instance representing the specified package version
     */
    operator fun get(path: String, version: String): GitLabPackage {
        val url = "$endpoint/$path/$version"
        return packages.getOrPut(url) { GitLabPackage(this, url) }
    }

    /**
     * Gets a package by its path and version provider.
     * 
     * @param path The path of the package
     * @param version A provider for the version of the package
     * @return A GitLabPackage instance representing the specified package version
     */
    operator fun get(path: String, version: Provider<in String>): GitLabPackage {
        val url = "$endpoint/$path/${version.get()}"
        return packages.getOrPut(url) { GitLabPackage(this, url) }
    }
}

/**
 * Adds a GitLab package registry as a Maven repository.
 * 
 * This extension function adds a Maven repository that points to the specified
 * GitLab package registry.
 * 
 * @param registry The GitLab package registry to add as a repository
 */
fun RepositoryHandler.packageRegistry(registry: GitLabPackageRegistry) {
    maven("${registry.endpoint}/maven")
}

/**
 * Adds a GitLab project's package registry as a Maven repository.
 * 
 * This extension function adds a Maven repository that points to the package
 * registry of the specified GitLab project.
 * 
 * @param project The GitLab project whose package registry to add as a repository
 */
fun RepositoryHandler.packageRegistry(project: GitLabProject) {
    maven("${project.packageRegistry.endpoint}/maven")
}

/**
 * Represents a package in a GitLab package registry.
 * 
 * This class provides access to artifacts within a GitLab package.
 * 
 * @property packageRegistry The GitLab package registry this package belongs to
 * @property url The URL of this package
 */
class GitLabPackage internal constructor( // @formatter:off
    val packageRegistry: GitLabPackageRegistry,
    val url: String
) { // @formatter:on
    private val artifacts: HashMap<String, GitLabPackageArtifact> = HashMap()

    /**
     * Generates a unique key for an artifact based on its file name, suffix, and directory name.
     * 
     * @param fileName The name of the artifact file
     * @param suffix The suffix to append to the artifact key
     * @param directoryName The directory name for the artifact
     * @return A unique key for the artifact
     */
    private fun getArtifactKey(fileName: String, suffix: String, directoryName: String): String {
        return if (suffix.isEmpty()) fileName
        else "$fileName:$suffix@$directoryName"
    }

    /**
     * Gets an artifact by its file name, suffix, and directory name.
     * 
     * @param fileName The name of the artifact file
     * @param suffix An optional suffix for the artifact (default is empty string)
     * @param directoryName The directory name for the artifact (defaults to the project name)
     * @return A GitLabPackageArtifact instance representing the specified artifact
     */
    operator fun get(
        fileName: String, suffix: String = "", directoryName: String = packageRegistry.project.name
    ): GitLabPackageArtifact {
        return artifacts.getOrPut(getArtifactKey(fileName, suffix, directoryName)) {
            GitLabPackageArtifact(
                this, url, fileName, suffix, directoryName
            )
        }
    }

    /**
     * Gets an artifact by its file name provider, suffix, and directory name.
     * 
     * @param fileName A provider for the name of the artifact file
     * @param suffix An optional suffix for the artifact (default is empty string)
     * @param directoryName The directory name for the artifact (defaults to the project name)
     * @return A GitLabPackageArtifact instance representing the specified artifact
     */
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

/**
 * Represents an artifact in a GitLab package.
 * 
 * This class provides access to an artifact file and creates Gradle tasks for
 * downloading, extracting, and cleaning the artifact.
 * 
 * @property packageInstance The GitLab package this artifact belongs to
 * @property packageUrl The URL of the package containing this artifact
 * @property fileName The name of the artifact file
 * @property suffix An optional suffix for the artifact
 * @property directoryName The directory name for the artifact
 */
class GitLabPackageArtifact internal constructor(
    val packageInstance: GitLabPackage,
    val packageUrl: String,
    val fileName: String,
    val suffix: String,
    val directoryName: String
) {
    private val projectName: String = packageInstance.packageRegistry.project.name
    private val project: Project = packageInstance.packageRegistry.project.server.project

    /**
     * The local directory path where the artifact will be downloaded.
     */
    val localDirectoryPath: Path by lazy { project.layout.buildDirectory / directoryName }

    /**
     * The local path where the artifact file will be downloaded.
     */
    val localPath: Path by lazy { localDirectoryPath / fileName }

    /**
     * The output directory path where the artifact will be extracted.
     * 
     * If a suffix is provided, the artifact will be extracted to a subdirectory
     * with that name. Otherwise, it will be extracted to the local directory path.
     */
    val outputDirectoryPath: Path by lazy {
        if (suffix.isBlank()) localDirectoryPath
        else localDirectoryPath / suffix
    }

    /**
     * Gradle task that downloads the artifact if it doesn't exist locally.
     * 
     * This task will only execute if the artifact hasn't been downloaded yet.
     */
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

    /**
     * Gradle task that extracts the downloaded artifact.
     * 
     * This task depends on the download task and will extract the artifact
     * to the output directory path.
     */
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

    /**
     * Gradle task that removes the downloaded artifact.
     * 
     * This task can be used to clean up the downloaded artifact file.
     */
    val cleanTask: Task = project.tasks.maybeCreate("clean${projectName.capitalized()}${suffix.capitalized()}").apply {
        doLast {
            localPath.deleteIfExists()
            project.logger.lifecycle("Removed $localPath")
        }
    }
}

private val gitlabServers: HashMap<String, GitLabServer> = HashMap()

/**
 * Gets or creates a GitLab server instance for the specified address.
 * 
 * This extension function provides access to a GitLab server instance that can be
 * used to interact with GitLab projects and their resources.
 * 
 * @param address The address of the GitLab server (defaults to "git.karmakrafts.dev")
 * @return A GitLabServer instance for the specified address
 */
fun Project.gitlab(
    address: String = "git.karmakrafts.dev"
): GitLabServer = gitlabServers.getOrPut(address) { GitLabServer(this, address) }

/**
 * Utility object for working with GitLab CI/CD.
 * 
 * This object provides properties and functions for interacting with GitLab CI/CD
 * environments and configuring projects accordingly.
 */
object GitLabCI {
    /**
     * Indicates whether the current environment is a GitLab CI/CD environment.
     * 
     * This property checks for the presence of the CI_PROJECT_ID environment variable,
     * which is set by GitLab CI/CD.
     */
    val isCI: Boolean
        get() = System.getenv("CI_PROJECT_ID") != null

    /**
     * Generates a default version string based on the GitLab CI/CD environment.
     * 
     * If running in a tagged pipeline (CI_COMMIT_TAG is set), this function returns
     * the base version. Otherwise, it appends the pipeline ID and "-SNAPSHOT" to the
     * base version.
     * 
     * @param baseVersion A provider for the base version
     * @return A version string suitable for the current CI/CD environment
     */
    fun getDefaultVersion(baseVersion: Provider<String>): String {
        return System.getenv("CI_COMMIT_TAG")?.let { baseVersion.get() }
            ?: "${baseVersion.get()}.${System.getenv("CI_PIPELINE_IID") ?: 0}-SNAPSHOT"
    }

    /**
     * Adds GitLab Package Registry as a publishing repository for the current project using
     * environment provided CI_API_V4_URL, CI_PROJECT_ID and CI_JOB_TOKEN variables.
     * 
     * This extension function configures a Maven repository that points to the GitLab
     * package registry of the current project, using authentication credentials from
     * the GitLab CI/CD environment.
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

    /**
     * Configures default issue management information in a Maven POM.
     * 
     * This extension function sets up the issue management section of a Maven POM
     * to point to the GitLab issues page of the current project.
     */
    fun MavenPom.defaultIssueManagement() {
        System.getenv("CI_PROJECT_URL")?.let { projectUrl ->
            issueManagement {
                system.set("GitLab")
                url.set("$projectUrl/-/issues")
            }
        }
    }

    /**
     * Configures default CI management information in a Maven POM.
     * 
     * This extension function sets up the CI management section of a Maven POM
     * to point to the GitLab pipelines page of the current project.
     */
    fun MavenPom.defaultCiManagement() {
        System.getenv("CI_PROJECT_URL")?.let { projectUrl ->
            ciManagement {
                system.set("GitLab")
                url.set("$projectUrl/-/pipelines")
            }
        }
    }

    /**
     * Configures default GitLab-related settings for publishing.
     * 
     * This extension function sets up the publishing extension with GitLab-specific
     * defaults, including the authenticated package registry and default issue and
     * CI management information.
     */
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

    /**
     * Configures default Karma Krafts-specific settings for publishing.
     * 
     * This extension function sets up the publishing extension with Karma Krafts-specific
     * defaults, including GitLab defaults and Karma Krafts organization information.
     */
    fun PublishingExtension.karmaKraftsDefaults() {
        gitlabDefaults()
        publications.withType<MavenPublication>().configureEach {
            pom {
                karmaKraftsOrganization()
            }
        }
    }

    /**
     * Configures default Karma Studios-specific settings for publishing.
     * 
     * This extension function sets up the publishing extension with Karma Studios-specific
     * defaults, including GitLab defaults and Karma Studios organization information.
     */
    fun PublishingExtension.karmaStudiosDefaults() {
        gitlabDefaults()
        publications.withType<MavenPublication>().configureEach {
            pom {
                karmaStudiosOrganization()
            }
        }
    }
}
