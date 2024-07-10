package com.github.ldenisey.setversions.task

import com.github.ldenisey.setversions.SetVersionsPluginExtension
import com.github.ldenisey.setversions.parser.*
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SetVersions : GetVersions() {

    companion object {
        const val MSG_SKIPPED = "Version update skipped by configuration."
        const val MSG_DO_NOT_SET_SAME_VERSION = "Trying to set the same version already configured, skipping task."
        const val MSG_VERSION_ALREADY_UPDATED = "Version already updated, probably in a previous task execution."
    }

    @TaskAction
    override fun run() {
        if ((project.extensions.getByName("setVersions") as SetVersionsPluginExtension).skipSet) {
            logger.info(MSG_SKIPPED)
            return
        }

        // Skip if version is not defined already
        if (currentVersion == UNSPECIFIED) {
            logger.warn(MSG_UNSPECIFIED_VERSION)
            return
        }

        if (currentVersion == computedVersion) {
            logger.warn(MSG_DO_NOT_SET_SAME_VERSION)
            return
        }

        // Try and update version from project build file
        if (updateFromBuildFile(project.buildFile)) {
            return
        }

        // Try and update version directly declared in a gradle.properties
        if (updateFromProperties("version")) {
            return
        }

        // Try and update version from a buildSrc plugin
        val buildSrcRootFile: File = project.rootProject.file("buildSrc")
        if (buildSrcRootFile.exists()) {
            logger.info("Looking in buildSrc for the version definition in a plugin.")
            val projectPlugins: List<String> = project.plugins.map {
                it::class.java.name.replace("Plugin", "").toLowerCase()
            }
            for (curFile in File(buildSrcRootFile, "src/main").walk()) {
                if ((curFile.name.endsWith(".gradle") || curFile.name.endsWith(".gradle.kts"))
                    && (projectPlugins.contains(curFile.name.replace(Regex("\\.gradle(\\.kts)?"), ""))
                            && updateFromBuildFile(curFile))
                ) {
                    return
                }
            }
        }

        // Did not find the version anywhere ...
        throw GradleException("Can not find the definition of current version '$currentVersion'. Activate info traces to see where it was searched.")
    }

    private fun updateFromBuildFile(buildFile: File): Boolean {
        var found = false
        val buildFileParser: BuildFileParser =
            if (buildFile.name.endsWith(".gradle.kts")) KotlinBuildFileParser(buildFile)
            else GroovyBuildFileParser(buildFile)

        val versionDefinition: String? = try {
            buildFileParser.versionDefinition
        } catch (ignored: VersionNotFound) {
            null
        }

        if (versionDefinition != null) {
            if (versionDefinition.contains(currentVersion)) {
                logger.info("Version defined in file ${buildFile.path}, updating it.")
                updateInFile(buildFileParser)
                found = true
            } else if (project.tasks.size > 1 && versionDefinition.contains(computedVersion)) {
                logger.info(MSG_VERSION_ALREADY_UPDATED)
                found = true
            } else {
                logger.info("Version not defined directly in build file, looking for a variable in gradle.properties files.")
                found = updateFromProperties(buildFileParser.versionDefinition)
            }
        }

        if (!found) {
            logger.info("No version definition found in ${buildFile.path}.")
        }
        return found
    }

    private fun updateFromProperties(versionDefinition: String): Boolean {
        var found = false
        var curProject: Project? = project
        while (!found && curProject != null) {
            val gradlePropertiesFile: File = curProject.file("gradle.properties")
            if (gradlePropertiesFile.exists()) {
                logger.info("Searching version variable in ${gradlePropertiesFile.path}")
                val propertiesParser = PropertiesParser(gradlePropertiesFile)
                // Loop in keys that are found in build.gradle version definition
                for (curKey in propertiesParser.getKeysContainedIn(versionDefinition)) {
                    val curValue = propertiesParser.getValue(curKey)
                    if (curValue == currentVersion) {
                        val expression = propertiesParser.getExpression(curKey)
                        if (expression != null) {
                            updateInFile(
                                propertiesParser.file, expression,
                                propertiesParser.content
                            )
                            found = true
                        }
                    } else if (project.tasks.size > 1 && curValue == computedVersion) {
                        logger.info(MSG_VERSION_ALREADY_UPDATED)
                        found = true
                    }
                }
                logger.info("Could not find a variable matching project version in ${gradlePropertiesFile.path}.")
            }
            curProject = curProject.parent
        }
        return found
    }

    private fun updateInFile(buildFileParser: BuildFileParser) {
        updateInFile(buildFileParser.file, buildFileParser.versionExpression, buildFileParser.content)
    }

    private fun updateInFile(file: File, versionExpression: String, content: String = file.readText()) {
        file.delete()
        file.writeText(content.replace(versionExpression, versionExpression.replace(currentVersion, computedVersion)))
    }
}