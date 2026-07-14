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

package dev.karmakrafts.conventions.dokka

import dev.karmakrafts.conventions.PluginIds
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

fun DocumentationConfig.withJava() {
    val pluginManager = project.pluginManager
    check(
        pluginManager.hasPlugin(PluginIds.JAVA) //
            || pluginManager.hasPlugin(PluginIds.KOTLIN_JVM) //
            || pluginManager.hasPlugin(PluginIds.KOTLIN_MP)
    ) {
        "withJava requires Java, Kotlin JVM or Kotlin Multiplatform plugin to be applied"
    }
    val version = project.extensions.getByType<JavaPluginExtension>().toolchain.languageVersion.get().asInt()
    dependsOn( // @formatter:off
        url = "https://docs.oracle.com/en/java/javase/$version/docs/api",
        packageListName = "element-list"
    ) // @formatter:on
}

fun DocumentationConfig.withKotlin() = dependsOn("https://kotlinlang.org/api/core")

fun DocumentationConfig.withKotlinxCoroutines() = dependsOn("https://kotlinlang.org/api/kotlinx.coroutines")

fun DocumentationConfig.withKotlinxIo() = dependsOn("https://kotlinlang.org/api/kotlinx-io")

fun DocumentationConfig.withKotlinxDateTime() = dependsOn("https://kotlinlang.org/api/kotlinx-datetime")

fun DocumentationConfig.withKotlinxSerialization() = dependsOn("https://kotlinlang.org/api/kotlinx.serialization")

fun DocumentationConfig.withKtor() = dependsOn("https://api.ktor.io")

fun DocumentationConfig.withGradle() = dependsOn( // @formatter:off
    url = "https://docs.gradle.org/${project.gradle.gradleVersion}/javadoc",
    packageListName = "element-list"
) // @formatter:on

fun DocumentationConfig.withAndroidGradle(version: Provider<String>) {
    dependsOn("https://developer.android.com/reference/tools/gradle-api/${version.get()}")
}

fun DocumentationConfig.withKotlinGradle() = dependsOn("https://kotlinlang.org/api/kotlin-gradle-plugin")