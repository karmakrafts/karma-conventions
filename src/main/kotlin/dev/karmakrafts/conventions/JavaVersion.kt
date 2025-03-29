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

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Int.toJavaVersion(): JavaVersion {
    return requireNotNull(JavaVersion.values().find {
        it.majorVersion.toIntOrNull() == this@toJavaVersion
    }) { "Invalid Java major version: $this" }
}

fun Project.configureJava(version: Int) {
    project.pluginManager.apply {
        withPlugin("java") {
            extensions.getByType<JavaPluginExtension>().apply {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(version))
                }
                val javaVersion = version.toJavaVersion()
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
        }
        withPlugin("org.jetbrains.kotlin.jvm") {
            logger.info("Found Kotlin JVM plugin, adjusting Java version")
            extensions.getByType(KotlinJvmExtension::class).apply {
                jvmToolchain(version)
            }
        }
        withPlugin("org.jetbrains.kotlin.multiplatform") {
            logger.info("Found Kotlin Multiplatform plugin, adjusting Java version")
            extensions.getByType<KotlinMultiplatformExtension>().apply {
                jvmToolchain(version)
            }
        }
        withPlugin("com.android.library") {
            logger.info("Found Android Library plugin, adjusting Java version")
            extensions.getByType<LibraryExtension>().compileOptions {
                val javaVersion = version.toJavaVersion()
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
        }
        withPlugin("com.android.application") {
            logger.info("Found Android Application plugin, adjusting Java version")
            extensions.getByType<ApplicationExtension>().compileOptions {
                val javaVersion = version.toJavaVersion()
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
        }
    }

}

fun Project.configureJava(provider: Provider<String>) {
    configureJava(provider.get().toInt())
}