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
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask
import java.net.URI
import java.time.ZonedDateTime
import kotlin.io.path.div

@PublishedApi
internal fun Project.registerDefaultDokkaJar(publishDocs: Boolean) {
    val dokkaHtmlJar = tasks.register("dokkaHtmlJar", Jar::class) { // @formatter:off
        group = "dokka"
        archiveClassifier.set("javadoc")
        // @formatter:off
        from(tasks.named("dokkaGeneratePublicationHtml", DokkaGenerateTask::class.java)
            .flatMap { task -> task.outputDirectory })
        // @formatter:on
    } // @formatter:on
    if (publishDocs) System.getProperty("publishDocs.root")?.let { docsDir ->
        tasks.register("publishDocs", Copy::class) {
            dependsOn(dokkaHtmlJar)
            mustRunAfter(dokkaHtmlJar)
            from(zipTree(dokkaHtmlJar.map { task -> task.outputs.files.first() }))
            into("$docsDir/${project.name}")
        }
    }
}

@PublishedApi
internal fun Project.registerPublicationDokkaJars() {
    if (pluginManager.hasPlugin("maven-publish")) extensions.getByType<PublishingExtension>().apply {
        publications.withType<MavenPublication> publication@{
            val dokkaJar = tasks.register("${name}DokkaJar", Jar::class) {
                group = "dokka"
                archiveClassifier.set("javadoc")
                // Each archive name should be distinct, to avoid implicit dependency issues.
                // We use the same format as the sources Jar tasks.
                // https://youtrack.jetbrains.com/issue/KT-46466
                archiveBaseName.set("${archiveBaseName.get()}-${this@publication.name}")
                // @formatter:off
                from(tasks.named("dokkaGeneratePublicationHtml", DokkaGenerateTask::class.java)
                    .flatMap { task -> task.outputDirectory })
                // @formatter:on
            }
            artifact(dokkaJar)
        }
    }
}

inline fun Project.configureDokka( // @formatter:off
    homepageLink: String = "https://docs.karmakrafts.dev",
    copyright: String = "&copy; ${ZonedDateTime.now().year} Karma Krafts",
    publishDocs: Boolean = true,
    crossinline config: DocumentationConfigSpec = {}
) { // @formatter:on
    check(pluginManager.hasPlugin(PluginIds.DOKKA)) { "Dokka plugin must be applied" }
    val config = DocumentationConfig(this, homepageLink).apply(config)
    val dependencyProjects = config.resolveProjects()
    val dependencyWebsites = config.getWebsites()
    extensions.getByType<DokkaExtension>().apply {
        moduleName.set(this@configureDokka.name)
        pluginsConfiguration.withType<DokkaHtmlPluginParameters> {
            this.homepageLink.set(homepageLink)
            footerMessage.set(copyright)
        }
        // All dependency projects configured in the closure need to be registered with their package-list files
        dokkaSourceSets.configureEach {
            externalDocumentationLinks.apply {
                for ((url, project) in dependencyProjects) {
                    register(project.path) {
                        this.url.set(URI.create(url))
                        packageListUrl.set(project.layout.buildDirectory.asFile.map { dir ->
                            (dir.toPath() / "dokka" / "html" / project.name / "package-list").toUri()
                        })
                    }
                }
                for ((url, website) in dependencyWebsites) {
                    register(url) {
                        this.url.set(URI.create(url))
                        packageListUrl.set(URI.create(website.packageList))
                    }
                }
            }
        }
    }
    // Make sure task dependencies between modules are wired correctly
    tasks.named("dokkaGenerate") {
        for ((_, project) in dependencyProjects) {
            project.tasks.findByName("dokkaGenerate")?.let { task ->
                dependsOn(task)
            }
        }
    }
    registerDefaultDokkaJar(publishDocs)
    registerPublicationDokkaJars()
}