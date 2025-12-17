# Karma Conventions

[![](https://git.karmakrafts.dev/kk/karma-conventions/badges/master/pipeline.svg)](https://git.karmakrafts.dev/kk/karma-conventions/-/pipelines)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.maven.apache.org%2Fmaven2%2Fdev%2Fkarmakrafts%2Fconventions%2Fkarma-conventions%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/karma-conventions/-/packages)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdev%2Fkarmakrafts%2Fconventions%2Fkarma-conventions%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/karma-conventions/-/packages)
[![](https://img.shields.io/badge/2.3.0-blue?logo=kotlin&label=kotlin)](https://kotlinlang.org/)
[![](https://img.shields.io/badge/documentation-black?logo=kotlin)](https://docs.karmakrafts.dev/karma-conventions)

Gradle conventions plugin for Karma Krafts projects.  
This plugin provides many utilities, including but no limited to:

* Git integration
* GitLab Package Registry integration
* GitLab CI integration
* URL encoding
* Configuring the Java version
* `java.nio.file.Path` extensions
* Coverage
* Publishing defaults & extensions
* Signing
* Network connectivity
* Kotlin target extensions
* Kotlin CInterop extensions
* Documentation generation using [Dokka](https://github.com/Kotlin/dokka)

### How to use it

First, add the official Maven Central repository to your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
    }
}
```

Then add a dependency on the plugin in your root buildscript:

```kotlin
plugins {
    id("dev.karmakrafts.conventions.karma-conventions") version "<version>"
}
```