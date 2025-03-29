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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLConnection

private const val SPECIAL_CHARS: String = ":/?#[]@!$&'()*+,;="

@PublishedApi
internal val json: Json = Json {
    ignoreUnknownKeys = true
}

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

fun URLConnection.setDefaultUserAgent() {
    setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:133.0) Gecko/20100101 Firefox/133.0")
}

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

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> fetch(
    address: String, method: String = "GET", request: HttpURLConnection.() -> Unit = {}
): T? = try {
    fetchRaw(address, method, request)?.let { json.decodeFromStream<T>(it) }
} catch (error: Throwable) {
    null
}