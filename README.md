[![Build](https://github.com/ldenisey/gradle-setversions-plugin/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/ldenisey/gradle-setversions-plugin/actions/workflows/build.yml)

# Gradle Set Versions Plugin

Simple plugin to modify gradle versions directly in build files or properties. No metadata file, no specific flow constraints.

## Usage

You can add this plugin to your top-level or modules build script.

In a groovy 'build.gradle' build file :

```groovy
plugins {
  id "com.github.ldenisey.setversions" version "$version"
}
```

In a kotlin 'build.gradle.kts' build file :

```kotlin
plugins {
  id("com.github.ldenisey.setversions") version "$version"
}
```

## Tasks

### `getVersions`

This task displays the current version of your project, optionally updated by suffix addition/removal and/or incrementation.

The new version is not applied, to do so use the task [setVersions](#setversions). As both tasks use the same logic
internally you can use getVersions task for dry run execution.

When modifying current version, it is expected to have :
- an optional prefix matching [A-Za-z0-9/_-]* regex
- a base version made of at least 2 numbers separated by points ((\d+\.)+\d+ regex)
- an optional suffix matching [A-Za-z0-9/_-]* regex

Quick examples :

| Project version          | Command                                                | Result                   |
|--------------------------|--------------------------------------------------------|--------------------------|
| 1.2.3-SNAPSHOT           | ./gradlew getVersions                                  | 1.2.3-SNAPSHOT           |
| 1.2.3-SNAPSHOT           | ./gradlew getVersions --suffix=false                   | 1.2.3                    |
| 1.2.3-SNAPSHOT           | ./gradlew getVersions --new-version=2.0.0              | 2.0.0                    |
| 1.2.3-SNAPSHOT           | ./gradlew getVersions --suffix=rc                      | 1.2.3-rc                 |
| 1.2.3-SNAPSHOT           | ./gradlew getVersions --increment=1 --suffix=false     | 2.0.0                    |
| 1.2.3-SNAPSHOT           | ./gradlew getVersions --increment=technical            | 1.2.4-SNAPSHOT           |
| 1.2.3-SNAPSHOT           | ./gradlew getVersions --increment=minor --suffix=false | 1.3.0                    |
| 1.2.3-SNAPSHOT           | ./gradlew getVersions --prefix=id/12                   | id/12-1.2.3-SNAPSHOT     |
| v1.2                     | ./gradlew getVersions --increment=major                | v2.0                     |
| v1.2                     | ./gradlew getVersions --increment=2 --suffix=dev       | v1.3-dev                 |
| feature/42-1.2.3.4-alpha | ./gradlew getVersions --increment=4                    | feature/42-1.2.3.5-alpha |
| feature/42-1.2.3.4-alpha | ./gradlew getVersions --increment=-1                   | feature/42-1.2.3.5-alpha |
| feature/42-1.2.3.4-alpha | ./gradlew getVersions --prefix=false                   | 42-1.2.3.4-alpha         |

#### Plugin configuration

##### defaultSuffix

Default suffix value used by `--suffix=true` option. 'SNAPSHOT' by default, you can set it in your 'build.gradle',
for example :

``` groovy
versions {
  defaultSuffix = 'dev'
}
```

##### skipSet

Boolean to disable version update on a specific module.

``` groovy
versions {
  skipSet = true
}
```

#### Task options

##### new-version

String option to define a new version from scratch, regardless of existing version value.

For example `./gradlew getVersions --new-version=1.2.3-SNAPSHOT` returns `1.2.3-SNAPSHOT`.

##### suffix

String option to add or replace a suffix. Can be set to any alphanumerical string, except `true` and `false`.
The suffix is separated from base version with `-`.

`--suffix=true` will append the project's [default suffix](#defaultSuffix). `--suffix=false` will trim it.

##### increment

Use this option to increment base version digits. Specify the position of the version digit to increment, all digits 
on the right of the incremented digit will be set to 0.

Position can be set with :
- a positive integer, 1 being first on the left 
- a negative integer, -1 being last digit on the right.
- an alias :
    - `major` for first digit
    - `minor` for second digit
    - `technical` for third digit

For examples, if current version is `1.2.3-SNAPSHOT`, executing `./gradlew getVersions --increment=1` will return
`2.0.0-SNAPSHOT`. It is equivalent to `./gradlew getVersions --increment=-3` and `./gradlew getVersions --increment=major`

### `setVersions`

This task updates the project/modules versions.

Its options to generate a new version are shared with [getVersions task](#getversions) task, refer to it for options.

There are several ways to define versions in gradle. As of now, this task can update versions defined in project/modules
build files, gradle.properties and in build scripts defined in buildSrc (see [gradle sample](https://docs.gradle.org/current/samples/sample_convention_plugins.html#organizing_build_logic)).
