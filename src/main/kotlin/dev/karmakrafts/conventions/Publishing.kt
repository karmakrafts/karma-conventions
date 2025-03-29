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
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomLicenseSpec
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import java.net.URI

/**
 * Adds MavenCentral as a publishing repository using
 * environment provided OSSRH_USERNAME and OSSRH_PASSWORD variables.
 */
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

/**
 * Adds MavenCentral Snapshots as a publishing repository using
 * environment provided OSSRH_USERNAME and OSSRH_PASSWORD variables.
 */
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

fun Project.authenticatedMavenCentral() {
    pluginManager.withPlugin("maven-publish") {
        extensions.getByType<PublishingExtension>().apply {
            repositories {
                if (version.toString().endsWith("-SNAPSHOT")) {
                    authenticatedMavenCentralSnapshots()
                    return@repositories
                }
                authenticatedMavenCentral()
            }
        }
    }
}

fun MavenPom.karmaKraftsOrganization() {
    organization {
        name.set("Karma Krafts")
        url.set("https://git.karmakrafts.dev/kk")
    }
    developers {
        developer {
            id.set("karmakrafts")
            name.set("Karma Krafts")
            url.set("https://git.karmakrafts.dev/kk")
            organization.set("Karma Krafts")
            organizationUrl.set("https://git.karmakrafts.dev/kk")
        }
    }
}

fun MavenPom.karmaStudiosOrganization() {
    organization {
        name.set("Karma Studios")
        url.set("https://git.karmakrafts.dev/karmastudios")
    }
    developers {
        developer {
            id.set("karmastudios")
            name.set("Karma Studios")
            url.set("https://git.karmakrafts.dev/karmastudios")
            organization.set("Karma Studios")
            organizationUrl.set("https://git.karmakrafts.dev/karmastudios")
        }
    }
}

fun MavenPomLicenseSpec.apache2() {
    license {
        name.set("Apache License 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0")
    }
}

fun MavenPomLicenseSpec.mplV2() {
    license {
        name.set("Mozilla Public License 2.0")
        url.set("https://www.mozilla.org/media/MPL/2.0/index.f75d2927d3c1.txt")
    }
}

fun MavenPomLicenseSpec.gplV3() {
    license {
        name.set("GNU General Public License 3.0")
        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
    }
}

fun PublishingExtension.apache2License() {
    publications.withType<MavenPublication>().configureEach {
        pom {
            licenses {
                apache2()
            }
        }
    }
}

fun PublishingExtension.mplV2License() {
    publications.withType<MavenPublication>().configureEach {
        pom {
            licenses {
                mplV2()
            }
        }
    }
}

fun PublishingExtension.gplV3License() {
    publications.withType<MavenPublication>().configureEach {
        pom {
            licenses {
                gplV3()
            }
        }
    }
}

fun PublishingExtension.setProjectInfo( // @formatter:off
    name: String,
    description: String,
    url: String = "https://git.karmakrafts.dev/kk/$name"
) { // @formatter:on
    publications.withType<MavenPublication>().configureEach {
        pom {
            this.name.set(name)
            this.description.set(description)
            this.url.set(url)
        }
    }
}