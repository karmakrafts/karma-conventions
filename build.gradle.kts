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
import java.time.ZonedDateTime
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.writeText

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.gradleNexus)
    `kotlin-dsl`
    `maven-publish`
    signing
}

group = "dev.karmakrafts.conventions"

val baseVersion = libs.versions.karma.conventions
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

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    val versionString = rootProject.version.toString()
    val buildDirPath = layout.buildDirectory.asFile.get().toPath()
    val createVersionFile by registering(DefaultTask::class) {
        val path = (buildDirPath / "generated" / "karma-conventions.version")
        outputs.file(path)
        outputs.upToDateWhen { false } // Always re-generate this file
        doFirst {
            path.deleteIfExists()
            path.parent.createDirectories()
            path.writeText(versionString)
        }
    }
    processResources { dependsOn(createVersionFile) }
    compileKotlin { dependsOn(processResources) }
    val sourcesJar by getting {
        dependsOn(compileJava)
        dependsOn(compileTestJava)
    }
    val javadocJar = named<Jar>("javadocJar") {
        dependsOn(dokkaGeneratePublicationHtml)
        from(dokkaGeneratePublicationHtml)
    }
    System.getProperty("publishDocs.root")?.let { docsDir ->
        register("publishDocs", Copy::class) {
            dependsOn(javadocJar)
            mustRunAfter(javadocJar)
            from(zipTree(javadocJar.get().outputs.files.first()))
            into(docsDir)
        }
    }
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
    compileOnly(libs.plugins.gradleNexus.asLibrary())
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

dokka {
    moduleName = project.name
    pluginsConfiguration {
        html {
            footerMessage = "(c) ${ZonedDateTime.now().year} Karma Krafts & associates"
        }
    }
}

nexusPublishing {
    repositories {
        System.getenv("OSSRH_USERNAME")?.let { userName ->
            sonatype {
                nexusUrl = URI.create("https://ossrh-staging-api.central.sonatype.com/service/local/")
                snapshotRepositoryUrl = URI.create("https://central.sonatype.com/repository/maven-snapshots/")
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
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = project.name
            description = "Karma Krafts conventions and utilities plugin for Gradle"
            url = "https://git.karmakrafts.dev/kk/karma-conventions"
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
                url = "https://github.com/karmakrafts/karma-conventions"
                connection = "scm:git:https://github.com/karmakrafts/karma-conventions.git"
                developerConnection = "scm:git:ssh://git@github.com/karmakrafts/karma-conventions.git"
            }
            issueManagement {
                system = "GitLab"
                url = "${this@pom.url}/-/issues"
            }
            ciManagement {
                system = "GitLab"
                url = "${this@pom.url}/-/pipelines"
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
signing {
    System.getenv("SIGNING_KEY_ID")?.let { keyId ->
        useInMemoryPgpKeys( // @formatter:off
            keyId,
            System.getenv("SIGNING_PRIVATE_KEY")?.let { encodedKey ->
                Base64.decode(encodedKey).decodeToString()
            },
            System.getenv("SIGNING_PASSWORD")
        ) // @formatter:on
    }
    sign(publishing.publications)
}