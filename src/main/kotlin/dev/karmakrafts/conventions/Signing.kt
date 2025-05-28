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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Configures artifact signing using PGP keys from environment variables.
 *
 * This extension function sets up the signing extension to use PGP keys provided
 * through environment variables. It will automatically sign all publications
 * defined in the publishing extension.
 *
 * The following environment variables are used:
 * - SIGNING_KEY_ID: The ID of the PGP key
 * - SIGNING_PRIVATE_KEY: The Base64-encoded private key
 * - SIGNING_PASSWORD: The password for the private key
 *
 * If the SIGNING_KEY_ID environment variable is not set, signing will be skipped.
 *
 * Example usage:
 * ```kotlin
 * signing {
 *     signPublications()
 * }
 * ```
 */
@OptIn(ExperimentalEncodingApi::class)
fun SigningExtension.signPublications() {
    System.getenv("SIGNING_KEY_ID")?.let { keyId ->
        useInMemoryPgpKeys( // @formatter:off
            keyId,
            System.getenv("SIGNING_PRIVATE_KEY")?.let { encodedKey ->
                Base64.decode(encodedKey).decodeToString()
            },
            System.getenv("SIGNING_PASSWORD")
        ) // @formatter:on
    }
    project.afterEvaluate {
        sign(project.extensions.getByType<PublishingExtension>().publications)
    }
}
