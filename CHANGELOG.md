## [Unreleased]

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