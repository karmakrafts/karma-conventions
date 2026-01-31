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

package dev.karmakrafts.conventions

import org.gradle.api.Project
import java.net.URI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Attempts to ping the specified address and returns the round-trip time if successful.
 *
 * @param address The URL address to ping (e.g., "https://example.com")
 * @param connectTimeout The maximum time to wait for establishing a connection
 * @param readTimeout The maximum time to wait for reading data
 * @return The round-trip time as a [Duration] if successful, or null if the ping failed
 */
fun ping( // @formatter:off
    address: String, 
    connectTimeout: Duration = 3.seconds, 
    readTimeout: Duration = 3.seconds
): Duration? { // @formatter:on
    return try {
        val startTime = System.currentTimeMillis()
        val connection = URI.create(address).toURL().openConnection()
        connection.connectTimeout = connectTimeout.inWholeMilliseconds.toInt()
        connection.readTimeout = readTimeout.inWholeMilliseconds.toInt()
        connection.inputStream.close()
        (startTime - System.currentTimeMillis()).milliseconds
    } catch (error: Throwable) {
        null
    }
}

/**
 * Checks if the system has an active internet connection.
 *
 * This function attempts to ping Google's servers to determine if there is an active internet connection.
 *
 * @return true if an internet connection is available, false otherwise
 */
fun hasInternetConnection(): Boolean = ping("https://google.com") != null

/**
 * Indicates whether Gradle is explicitly running in offline mode.
 *
 * This property checks if the Gradle build was started with the `--offline` flag.
 *
 * @return true if Gradle is running in offline mode, false otherwise
 */
val Project.isForcedOffline: Boolean
    get() = gradle.startParameter.isOffline

/**
 * Determines if the project should operate in offline mode.
 *
 * This property returns true if either:
 * - Gradle was explicitly started in offline mode (via `--offline` flag)
 * - There is no active internet connection
 *
 * @return true if the project should operate in offline mode, false otherwise
 */
val Project.isOffline: Boolean
    get() = isForcedOffline || !hasInternetConnection()
