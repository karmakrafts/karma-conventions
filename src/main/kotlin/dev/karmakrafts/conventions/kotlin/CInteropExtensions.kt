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

package dev.karmakrafts.conventions.kotlin

import dev.karmakrafts.conventions.GitLabPackage
import dev.karmakrafts.conventions.GitLabPackageArtifact
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultCInteropSettings
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/**
 * Makes a C interop processing task depend on the extraction of a GitLab package artifact.
 *
 * This extension function configures the C interop processing task to depend on the
 * extraction of the specified GitLab package artifact. This ensures that the C interop
 * processing will only start after the required artifact has been downloaded and extracted.
 *
 * @param artifact The GitLab package artifact that the C interop processing should depend on
 */
fun DefaultCInteropSettings.dependsOn(artifact: GitLabPackageArtifact) {
    artifact.project.tasks.named(interopProcessingTaskName) {
        dependsOn(artifact.extractTask)
    }
}

/**
 * Configures a C interop for the [KotlinNativeTarget] using a [GitLabPackage].
 *
 * This extension function automatically resolves the correct artifact from the given
 * [GitLabPackage] based on the target's architecture and family name, and registers
 * it as a C interop.
 *
 * @param name The name of the C interop to register
 * @param pkg The GitLab package to retrieve the interop artifact from
 * @param fileSuffix The suffix of the artifact file name. Defaults to "-release"
 * @param fileExtension The file extension of the package artifact to download. Defaults to "zip"
 */
fun KotlinNativeTarget.withCInterop( // @formatter:off
    name: String,
    pkg: GitLabPackage,
    fileSuffix: String = "-release",
    fileExtension: String = "zip"
) { // @formatter:on
    compilations.named("main") {
        val architecture = konanTarget.accurateArchitectureName
        val fileName = "build-${konanTarget.familyName}-$architecture$fileSuffix.$fileExtension"
        val suffix = "${konanTarget.familyName}${konanTarget.accurateArchitectureName.capitalized()}"
        val artifact = pkg[fileName, suffix, name]
        cinterops {
            register(name) {
                dependsOn(artifact)
            }
        }
    }
}