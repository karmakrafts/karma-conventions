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

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Applies common compiler options to all available KMP targets.
 */
fun KotlinJvmProjectExtension.defaultCompilerOptions() {
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

/**
 * Enables all experimental language features for the current Kotlin version.
 */
fun KotlinJvmProjectExtension.enableExperimentalFeatures() {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",  // Enable expect-actual classes
            "-Xcontext-parameters",     // Enable context parameters
            "-Xexplicit-backing-fields" // Enable explicit backing fields
        )
    }
}