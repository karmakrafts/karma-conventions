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
import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilationBuilder
import com.android.build.api.dsl.KotlinMultiplatformAndroidDeviceTest
import com.android.build.api.dsl.KotlinMultiplatformAndroidHostTest
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import dev.karmakrafts.conventions.EmbeddedDependencies
import dev.karmakrafts.conventions.PluginIds
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsNodeDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsSubTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmWasiTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.targets.wasm.dsl.KotlinWasmtimeDsl
import org.jetbrains.kotlin.konan.target.HostManager

/**
 * A workaround for running Kotlin/Native Linux arm64 unit tests via Gradle
 * even tho the compiler does not support Linux arm64 as a host.
 * This works in conjunction with a CI, copying the test binary from host to test target.
 *
 * This may be removed in the future when the compiler adds host support for Linux arm64,
 * but it is to be considered stable for the time being.
 *
 * **This function assumes a debug build of the tests to be present in the expected Gradle/KMP location,
 * release builds will be ignored.**
 */
fun KotlinMultiplatformExtension.withLinuxArm64StandaloneTests() {
    project.tasks.register("linuxArm64StandaloneTest", KotlinNativeStandaloneTest::class.java) {
        testExecutable.set(project.layout.buildDirectory.file("bin/linuxArm64/debugTest/test.kexe"))
    }
}

/**
 * Applies the common Kotlin/Native compiler configuration to this target.
 *
 * Allows the first Kotlin/Native library with a duplicated unique name to be used and emits a warning.
 */
fun KotlinNativeTarget.defaultNativeTarget() {
    compilerOptions {
        freeCompilerArgs.add("-Xklib-duplicated-unique-name-strategy=allow-first-with-warning")
    }
}

/**
 * Applies the default Kotlin/Native configuration for a macOS target.
 */
fun KotlinNativeTarget.defaultMacosTarget() {
    defaultNativeTarget()
}

/**
 * Adds a macOS Arm64 target to the multiplatform project.
 *
 * The target is named by appending `Arm64` to [name] and receives the default macOS configuration
 * before [config] is applied.
 *
 * @param name The prefix for the generated target name. Defaults to `macos`.
 * @param config The configuration to apply to each macOS target.
 */
inline fun KotlinMultiplatformExtension.withMacos(
    name: String = "macos", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
    macosArm64("${name}Arm64") {
        defaultMacosTarget()
        config()
    }
}

/**
 * Applies the default Kotlin/Native configuration for an iOS target.
 */
fun KotlinNativeTarget.defaultIosTarget() {
    defaultNativeTarget()
}

/**
 * Adds iOS targets (Arm64, and Simulator Arm64) to the multiplatform project.
 *
 * Generated target names append `Arm64` and `SimulatorArm64` to [name]. Both targets receive the
 * default iOS configuration before [config] is applied.
 *
 * @param name The prefix for the generated target names. Defaults to `ios`.
 * @param config The configuration to apply to each iOS target.
 */
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
 * Generated target names append `Arm64` and `SimulatorArm64` to [name]. Both targets receive the
 * default iOS configuration before [config] is applied.
 *
 * @param name The prefix for the generated target names. Defaults to `tvos`.
 * @param config The configuration to apply to each tvOS target.
 */
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
 * Generated target names append `Arm32`, `Arm64`, and `SimulatorArm64` to [name]. Each target
 * receives the default iOS configuration before [config] is applied.
 *
 * @param name The prefix for the generated target names. Defaults to `watchos`.
 * @param config The configuration to apply to each watchOS target.
 */
inline fun KotlinMultiplatformExtension.withWatchos(
    name: String = "watchos", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
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
 * Applies the default Kotlin/Native configuration for a Linux target.
 */
fun KotlinNativeTarget.defaultLinuxTarget() {
    defaultNativeTarget()
}

/**
 * Adds Linux targets (x64 and Arm64) to the multiplatform project.
 *
 * Generated target names append `X64` and `Arm64` to [name]. Both targets receive the default
 * Linux configuration before [config] is applied.
 *
 * @param name The prefix for the generated target names. Defaults to `linux`.
 * @param config The configuration to apply to each Linux target.
 */
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
 * Applies the default Kotlin/Native configuration for an Android Native target.
 */
fun KotlinNativeTarget.defaultAndroidNativeTarget() {
    defaultNativeTarget()
}

/**
 * Adds Android Native targets (Arm32, Arm64, x64, and x86) to the multiplatform project.
 *
 * Generated target names append `Arm32`, `Arm64`, `X64`, and `X86` to [name]. Each target
 * receives the default Android Native configuration before [config] is applied.
 *
 * @param name The prefix for the generated target names. Defaults to `androidNative`.
 * @param config The configuration to apply to each Android Native target.
 */
@Suppress("DEPRECATION") // TODO: Substitute with KCML custom targets if these get removed eventually
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
 * Applies the default Kotlin/Native configuration for a MinGW (Windows) target.
 */
fun KotlinNativeTarget.defaultMingwTarget() {
    defaultNativeTarget()
}

/**
 * Adds the MinGW x64 (Windows) target to the multiplatform project.
 *
 * The target is named by appending `X64` to [name] and receives the default MinGW configuration
 * before [config] is applied.
 *
 * @param name The prefix for the generated target name. Defaults to `mingw`.
 * @param config The configuration to apply to the MinGW target.
 */
inline fun KotlinMultiplatformExtension.withMingw(
    name: String = "mingw", crossinline config: KotlinNativeTarget.() -> Unit = {}
) {
    mingwX64("${name}X64") {
        defaultMingwTarget()
        config()
    }
}

/**
 * Adds all supported native targets to the multiplatform project.
 *
 * This adds Android Native, Linux, macOS, iOS, tvOS, watchOS, and MinGW targets using their
 * respective default configuration, then applies [config] to every generated target.
 *
 * @param config The configuration to apply to each native target.
 */
inline fun KotlinMultiplatformExtension.withNative(crossinline config: KotlinNativeTarget.() -> Unit = {}) {
    // Native target may only be enabled when the host supports it, otherwise Gradle will always fail/crash
    if (HostManager.hostIsSupported) {
        withAndroidNative(config = config)
        withLinux(config = config)
        withMacos(config = config)
        withIos(config = config)
        withTvos(config = config)
        withWatchos(config = config)
        withMingw(config = config)
    }
}

/**
 * Applies the default JavaScript compiler configuration.
 *
 * Source maps are enabled and always embed source content in the generated source maps.
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
 * The target is named [name], receives the default JavaScript configuration, and then applies
 * [config].
 *
 * @param name The JavaScript target name. Defaults to `js`.
 * @param config The configuration to apply to the JavaScript target.
 */
inline fun KotlinMultiplatformExtension.withJs(
    name: String = "js", crossinline config: KotlinJsTargetDsl.() -> Unit = {}
) {
    js(name) {
        defaultJsTarget()
        config()
    }
}

/**
 * Adds the WebAssembly (Wasm) browser target to the multiplatform project.
 *
 * @param name The Wasm JavaScript target name. Defaults to `wasmJs`.
 * @param config The configuration to apply to the Wasm target.
 */
inline fun KotlinMultiplatformExtension.withWasmJs(
    name: String = "wasmJs", crossinline config: KotlinJsTargetDsl.() -> Unit = {}
) {
    wasmJs(name) {
        config()
    }
}

/**
 * Adds Node.js support to a WASI target.
 *
 * @param config The configuration to apply to the Node.js sub-target.
 */
@Deprecated(message = "NodeJS WASI target is deprecated in Kotlin", replaceWith = ReplaceWith("withWasmtime"))
inline fun KotlinWasmWasiTargetDsl.withNodeJs(crossinline config: KotlinJsNodeDsl.() -> Unit = {}) {
    this.wasmtime { config() }
}

/**
 * Adds Wasmtime support to a WASI target.
 *
 * @param config The configuration to apply to the Wasmtime sub-target.
 */
@OptIn(ExperimentalWasmDsl::class)
inline fun KotlinWasmWasiTargetDsl.withWasmtime(crossinline config: KotlinWasmtimeDsl.() -> Unit = {}) {
    wasmtime {
        config()
    }
}

/**
 * Adds the WebAssembly (Wasm) WASI target to the multiplatform project.
 *
 * @param name The Wasm WASI target name. Defaults to `wasmWasi`.
 * @param config The configuration to apply to the Wasm target.
 */
@OptIn(ExperimentalWasmDsl::class)
inline fun KotlinMultiplatformExtension.withWasmWasi(
    name: String = "wasmWasi", crossinline config: KotlinWasmWasiTargetDsl.() -> Unit = {}
) {
    wasmWasi(name) {
        config()
    }
}

/**
 * Adds both JavaScript and WebAssembly (Wasm) targets to the multiplatform project.
 *
 * This creates the default `js` and `wasmJs` targets and applies [config] to both. The JavaScript
 * target also receives the default JavaScript compiler configuration.
 *
 * @param config The configuration to apply to both targets.
 */
inline fun KotlinMultiplatformExtension.withWeb(crossinline config: KotlinJsTargetDsl.() -> Unit = {}) {
    withJs(config = config)
    withWasmJs(config = config)
}

/**
 * Applies the default browser test configuration.
 *
 * Configures the test task to use Karma with Chrome Headless.
 */
fun KotlinJsSubTargetDsl.defaultBrowserConfig() {
    testTask {
        useKarma {
            useChromeHeadless()
        }
    }
}

/**
 * Adds browser support to a JavaScript-like target.
 *
 * Applies the default browser test configuration before [config].
 *
 * @param config The configuration to apply to the browser sub-target.
 */
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
inline fun KotlinJsTargetDsl.withNodeJs(crossinline config: KotlinJsNodeDsl.() -> Unit = {}) {
    nodejs {
        config()
    }
}

/**
 * Adds the JVM target to the multiplatform project.
 *
 * @param name The JVM target name. Defaults to `jvm`.
 * @param config The configuration to apply to the JVM target.
 */
inline fun KotlinMultiplatformExtension.withJvm(
    name: String = "jvm", crossinline config: KotlinJvmTarget.() -> Unit = {}
) {
    jvm(name) {
        config()
    }
}

/**
 * Adds an Android target to the multiplatform project and configures the Android application extension.
 *
 * When the Android application plugin is applied, this configures its namespace, compile SDK, lint
 * target SDK, and minimum SDK. SDK providers must resolve to integer version numbers.
 *
 * @param name The Android target name. Defaults to `android`.
 * @param namespace The unique Android namespace associated with this target's module.
 * @param compileSdk A provider to provide the compile SDK version for this application.
 * @param minSdk A provider to provide the minimal SDK version for this application.
 * @param targetSdk A provider to provide the target SDK version the linter is configured at for this application.
 * @param config The configuration to apply to the Android target.
 */
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
 * Registers default Android host tests for this target.
 *
 * Host tests run on the local machine. [compilationConfig] configures the test compilation builder
 * and [testConfig] configures the resulting host test.
 *
 * @param compilationConfig The configuration to apply while creating the host test compilation.
 * @param testConfig The configuration to apply to the registered host test.
 */
inline fun KotlinMultiplatformAndroidLibraryTarget.withDefaultHostTests(
    crossinline compilationConfig: KotlinMultiplatformAndroidCompilationBuilder.() -> Unit = {},
    crossinline testConfig: KotlinMultiplatformAndroidHostTest.() -> Unit = {}
) {
    withHostTestBuilder {
        compilationConfig()
    }.configure {
        testConfig()
    }
    // Automatically inject the required runner dependencies to make our lives easier
    project.extensions.getByType<KotlinMultiplatformExtension>().sourceSets.apply {
        named("androidHostTest") {
            dependencies {
                implementation(EmbeddedDependencies.androidxTestJUnit)
            }
        }
    }
    project.afterEvaluate {
        project.tasks.apply {
            // Ensure any JVM desktop natives are available on the classpath to the Android host tests
            named { it == "testAndroidHostTest" }.withType(Test::class.java).configureEach {
                dependsOn("jvmProcessResources")
                val jvmMainResources = project.provider {
                    project.extensions.getByType<KotlinMultiplatformExtension>().sourceSets.getByName("jvmMain").resources.srcDirs
                }
                classpath = project.files(named { it == "jvmProcessResources" }, jvmMainResources) + classpath
            }
        }
    }
}

/**
 * Registers default Android device tests for this target,
 * including a default Pixel 6/API 36 emulator device.
 *
 * Device tests run on an emulator or physical device. The registered managed device is always
 * named according to the given device type and API level; its device model and API level are configured by [device] and [apiLevel].
 * [compilationConfig] configures the test compilation builder and [testConfig] configures the
 * resulting device test.
 *
 * @param sourceSetTreeName The source set tree used by the device test. Defaults to `test`.
 * @param device The managed device hardware profile. Defaults to `Pixel 6`.
 * @param apiLevel A provider for the managed device API level. Defaults to API 36.
 * @param compilationConfig The configuration to apply while creating the device test compilation.
 * @param testConfig The configuration to apply to the registered device test.
 */
inline fun KotlinMultiplatformAndroidLibraryTarget.withDefaultDeviceTests(
    sourceSetTreeName: String = "test",
    device: String = "Pixel 6",
    apiLevel: Provider<String> = project.provider { "36" },
    crossinline compilationConfig: KotlinMultiplatformAndroidCompilationBuilder.() -> Unit = {},
    crossinline testConfig: KotlinMultiplatformAndroidDeviceTest.() -> Unit = {}
) {
    withDeviceTestBuilder {
        this.sourceSetTreeName = sourceSetTreeName
        compilationConfig()
    }.configure {
        managedDevices {
            val cleanDeviceType = device.replace(" ", "").lowercase()
            val apiLevelValue = apiLevel.get()
            @Suppress("UnstableApiUsage") //
            localDevices.register("${cleanDeviceType}Api$apiLevelValue") {
                this.device = device
                this.apiLevel = apiLevelValue.toInt()
            }
        }
        // Use androidx.test.ext.junit & androidx.test.runner
        instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testConfig()
    }
    // Automatically inject the required runner dependencies to make our lives easier
    project.extensions.getByType<KotlinMultiplatformExtension>().sourceSets.apply {
        named("androidDeviceTest") {
            dependencies {
                implementation(EmbeddedDependencies.androidxTestJUnit)
                implementation(EmbeddedDependencies.androidxTestRunner)
            }
        }
    }
}

/**
 * Configures the existing Android Kotlin Multiplatform library target.
 *
 * The target's namespace, compile SDK, minimum SDK, and lint target SDK are configured before
 * [config] is applied. SDK providers must resolve to integer version numbers.
 *
 * @param namespace The unique Android namespace associated with this target's module.
 * @param compileSdk A provider to provide the compile SDK version for this library.
 * @param minSdk A provider to provide the minimal SDK version for this library.
 * @param targetSdk A provider to provide the target SDK version the linter is configured at for this library.
 * @param config The configuration to apply to the Android target.
 */
inline fun KotlinMultiplatformExtension.withAndroidLibrary(
    namespace: String = project.group.toString(),
    compileSdk: Provider<String> = project.provider { "36" },
    minSdk: Provider<String> = project.provider { "24" },
    targetSdk: Provider<String> = compileSdk,
    crossinline config: KotlinMultiplatformAndroidLibraryTarget.() -> Unit = {}
) {
    extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
        this.namespace = namespace
        this.compileSdk = compileSdk.get().toInt()
        this.minSdk = minSdk.get().toInt()
        lint.targetSdk = targetSdk.get().toInt()
        config()
    }
}

/**
 * Restricts hierarchy configuration to Android Kotlin Multiplatform library compilations.
 *
 * This is a shorthand for filtering compilations to [KotlinMultiplatformAndroidCompilation], which
 * is necessary because the Android Gradle Plugin library extension is not fully supported by the
 * hierarchy DSL.
 */
@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinHierarchyBuilder.withAndroidLibrary() {
    withCompilations { compilation -> compilation is KotlinMultiplatformAndroidCompilation }
}

/**
 * Applies common experimental API opt-ins to the multiplatform compiler options.
 *
 * Opts in to experimental standard library, unsigned types, UUID, time, atomic, encoding,
 * contracts, and extended contracts APIs.
 */
fun KotlinMultiplatformExtension.defaultCompilerOptions() {
    compilerOptions {
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