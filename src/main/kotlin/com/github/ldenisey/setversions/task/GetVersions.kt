package com.github.ldenisey.setversions.task

import com.github.ldenisey.setversions.SetVersionsPluginExtension
import com.github.ldenisey.setversions.version.FullVersion
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class GetVersions : DefaultTask() {

    companion object {
        const val OPT_NEW_VERSION = "new-version"
        const val OPT_PREFIX = "prefix"
        const val OPT_SUFFIX = "suffix"
        const val OPT_INCREMENT = "increment"
        const val MSG_NEW_VERSION_AND_SUFFIX_OR_PREFIX =
            "Options '$OPT_NEW_VERSION' can not be used with '$OPT_PREFIX' and/or '$OPT_SUFFIX'."
        const val MSG_UNSPECIFIED_VERSION = "Version is undefined, can not apply any change onto it."
    }

    @get:Input
    @get:Optional
    @set:Option(option = OPT_NEW_VERSION, description = "Specify the new version for scratch")
    var newVersion: String? = System.getProperty(OPT_NEW_VERSION)

    @get:Input
    @get:Optional
    @set:Option(
        option = OPT_SUFFIX,
        description = "Append or replace a suffix to the version. Can be a string, 'true' to append the default suffix or 'false' to trim any existing suffix."
    )
    var suffix: String? = System.getProperty(OPT_SUFFIX)

    @get:Input
    @get:Optional
    @set:Option(
        option = OPT_PREFIX,
        description = "Prepend or replace a prefix to the version. Can be any string or 'false' to trim any existing suffix."
    )
    var prefix: String? = System.getProperty(OPT_PREFIX)

    @get:Input
    @get:Optional
    @set:Option(
        option = OPT_INCREMENT,
        description = "Position of a version digit to increment, starting at 1, from left to right. Following digits will be set to 0. Aliases can be used : 'major' for the first digit, 'minor' for the second, 'technical' for the third."
    )
    var increment: String? = System.getProperty(OPT_INCREMENT)

    @get:Internal
    val currentVersion: String by lazy {
        project.version.toString().trim()
    }

    @get:Internal
    val computedVersion: String by lazy {
        when {
            currentVersion == "unspecified" -> {
                logger.warn(MSG_UNSPECIFIED_VERSION)
                return@lazy currentVersion
            }
            newVersion != null -> {
                return@lazy newVersion!!
            }
            else -> {
                return@lazy FullVersion.fromString(currentVersion)
                    .incrementBaseVersion(increment)
                    .updateSuffix(
                        suffix,
                        (project.extensions.getByName("setVersions") as SetVersionsPluginExtension).defaultSuffix
                    )
                    .updatePrefix(prefix)
                    .toString()
            }
        }
    }

    @TaskAction
    open fun run() {
        if (newVersion != null && (suffix != null || prefix != null)) {
            throw InvalidUserDataException(MSG_NEW_VERSION_AND_SUFFIX_OR_PREFIX)
        }
        logger.quiet(computedVersion)
    }
}
