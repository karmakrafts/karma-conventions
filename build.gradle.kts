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

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.kotlinx.serialization)
}

group = "io.karma.conventions"

val baseVersion = libs.versions.karmaConventions
version = System.getenv("CI_COMMIT_TAG")?.let { baseVersion.get() }
    ?: "${baseVersion.get()}.${System.getenv("CI_PIPELINE_IID") ?: 0}-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
}

gradlePlugin {
    plugins {
        create("Karma Conventions") {
            id = "$group.${rootProject.name}" // io.karma.conventions.karma-conventions
            implementationClass = "$group.KarmaConventionsPlugin"
            displayName = name
        }
    }
}

System.getenv("CI_PROJECT_ID")?.let {
    dependencyLocking {
        lockAllConfigurations()
    }

    tasks {
        val dependenciesForAll by registering(DependencyReportTask::class)
    }
}