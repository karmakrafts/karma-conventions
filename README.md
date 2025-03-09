# Karma Conventions

[![](https://git.karmakrafts.dev/kk/karma-conventions/badges/master/pipeline.svg)](https://git.karmakrafts.dev/kk/karma-conventions/-/pipelines)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Ffiles.karmakrafts.dev%2Fmaven%2Fio%2Fkarma%2Fconventions%2Fkarma-conventions%2Fmaven-metadata.xml)](https://git.karmakrafts.dev/kk/karma-conventions/-/packages)

Gradle conventions plugin for Karma Krafts projects.  
This plugin provides many utilities, including but no limited to:

* Git integration
* GitLab Package Registry integration
* GitLab CI integration
* URL encoding
* Configuring the Java version
* `java.nio.file.Path` extensions

### How to use it

First, add the official Karma Krafts maven repository to your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        maven("https://files.karmakrafts.dev/maven")
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://files.karmakrafts.dev/maven")
    }
}
```

Then add a dependency on the plugin in your root buildscript:

```kotlin
plugins {
    id("io.karma.conventions") version "<version>"
}
```