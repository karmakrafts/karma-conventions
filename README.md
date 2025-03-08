# Karma Conventions

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