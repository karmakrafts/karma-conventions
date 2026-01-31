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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLConnection

/**
 * Characters that need to be percent-encoded in URLs.
 * This includes special characters defined in RFC 3986.
 */
private const val SPECIAL_CHARS: String = ":/?#[]@!$&'()*+,;="

/**
 * JSON serializer configuration used for deserializing HTTP responses.
 * Configured to ignore unknown keys for more flexible parsing.
 */
@PublishedApi
internal val json: Json = Json {
    ignoreUnknownKeys = true
}

/**
 * Percent-encodes special characters in a string according to URL encoding standards.
 *
 * This function converts characters defined in [SPECIAL_CHARS] to their percent-encoded
 * representation (e.g., ":" becomes "%3A"). Characters not in the special chars list
 * remain unchanged.
 *
 * @return A new string with special characters percent-encoded
 */
fun String.percentEncode(): String {
    var encoded = ""
    for (char in this) {
        if (char !in SPECIAL_CHARS) {
            encoded += char
            continue
        }
        encoded += "%${char.code.toUByte().toString(16).uppercase()}"
    }
    return encoded
}

/**
 * Sets a default user agent string for this URLConnection.
 *
 * This extension function configures the connection with a standard Firefox user agent
 * to help ensure requests are processed normally by servers that might reject requests
 * without a recognized user agent.
 */
fun URLConnection.setDefaultUserAgent() {
    setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:133.0) Gecko/20100101 Firefox/133.0")
}

/**
 * Fetches raw data from a URL as an input stream.
 *
 * This function makes an HTTP request to the specified address and returns the response
 * as an InputStream. It automatically sets a default user agent and handles the specified
 * HTTP method.
 *
 * @param address The URL to fetch data from
 * @param method The HTTP method to use (default is "GET")
 * @param request A lambda to configure additional request properties
 * @return An InputStream containing the response data, or null if the request failed
 */
inline fun fetchRaw(
    address: String, method: String = "GET", request: HttpURLConnection.() -> Unit = {}
): InputStream? {
    return try {
        val connection = URI.create(address).toURL().openConnection() as? HttpURLConnection ?: return null
        connection.setDefaultUserAgent()
        connection.requestMethod = method
        connection.request()
        connection.inputStream
    } catch (error: Throwable) {
        null
    }
}

/**
 * Fetches and deserializes JSON data from a URL into the specified type.
 *
 * This function makes an HTTP request to the specified address, reads the response as JSON,
 * and deserializes it into an object of type T using kotlinx.serialization.
 *
 * @param T The type to deserialize the JSON response into
 * @param address The URL to fetch data from
 * @param method The HTTP method to use (default is "GET")
 * @param request A lambda to configure additional request properties
 * @return An object of type T containing the deserialized data, or null if the request failed or deserialization failed
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> fetch(
    address: String, method: String = "GET", request: HttpURLConnection.() -> Unit = {}
): T? = try {
    fetchRaw(address, method, request)?.let { json.decodeFromStream<T>(it) }
} catch (error: Throwable) {
    null
}
