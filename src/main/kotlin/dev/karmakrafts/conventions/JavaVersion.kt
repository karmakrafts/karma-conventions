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

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * Converts an integer Java version number to a JavaVersion enum value.
 *
 * This extension function takes an integer representing a Java major version
 * (e.g., 8, 11, 17) and returns the corresponding JavaVersion enum value.
 *
 * @return The JavaVersion enum value corresponding to this integer
 * @throws IllegalArgumentException if the integer does not correspond to a valid Java version
 */
fun Int.toJavaVersion(): JavaVersion {
    return requireNotNull(JavaVersion.entries.find {
        it.majorVersion.toIntOrNull() == this@toJavaVersion
    }) { "Invalid Java major version: $this" }
}

/**
 * Configures Java version settings for various plugins in a Gradle project.
 *
 * This function automatically detects and configures Java version settings for:
 * - Java plugin (toolchain, source and target compatibility)
 * - Kotlin JVM plugin (JVM toolchain)
 * - Kotlin Multiplatform plugin (JVM toolchain)
 * - Android Library plugin (source and target compatibility)
 * - Android Application plugin (source and target compatibility)
 *
 * @param compileVersion The Java version to compile against (JDK version).
 * @param targetVersion The Java version to produce bytecode for (classfile version).
 */
fun Project.configureJava(compileVersion: Int, targetVersion: Int = compileVersion) {
    project.pluginManager.apply {
        if (hasPlugin(PluginIds.JAVA)) withPlugin(PluginIds.JAVA) {
            extensions.getByType<JavaPluginExtension>().apply {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(compileVersion))
                }
            }
            tasks.withType<JavaCompile> {
                options.release.set(targetVersion)
            }
        }
        if (hasPlugin(PluginIds.KOTLIN_JVM)) withPlugin(PluginIds.KOTLIN_JVM) {
            logger.info("Found Kotlin JVM plugin, adjusting Java version")
            extensions.getByType(KotlinJvmExtension::class).apply {
                jvmToolchain(compileVersion)
            }
            tasks.withType<KotlinJvmCompile> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(targetVersion.toString()))
                }
            }
        }
        if (hasPlugin(PluginIds.KOTLIN_MP)) withPlugin(PluginIds.KOTLIN_MP) {
            logger.info("Found Kotlin Multiplatform plugin, adjusting Java version")
            extensions.getByType<KotlinMultiplatformExtension>().apply {
                jvmToolchain(compileVersion)
            }
            tasks.withType<KotlinJvmCompile> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(targetVersion.toString()))
                }
            }
        }
        if (hasPlugin(PluginIds.ANDROID_APP)) withPlugin(PluginIds.ANDROID_APP) {
            logger.info("Found Android Application plugin, adjusting Java version")
            extensions.getByType<ApplicationExtension>().compileOptions {
                val javaVersion = compileVersion.toJavaVersion()
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
        }
    }
}

/**
 * Configures Java version settings using a string provider.
 *
 * This is a convenience overload that accepts a Provider<String> containing
 * the Java version number as a string. The string is converted to an integer
 * and passed to the main configureJava function.
 *
 * @param compileVersion The Java version to compile against (JDK version).
 * @param targetVersion The Java version to produce bytecode for (classfile version).
 * @see configureJava
 */
fun Project.configureJava(compileVersion: Provider<String>, targetVersion: Provider<String> = compileVersion) {
    configureJava(compileVersion.get().toInt(), targetVersion.get().toInt())
}
