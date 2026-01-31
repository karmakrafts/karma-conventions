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

import dev.karmakrafts.conventions.GitLabPackageArtifact
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultCInteropSettings

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