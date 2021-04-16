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
    }

    @TaskAction
    override fun run() {
        if ((project.extensions.getByName("setVersions") as SetVersionsPluginExtension).skipSet) {
            logger.info(MSG_SKIPPED)
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
            if (buildFile.endsWith(".gradle.kts")) KotlinBuildFileParser(buildFile) else GroovyBuildFileParser(buildFile)

        val versionDefinition: String? = try {
            buildFileParser.versionDefinition
        } catch (vnfe: VersionNotFound) {
            null
        }

        if (versionDefinition != null) {
            if (versionDefinition.contains(currentVersion)) {
                logger.info("Version defined in file ${buildFile.path}, updating it.")
                updateInFile(buildFileParser)
                found = true
            } else if (project.tasks.size > 1 && versionDefinition.contains(computedVersion)) {
                logger.info("Version already updated, probably in a previous task execution.")
                found = true
            } else {
                logger.info("Version not defined directly in build file, looking for a variable in gradle.properties files.")
                var curProject: Project? = project
                while (!found && curProject != null) {
                    found = updateFromProperties(buildFileParser, curProject.file("gradle.properties"))
                    curProject = curProject.parent
                }
            }
        }

        if (!found) {
            logger.info("No version definition found in ${buildFile.path}.")
        }
        return found
    }

    private fun updateFromProperties(buildGradleParser: BuildFileParser, gradlePropertiesFile: File): Boolean {
        var found = false
        if (gradlePropertiesFile.exists()) {
            logger.info("Searching version variable in ${gradlePropertiesFile.path}")
            val propertiesParser = PropertiesParser(gradlePropertiesFile)
            // Loop in keys that are found in build.gradle version definition
            for (curKey in propertiesParser.getKeysContainedIn(buildGradleParser.versionDefinition)) {
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
                    logger.info("Version already updated, probably in a previous task execution.")
                    found = true
                }
            }
            logger.info("Could not find a variable matching project version in ${gradlePropertiesFile.path}.")
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