# Karma Conventions

[![](https://git.karmakrafts.dev/kk/karma-conventions/badges/master/pipeline.svg)](https://git.karmakrafts.dev/kk/karma-conventions/-/pipelines)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.maven.apache.org%2Fmaven2%2Fdev%2Fkarmakrafts%2Fconventions%2Fkarma-conventions%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/karma-conventions/-/packages)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdev%2Fkarmakrafts%2Fconventions%2Fkarma-conventions%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/karma-conventions/-/packages)

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

### How to use it

First, add the official Maven Central repository to your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots")
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots")
        mavenCentral()
    }
}
```

Then add a dependency on the plugin in your root buildscript:

```kotlin
plugins {
    id("dev.karmakrafts.conventions") version "<version>"
}
```