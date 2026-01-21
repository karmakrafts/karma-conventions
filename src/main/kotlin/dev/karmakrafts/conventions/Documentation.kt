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
import java.time.ZonedDateTime

fun Project.defaultDokkaConfig( // @formatter:off
    homepageLink: String? = "https://docs.karmakrafts.dev",
    copyright: String = "&copy; ${ZonedDateTime.now().year} Karma Krafts",
    publishDocs: Boolean = true
) { // @formatter:on
    check(pluginManager.hasPlugin("org.jetbrains.dokka")) { "Dokka plugin must be applied" }
    // Configure Dokka moduleName from project and set appropriate documentation footer copyright
    extensions.getByType<DokkaExtension>().apply {
        moduleName.set(this@defaultDokkaConfig.name)
        pluginsConfiguration.withType<DokkaHtmlPluginParameters> {
            this.homepageLink.set(homepageLink)
            footerMessage.set(copyright)
        }
    }
    // Configure publishing task when enabled and root directory is provided
    // Set up a dokkaJar task
    val dokkaHtmlJar = tasks.register("dokkaHtmlJar", Jar::class) { // @formatter:off
        group = "dokka"
        archiveClassifier.set("javadoc")
        from(tasks.named("dokkaGeneratePublicationHtml", DokkaGenerateTask::class.java)
            .flatMap { task -> task.outputDirectory })
    } // @formatter:on
    if (publishDocs) System.getProperty("publishDocs.root")?.let { docsDir ->
        tasks.register("publishDocs", Copy::class) {
            dependsOn(dokkaHtmlJar)
            mustRunAfter(dokkaHtmlJar)
            from(zipTree(dokkaHtmlJar.map { task -> task.outputs.files.first() }))
            into("$docsDir/${project.name}")
        }
    }
    // Attach generated JAR to all maven publications of this project if plugin is present
    if (pluginManager.hasPlugin("maven-publish")) extensions.getByType<PublishingExtension>().apply {
        publications.withType<MavenPublication> {
            artifact(tasks.register("${name}DokkaJar", Jar::class) {
                group = "dokka"
                archiveClassifier.set("javadoc")
                from(
                    tasks.named("dokkaGeneratePublicationHtml", DokkaGenerateTask::class.java)
                        .flatMap { task -> task.outputDirectory })
            })
        }
    }
}