package com.github.ldenisey.setversions

import com.github.ldenisey.setversions.task.GetVersions
import com.github.ldenisey.setversions.task.SetVersions
import org.gradle.api.Plugin
import org.gradle.api.Project

class SetVersionsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("setVersions", SetVersionsPluginExtension::class.java)
        project.tasks.register("getVersions", GetVersions::class.java)
        project.tasks.register("setVersions", SetVersions::class.java)
    }
}