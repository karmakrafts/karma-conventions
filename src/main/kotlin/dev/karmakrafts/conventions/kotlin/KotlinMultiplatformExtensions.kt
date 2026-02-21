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

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import dev.karmakrafts.conventions.PluginIds
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode
import org.jetbrains.kotlin.gradle.dsl.KotlinGradlePluginDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsNodeDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsSubTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/**
 * Configures the default native target settings.
 */
fun KotlinNativeTarget.defaultNativeTarget() {
    compilerOptions {
        freeCompilerArgs.add("-Xklib-duplicated-unique-name-strategy=allow-first-with-warning")
    }
}

/**
 * Configures the default macOS target settings.
 */
fun KotlinNativeTarget.defaultMacosTarget() {
    defaultNativeTarget()
}

/**
 * Adds macOS targets to the multiplatform project.
 *
 * @param config The configuration to apply to each macOS target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withMacos(
    name: String = "macos", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
    macosArm64("${name}Arm64") {
        defaultMacosTarget()
        config()
    }
}

/**
 * Configures the default iOS target settings.
 */
fun KotlinNativeTarget.defaultIosTarget() {
    defaultNativeTarget()
}

/**
 * Adds iOS targets (Arm64, and Simulator Arm64) to the multiplatform project.
 *
 * @param config The configuration to apply to each iOS target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withIos(
    name: String = "ios", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
    iosArm64("${name}Arm64") {
        defaultIosTarget()
        config()
    }
    iosSimulatorArm64("${name}SimulatorArm64") {
        defaultIosTarget()
        config()
    }
}

/**
 * Adds tvOS targets (Arm64, and Simulator Arm64) to the multiplatform project.
 *
 * @param config The configuration to apply to each tvOS target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withTvos(
    name: String = "tvos", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
    tvosArm64("${name}Arm64") {
        defaultIosTarget()
        config()
    }
    tvosSimulatorArm64("${name}SimulatorArm64") {
        defaultIosTarget()
        config()
    }
}

/**
 * Adds watchOS targets (Arm32, Arm64, and Simulator Arm64) to the multiplatform project.
 *
 * @param config The configuration to apply to each watchOS target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withWatchos(
    name: String = "watchos", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
    watchosArm32("${name}Arm32") {
        defaultIosTarget()
        config()
    }
    watchosArm64("${name}Arm64") {
        defaultIosTarget()
        config()
    }
    watchosSimulatorArm64("${name}SimulatorArm64") {
        defaultIosTarget()
        config()
    }
}

/**
 * Configures the default Linux target settings.
 */
fun KotlinNativeTarget.defaultLinuxTarget() {
    defaultNativeTarget()
}

/**
 * Adds Linux targets (x64 and Arm64) to the multiplatform project.
 *
 * @param config The configuration to apply to each Linux target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withLinux(
    name: String = "linux", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
    linuxX64("${name}X64") {
        defaultLinuxTarget()
        config()
    }
    linuxArm64("${name}Arm64") {
        defaultLinuxTarget()
        config()
    }
}

/**
 * Configures the default Android Native target settings.
 */
fun KotlinNativeTarget.defaultAndroidNativeTarget() {
    defaultNativeTarget()
}

/**
 * Adds Android Native targets (Arm32, Arm64, x64, and x86) to the multiplatform project.
 *
 * @param config The configuration to apply to each Android Native target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withAndroidNative(
    name: String = "androidNative", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
    androidNativeArm32("${name}Arm32") {
        defaultAndroidNativeTarget()
        config()
    }
    androidNativeArm64("${name}Arm64") {
        defaultAndroidNativeTarget()
        config()
    }
    androidNativeX64("${name}X64") {
        defaultAndroidNativeTarget()
        config()
    }
    androidNativeX86("${name}X86") {
        defaultAndroidNativeTarget()
        config()
    }
}

/**
 * Configures the default MinGW (Windows) target settings.
 */
fun KotlinNativeTarget.defaultMingwTarget() {
    defaultNativeTarget()
}

/**
 * Adds the MinGW x64 (Windows) target to the multiplatform project.
 *
 * @param config The configuration to apply to the MinGW target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withMingw(
    name: String = "mingw", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
    mingwX64("${name}X64") {
        defaultMingwTarget()
        config()
    }
}

/**
 * Adds all common native targets (Android Native, Linux, macOS, iOS, tvOS, watchOS, and MinGW) to the multiplatform project.
 *
 * @param config The configuration to apply to each native target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withNative(crossinline config: KotlinNativeTarget.() -> Unit = {}) {
    withAndroidNative(config = config)
    withLinux(config = config)
    withMacos(config = config)
    withIos(config = config)
    withTvos(config = config)
    withWatchos(config = config)
    withMingw(config = config)
}

/**
 * Configures the default JavaScript target settings, including source maps.
 */
fun KotlinJsTargetDsl.defaultJsTarget() {
    compilerOptions {
        sourceMap.set(true)
        sourceMapEmbedSources.set(JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
    }
}

/**
 * Adds the JavaScript target to the multiplatform project.
 *
 * @param config The configuration to apply to the JavaScript target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withJs(
    name: String = "js", crossinline config: KotlinJsTargetDsl.() -> Unit = {}
) {
    js(name) {
        defaultJsTarget()
        config()
    }
}

/**
 * Adds the WebAssembly (Wasm) target to the multiplatform project.
 *
 * @param config The configuration to apply to the Wasm target.
 */
@KotlinGradlePluginDsl
@OptIn(ExperimentalWasmDsl::class)
inline fun KotlinMultiplatformExtension.withWasm(
    name: String = "wasmJs", crossinline config: KotlinJsTargetDsl.() -> Unit = {}
) {
    wasmJs(name) {
        config()
    }
}

/**
 * Adds both JavaScript and WebAssembly (Wasm) targets to the multiplatform project.
 *
 * @param config The configuration to apply to both targets.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withWeb(crossinline config: KotlinJsTargetDsl.() -> Unit = {}) {
    withJs(config = config)
    withWasm(config = config)
}

/**
 * Configures the default browser settings, specifically using Firefox Headless for Karma tests.
 */
fun KotlinJsSubTargetDsl.defaultBrowserConfig() {
    testTask {
        useKarma {
            useFirefoxHeadless()
        }
    }
}

/**
 * Adds browser support to a JavaScript-like target.
 *
 * @param config The configuration to apply to the browser sub-target.
 */
@KotlinGradlePluginDsl
inline fun KotlinJsTargetDsl.withBrowser(
    crossinline config: KotlinJsBrowserDsl.() -> Unit = {}
) {
    browser {
        defaultBrowserConfig()
        config()
    }
}

/**
 * Adds Node.js support to a JavaScript-like target.
 *
 * @param config The configuration to apply to the Node.js sub-target.
 */
@KotlinGradlePluginDsl
inline fun KotlinJsTargetDsl.withNodeJs(crossinline config: KotlinJsNodeDsl.() -> Unit = {}) {
    nodejs {
        config()
    }
}

/**
 * Adds the JVM target to the multiplatform project.
 *
 * @param config The configuration to apply to the JVM target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withJvm(
    name: String = "jvm", crossinline config: KotlinJvmTarget.() -> Unit = {}
) {
    jvm(name) {
        config()
    }
}

/**
 * Adds the Android target to the multiplatform project and applies default Android configuration.
 *
 * @param namespace The unique Android namespace associated with this target's module.
 * @param compileSdk A provider to provide the compile SDK version for this application.
 * @param minSdk A provider to provide the minimal SDK version for this application.
 * @param targetSdk A provider to provide the target SDK version the linter is configured at for this application.
 * @param config The configuration to apply to the Android target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withAndroid(
    name: String = "android",
    namespace: String = project.group.toString(),
    compileSdk: Provider<String> = project.provider { "36" },
    minSdk: Provider<String> = project.provider { "24" },
    targetSdk: Provider<String> = compileSdk,
    crossinline config: KotlinAndroidTarget.() -> Unit = {}
) {
    androidTarget(name) {
        config()
    }
    project.pluginManager.withPlugin(PluginIds.ANDROID_APP) {
        project.extensions.configure<ApplicationExtension> {
            this.namespace = namespace
            this.compileSdk = compileSdk.get().toInt()
            lint.targetSdk = targetSdk.get().toInt()
            defaultConfig.apply {
                this.minSdk = minSdk.get().toInt()
            }
        }
    }
}

/**
 * Adds the Android target to the multiplatform project, configures it for a library (publishing release variants),
 * and applies default Android configuration.
 *
 * @param namespace The unique Android namespace associated with this target's module.
 * @param compileSdk A provider to provide the compile SDK version for this library.
 * @param minSdk A provider to provide the minimal SDK version for this library.
 * @param targetSdk A provider to provide the target SDK version the linter is configured at for this library.
 * @param config The configuration to apply to the Android target.
 */
@KotlinGradlePluginDsl
inline fun KotlinMultiplatformExtension.withAndroidLibrary(
    namespace: String = project.group.toString(),
    compileSdk: Provider<String> = project.provider { "36" },
    minSdk: Provider<String> = project.provider { "24" },
    targetSdk: Provider<String> = compileSdk,
    crossinline config: KotlinMultiplatformAndroidLibraryTarget.() -> Unit = {}
) {
    extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("androidLibrary") {
        this.namespace = namespace
        this.compileSdk = compileSdk.get().toInt()
        this.minSdk = minSdk.get().toInt()
        lint.targetSdk = targetSdk.get().toInt()
        config()
    }
}

/**
 * Shorthand for `withCompilations { compilation -> compilation is KotlinMultiplatformAndroidCompilation }`,
 * since the new AGP library extension isn't fully supported by the hierarchy DSL yet.
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
@KotlinGradlePluginDsl
fun KotlinHierarchyBuilder.withAndroidLibrary() {
    withCompilations { compilation -> compilation is KotlinMultiplatformAndroidCompilation }
}

/**
 * Applies common compiler options to all available KMP targets.
 */
@KotlinGradlePluginDsl
fun KotlinMultiplatformExtension.defaultCompilerOptions() {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",  // Enable expect-actual classes
            "-Xcontext-parameters",     // Enable context parameters
            "-Xexplicit-backing-fields" // Enable explicit backing fields
        )
        optIn.addAll(
            "kotlin.ExperimentalStdlibApi",
            "kotlin.ExperimentalUnsignedTypes",
            "kotlin.uuid.ExperimentalUuidApi",
            "kotlin.time.ExperimentalTime",
            "kotlin.concurrent.atomics.ExperimentalAtomicApi",
            "kotlin.io.encoding.ExperimentalEncodingApi",
            "kotlin.contracts.ExperimentalContracts",
            "kotlin.contracts.ExperimentalExtendedContracts"
        )
    }
}