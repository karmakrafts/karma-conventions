## [Unreleased]

### Changed

- Updated to Kotlin 2.4.10
- Updated to AGP 9.3.0
- Added groups to all Gradle tasks in build & APIs
- Added descriptions to all Gradle tasks in build & APIs
- Migrate from delegate properties to explicit calls for task container access

## [1.18.3]

### Changed

- Updated CI configuration to use new bot identities

## [1.18.2]

### Changed

- Updated to Gradle 9.6.1
- Updated to NMCP 1.6.1

## [1.18.1]

### Changed

- Updated to Gradle 9.6.0

## [1.18.0]

### Added

- `withWasmWasi` KMP target extension
- `withNodeJs` WASM WASI runtime extension

### Changed

- `withWasm` KMP target extension renamed to `withWasmJs`
- Enable unused return value checker in `defaultCompilerOptions` extensions

## [1.17.1]

### Added

- Separate `compileVersion` and `targetVersion` parameters for `configureJava` function

## [1.17.0]

### Changed

- Updated to Kotlin 2.4.0

## [1.16.4]

### Changed

- Updated to Gradle 9.5.1

## [1.16.3]

### Changed

- Replace `androidLibrary` KMP target extension with `android` for `withAndroidLibrary`

## [1.16.2]

### Changed

- Updated to Android Gradle 9.2.1

## [1.16.1]

### Changed

- `defaultCompilerOptions` does not enable experimental features by default

### Added

- `enableExperimentalFeatures` extension for Kotlin JVM and Kotlin MP project scopes

## [1.16.0]

### Changed

- Updated to Gradle 9.5.0
- Updated to Kover 0.9.8
- Updated to Kotlin 2.3.21
- Updated to Dokka 2.2.0
- Updated to Android Gradle 9.2.0
- Updated to kotlinx.serialization 1.11.0

## [1.15.1]

### Changed

- Updated to Gradle 9.4.1

## [1.15.0]

### Changed

- Updated to Kotlin 2.3.20

## [1.14.0]

### Changed

- Updated to Kotlin 2.3.20-RC3

## [1.13.0]

### Changed

- Updated to Gradle 9.4.0
- Updated to Kotlin 2.3.20-RC2

## [1.12.5]

### Fixed

- Default artifact file name for `withCInterop` not being computed correctly

## [1.12.4]

### Added

- `withCInterop` extension for `KotlinNativeTarget` to allow directly  
  consuming `GitLabPackage` artifacts based on the Konan target.

## [1.12.3]

### Added

- `defaultCompilerOptions` extension for `KotlinJvmProjectExtension`

## [1.12.2]

### Fixed

- GitLab and Git APIs are not compatible with Gradle configuration cache

## [1.12.1]

### Fixed

- Fixed documentation dependencies not wiring task graph correctly

## [1.12.0]

### Added

- Added new Dokka configuration DSL for easily linking dependency documentation

### Changed

- Updated Kotlin to 2.3.20-RC

## [1.11.4]

### Fixed

- Patch package-list for external KGP documentation to workaround broken module entries

## [1.11.3]

### Add

- Add external documentation for KGP

## [1.11.2]

### Changed

- Fix links for external Oracle and Android docs

## [1.11.1]

### Changed

- Properly link external documentation for generated docs

## [1.11.0]

### Changed

- Wire through name parameter for KMP target extensions
- Update AGP to 9.0.1

## [1.10.17]

### Fixed

- Fix Trivy CI configuration being broken due to faulty runner drive

### Changed

- Combine publishing CI task for GitLab & Maven Central

## [1.10.16]

### Changed

- defaultCompilerOptions KMP extension now looks like a DSL function
- withAndroidLibrary KMP hierarchy extension now looks like a DSL function

## [1.10.15]

### Fixed

- Fixed positioning of headers in GitHub releases

## [1.10.14]

### Added

- Add links to GitHub releases

## [1.10.13]

## [1.10.12]

## [1.10.11]

## [1.10.10]

## [1.10.9]

### Fixed

- Fix GitHub release infos not being expanded

## [1.10.9]

### Fixed

- Fixed GitHub release job not escaping body JSON correctly

## [1.10.8]

### Fixed

- Fixed GitHub release job not escaping changelog for JSON

## [1.10.7]

### Added

- Mirror releases to GitHub

## [1.10.6]

### Added

- Added Documentation and Changelog links to releases

## [1.10.5]

### Fixed

- Fixed update changelog job Git authentication in CI config

## [1.10.4]

### Fixed

- Fixed default branch name in CI changelog job configuration

## [1.10.3]

### Added

- KMP target extensions for recommended watchOS and tvOS targets

### Changed

- Split publishing tasks in CI config for snapshots and releases

## [1.10.2]

### Removed

- Automatic changelog update via CI

## [1.10.1]

### Fixed

- Fixed automatic changelog update in CI configuration

## [1.10.0]

### Added

- New changelog system

### Changed

- Updated Kotlin to 2.3.20-Beta2
- Updated kotlinx.serialization to 1.10.0
