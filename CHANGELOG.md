# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres poorly to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.5] - 2024-12-16
### Changed
- Bump org.quartz-scheduler:quartz from 2.3.2 to 2.5.0.
- Bump org.quartz-scheduler:quartz-jobs from 2.3.2 to 2.5.0.
- Bump fi.jubic:easyparent from 0.1.13 to 0.1.16.
- Bump fi.jubic:easyconfig-core from 0.10.3 to 0.10.6.

## [0.2.4] - 2024-08-31
### Changed
- Bump fi.jubic:easyparent from 0.1.12 to 0.1.13.
- Bump fi.jubic:easyconfig-core from 0.10.0 to 0.10.3.

## [0.2.3] - 2024-05-06
### Changed
- Bump fi.jubic:easyparent from 0.1.11 to 0.1.12.

## [0.2.2] - 2023-12-26
### Added
- Java 21 tests.
### Changed
- Bump fi.jubic:easyconfig-core from 0.9.2 to 0.10.0.

## [0.2.1] - 2021-12-10
### Added
- Full Java 17 support.

## [0.2.0] - 2021-04-13
### Removed
- Removed `easyconfig-liquibase` and `easyconfig-dbunit` modules. The functionality was moved to
`easyconfig` modules to avoid the extra dependency.
