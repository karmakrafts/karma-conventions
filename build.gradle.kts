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

import java.net.URI
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.writeText

plugins {
    `kotlin-dsl`
    `maven-publish`
    signing
    alias(libs.plugins.kotlinx.serialization)
}

group = "dev.karmakrafts.conventions"

val baseVersion = libs.versions.karmaConventions
version = System.getenv("CI_COMMIT_TAG")?.let { baseVersion.get() }
    ?: "${baseVersion.get()}.${System.getenv("CI_PIPELINE_IID") ?: 0}-SNAPSHOT"

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
    sourceSets {
        main {
            resources.srcDir("build/generated")
        }
    }
}

tasks {
    val createVersionFile by registering {
        doFirst {
            val path = (layout.buildDirectory.asFile.get().toPath() / "generated" / "karma-conventions.version")
            path.deleteIfExists()
            path.parent.createDirectories()
            path.writeText(rootProject.version.toString())
        }
        outputs.upToDateWhen { false } // Always re-generate this file
    }
    processResources { dependsOn(createVersionFile) }
    compileKotlin { dependsOn(processResources) }
}

fun Provider<PluginDependency>.asLibrary(): Provider<String> {
    return map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    compileOnly(libs.plugins.kotlin.multiplatform.asLibrary())
    compileOnly(libs.plugins.kotlin.jvm.asLibrary())
    compileOnly(libs.plugins.kotlinx.kover.asLibrary())
    compileOnly(libs.plugins.android.library.asLibrary())
    compileOnly(libs.plugins.android.application.asLibrary())
}

gradlePlugin {
    plugins {
        create("KarmaConventions") {
            id = "$group.${rootProject.name}" // io.karma.conventions.karma-conventions
            implementationClass = "$group.KarmaConventionsPlugin"
            displayName = name
        }
    }
}

System.getenv("CI_PROJECT_ID")?.let {
    dependencyLocking {
        lockAllConfigurations()
    }
    tasks {
        val dependenciesForAll by registering(DependencyReportTask::class)
    }
}

fun RepositoryHandler.authenticatedMavenCentral() {
    System.getenv("OSSRH_USERNAME")?.let { userName ->
        maven {
            url = URI.create("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            name = "MavenCentral"
            credentials {
                username = userName
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

fun RepositoryHandler.authenticatedMavenCentralSnapshots() {
    System.getenv("OSSRH_USERNAME")?.let { userName ->
        maven {
            url = URI.create("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            name = "MavenCentral"
            credentials {
                username = userName
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

publishing {
    repositories {
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
        if (version.toString().endsWith("-SNAPSHOT")) authenticatedMavenCentralSnapshots()
        else authenticatedMavenCentral()
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = project.name
            description = "Karma Krafts conventions and utilities plugin for Gradle."
            url = System.getenv("CI_PROJECT_URL")
            licenses {
                license {
                    name = "Apache License 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0"
                }
            }
            organization {
                name = "Karma Krafts"
                url = "https://git.karmakrafts.dev/kk"
            }
            developers {
                developer {
                    id = "karmakrafts"
                    name = "Karma Krafts"
                    url = "https://git.karmakrafts.dev/kk"
                    organization = "Karma Krafts"
                    organizationUrl = "https://git.karmakrafts.dev/kk"
                }
            }
            scm {
                url = this@pom.url
            }
            issueManagement {
                system = "GitLab"
                url = "${System.getenv("CI_PROJECT_URL")}/-/issues"
            }
            ciManagement {
                system = "GitLab"
                url = "${System.getenv("CI_PROJECT_URL")}/-/pipelines"
            }
        }
    }
}

signing {
    System.getenv("SIGNING_KEY_ID")?.let { keyId ->
        useInMemoryPgpKeys( // @formatter:off
            keyId,
            System.getenv("SIGNING_PRIVATE_KEY"),
            System.getenv("SIGNING_PASSWORD")
        ) // @formatter:on
        for (publication in project.extensions.getByType(PublishingExtension::class).publications) {
            sign(publication)
        }
    }
}