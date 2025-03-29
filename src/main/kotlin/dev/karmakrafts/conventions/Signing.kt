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
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.signing.SigningExtension

/**
 * Configures the PGP keys from given environment variables
 * SIGNING_KEY_ID, SIGNING_PRIVATE_KEY and SIGNING_PASSWORD if present.
 */
fun SigningExtension.signPublications() {
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