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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

/**
 * Discovers and runs the tests in an already compiled Kotlin/Native test executable.
 * Each test runs in a separate process, and failures are reported after all tests have finished.
 */
@DisableCachingByDefault(because = "Native tests depend on the host environment")
abstract class KotlinNativeStandaloneTest @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {
    @InputFile
    @PathSensitive(PathSensitivity.ABSOLUTE)
    val testExecutable: RegularFileProperty = objectFactory.fileProperty()

    @OutputFile
    val reportFile: RegularFileProperty =
        objectFactory.fileProperty().convention(project.layout.buildDirectory.file("test-results/$name/TEST-$name.xml"))

    @get:Inject
    protected abstract val execOperations: ExecOperations

    init {
        outputs.upToDateWhen { false }
        group = "verification"
        description = "Runs a pre-compiled Kotlin/Native test executable and generates a report for it"
    }

    @TaskAction
    fun run() {
        val executable = testExecutable.get().asFile
        if (!executable.isFile || !executable.canExecute()) {
            throw GradleException("Kotlin/Native test executable is not executable: $executable")
        }
        val discovery = execute("--ktest_list_tests")
        if (discovery.exitCode != 0 || discovery.error != null) {
            throw GradleException("Could not discover Kotlin/Native tests: ${discovery.diagnostics}")
        }
        val results = discoverTests(discovery.stdout).map { test ->
            logger.lifecycle("Running ${test.fullName}")
            // The native runner splits arguments on every '=', including those in filter values.
            val filter = Regex.escape(test.fullName).replace("=", "\\E\\u003D\\Q")
            TestResult(test, execute("--ktest_regex_filter=$filter", "--ktest_logger=GTEST"))
        }
        writeReport(results)
        val failed = results.count { it.failed || it.process.error != null }
        if (failed > 0) {
            throw GradleException("$failed Kotlin/Native test(s) failed. See ${reportFile.get().asFile}")
        }
    }

    private fun execute(vararg arguments: String): ProcessResult {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val start = System.nanoTime()
        var exitCode: Int? = null
        var error: String? = null
        try {
            exitCode = execOperations.exec {
                val executable = testExecutable.get().asFile
                executable(executable.absolutePath)
                workingDir(executable.parentFile)
                args(*arguments)
                standardOutput = stdout
                errorOutput = stderr
                isIgnoreExitValue = true
            }.exitValue
        } catch (exception: GradleException) {
            error = exception.stackTraceToString()
        }
        return ProcessResult(
            stdout.toString(Charsets.UTF_8),
            stderr.toString(Charsets.UTF_8),
            exitCode,
            (System.nanoTime() - start) / 1_000_000_000.0,
            error
        )
    }

    private fun discoverTests(output: String): List<TestCase> {
        val tests = mutableListOf<TestCase>()
        var suite: String? = null
        output.lineSequence().filter { it.isNotBlank() }.forEach { line ->
            if (line.first().isWhitespace()) {
                val currentSuite =
                    suite ?: throw GradleException("Test without a suite in Kotlin/Native test list: $line")
                tests += TestCase(currentSuite, line.trim())
            }
            else if (line.endsWith('.') && line.length > 1) {
                suite = line.dropLast(1)
            }
            else {
                throw GradleException("Invalid Kotlin/Native test list entry: $line")
            }
        }
        return tests
    }

    private fun writeReport(results: List<TestResult>) {
        val file = reportFile.get().asFile
        file.parentFile.mkdirs()
        file.outputStream().buffered().use { output ->
            val writer = XMLOutputFactory.newFactory().createXMLStreamWriter(output, "UTF-8")
            try {
                writer.writeStartDocument("UTF-8", "1.0")
                writer.writeStartElement("testsuites")
                writer.writeAttribute("name", xmlText(name))
                writer.writeStatistics(results)
                results.groupBy { it.test.suite }.forEach { (suite, suiteResults) ->
                    writer.writeStartElement("testsuite")
                    writer.writeAttribute("name", xmlText(suite))
                    writer.writeStatistics(suiteResults)
                    suiteResults.forEach { result ->
                        writer.writeStartElement("testcase")
                        writer.writeAttribute("classname", xmlText(result.test.suite))
                        writer.writeAttribute("name", xmlText(result.test.name))
                        writer.writeAttribute("time", result.process.seconds.toString())
                        when {
                            result.process.error != null -> {
                                writer.writeStartElement("error")
                                writer.writeAttribute("message", "Could not execute test")
                                writer.writeCharacters(xmlText(result.process.diagnostics))
                                writer.writeEndElement()
                            }

                            result.failed -> {
                                writer.writeStartElement("failure")
                                writer.writeAttribute("message", "Process exited with code ${result.process.exitCode}")
                                writer.writeCharacters(xmlText(result.process.diagnostics))
                                writer.writeEndElement()
                            }

                            result.skipped -> writer.writeEmptyElement("skipped")
                        }
                        writer.writeTextElement("system-out", result.process.stdout)
                        writer.writeTextElement("system-err", result.process.stderr)
                        writer.writeEndElement()
                    }
                    writer.writeEndElement()
                }
                writer.writeEndElement()
                writer.writeEndDocument()
            }
            finally {
                writer.close()
            }
        }
    }

    private fun XMLStreamWriter.writeStatistics(results: List<TestResult>) {
        writeAttribute("tests", results.size.toString())
        writeAttribute("failures", results.count { it.failed }.toString())
        writeAttribute("errors", results.count { it.process.error != null }.toString())
        writeAttribute("skipped", results.count { it.skipped }.toString())
        writeAttribute("time", results.sumOf { it.process.seconds }.toString())
    }

    private fun XMLStreamWriter.writeTextElement(name: String, text: String) {
        writeStartElement(name)
        writeCharacters(xmlText(text))
        writeEndElement()
    }

    private fun xmlText(text: String): String = buildString {
        text.codePoints().forEach { codePoint ->
            appendCodePoint(
                if (codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD || codePoint in 0x20..0xD7FF || codePoint in 0xE000..0xFFFD || codePoint in 0x10000..0x10FFFF) codePoint else 0xFFFD
            )
        }
    }

    private data class TestCase(val suite: String, val name: String) {
        val fullName: String = "$suite.$name"
    }

    private data class ProcessResult(
        val stdout: String, val stderr: String, val exitCode: Int?, val seconds: Double, val error: String?
    ) {
        val diagnostics: String
            get() = listOfNotNull("Exit code: $exitCode", error, stdout, stderr).joinToString("\n")
    }

    private data class TestResult(val test: TestCase, val process: ProcessResult) {
        val failed: Boolean = process.error == null && process.exitCode != 0
        val skipped: Boolean = process.error == null && process.exitCode == 0 && process.stdout.lineSequence()
            .any { it.trim() == "[  SKIPPED ] ${test.fullName}" }
    }
}