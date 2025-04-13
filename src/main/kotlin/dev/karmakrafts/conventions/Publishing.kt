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

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomLicenseSpec
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType

/**
 * Configures the Maven POM with Karma Krafts organization information.
 * 
 * This extension function sets up the organization and developers sections
 * of a Maven POM with standard Karma Krafts information.
 * 
 * Example usage:
 * ```kotlin
 * publishing {
 *     publications {
 *         withType<MavenPublication> {
 *             pom {
 *                 karmaKraftsOrganization()
 *             }
 *         }
 *     }
 * }
 * ```
 */
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

/**
 * Configures the Maven POM with Karma Studios organization information.
 * 
 * This extension function sets up the organization and developers sections
 * of a Maven POM with standard Karma Studios information.
 * 
 * Example usage:
 * ```kotlin
 * publishing {
 *     publications {
 *         withType<MavenPublication> {
 *             pom {
 *                 karmaStudiosOrganization()
 *             }
 *         }
 *     }
 * }
 * ```
 */
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

/**
 * Adds the Apache License 2.0 to a Maven POM license specification.
 * 
 * This extension function configures a license entry for the Apache License 2.0
 * in the licenses section of a Maven POM.
 */
fun MavenPomLicenseSpec.apache2() {
    license {
        name.set("Apache License 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0")
    }
}

/**
 * Adds the Mozilla Public License 2.0 to a Maven POM license specification.
 * 
 * This extension function configures a license entry for the Mozilla Public License 2.0
 * in the licenses section of a Maven POM.
 */
fun MavenPomLicenseSpec.mplV2() {
    license {
        name.set("Mozilla Public License 2.0")
        url.set("https://www.mozilla.org/media/MPL/2.0/index.f75d2927d3c1.txt")
    }
}

/**
 * Adds the GNU General Public License 3.0 to a Maven POM license specification.
 * 
 * This extension function configures a license entry for the GNU General Public License 3.0
 * in the licenses section of a Maven POM.
 */
fun MavenPomLicenseSpec.gplV3() {
    license {
        name.set("GNU General Public License 3.0")
        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
    }
}

/**
 * Configures all Maven publications with the Apache License 2.0.
 * 
 * This extension function applies the Apache License 2.0 to all Maven publications
 * in the publishing extension.
 * 
 * Example usage:
 * ```kotlin
 * publishing {
 *     apache2License()
 * }
 * ```
 */
fun PublishingExtension.apache2License() {
    publications.withType<MavenPublication>().configureEach {
        pom {
            licenses {
                apache2()
            }
        }
    }
}

/**
 * Configures all Maven publications with the Mozilla Public License 2.0.
 * 
 * This extension function applies the Mozilla Public License 2.0 to all Maven publications
 * in the publishing extension.
 * 
 * Example usage:
 * ```kotlin
 * publishing {
 *     mplV2License()
 * }
 * ```
 */
fun PublishingExtension.mplV2License() {
    publications.withType<MavenPublication>().configureEach {
        pom {
            licenses {
                mplV2()
            }
        }
    }
}

/**
 * Configures all Maven publications with the GNU General Public License 3.0.
 * 
 * This extension function applies the GNU General Public License 3.0 to all Maven publications
 * in the publishing extension.
 * 
 * Example usage:
 * ```kotlin
 * publishing {
 *     gplV3License()
 * }
 * ```
 */
fun PublishingExtension.gplV3License() {
    publications.withType<MavenPublication>().configureEach {
        pom {
            licenses {
                gplV3()
            }
        }
    }
}

/**
 * Configures basic project information for all Maven publications.
 * 
 * This extension function sets the name, description, and URL for all Maven
 * publications in the publishing extension.
 * 
 * Example usage:
 * ```kotlin
 * publishing {
 *     setProjectInfo(
 *         name = "my-library",
 *         description = "A useful library for doing things"
 *     )
 * }
 * ```
 * 
 * @param name The name of the project
 * @param description A description of the project
 * @param url The URL of the project (defaults to a Karma Krafts GitLab URL based on the name)
 */
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
