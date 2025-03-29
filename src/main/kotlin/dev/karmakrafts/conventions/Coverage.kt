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

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

fun Project.registerCoverageTask() {
    apply(plugin = "org.jetbrains.kotlinx.kover")
    tasks.register("testCoverage") {
        val reportTask = tasks.named("koverXmlReportJvm")
        dependsOn(reportTask)
        doLast {
            val regex = """<counter type="INSTRUCTION" missed="(\d+)" covered="(\d+)"/>""".toRegex()
            for (file in reportTask.get().outputs.files) {
                file.useLines { lines ->
                    val coverage = lines.last(regex::containsMatchIn)
                    regex.find(coverage)?.let { coverageData ->
                        val covered = coverageData.groupValues[2].toInt()
                        val missed = coverageData.groupValues[1].toInt()
                        println("Total test coverage: ${covered * 100 / (missed + covered)}%")
                    }
                }
            }
        }
    }
}