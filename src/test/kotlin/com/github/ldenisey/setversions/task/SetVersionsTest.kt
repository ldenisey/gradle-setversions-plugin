package com.github.ldenisey.setversions.task

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class SetVersionsTest : AbstractTaskTest() {

    @Test
    fun `Warning when version unspecified`() {
        val buildFile = File(projectFolder, "build.gradle")
        buildFile.writeText(
            """
        plugins {
            id("com.github.ldenisey.setversions")
        }

        group = "com.github.ldenisey"
        """.trimIndent()
        )

        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "--new-version=1.0.0")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains(GetVersions.MSG_UNSPECIFIED_VERSION))
        assertFalse(result.output.contains(SetVersions.MSG_DO_NOT_SET_SAME_VERSION))
        assertEquals(TaskOutcome.SUCCESS, result.task(":setVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Warning for unchanged version`() {
        val buildFile = File(projectFolder, "build.gradle")
        buildFile.writeText(
            """
        plugins {
            id("com.github.ldenisey.setversions")
        }

        group = "com.github.ldenisey"
        version = "1.0.0"
        """.trimIndent()
        )
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "--new-version=1.0.0")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(buildFile.readText().contains("1.0.0"))
        assertTrue(result.output.contains(SetVersions.MSG_DO_NOT_SET_SAME_VERSION))
        assertEquals(TaskOutcome.SUCCESS, result.task(":setVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Skipped by configuration`() {
        val buildFile = File(projectFolder, "build.gradle")
        buildFile.writeText(
            """
        plugins {
            id("com.github.ldenisey.setversions")
        }

        group = "com.github.ldenisey"
        version = "1.0.0"
        
        setVersions {
            skipSet = true
        }
        """.trimIndent()
        )
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "-Dnew-version=1.0.0", "-i")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(buildFile.readText().contains("1.0.0"))
        assertTrue(result.output.contains(SetVersions.MSG_SKIPPED))
        assertEquals(TaskOutcome.SUCCESS, result.task(":setVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Groovy build with assignment expression`() {
        val buildFile = File(projectFolder, "build.gradle")
        buildFile.writeText(
            """
        plugins {
            id 'com.github.ldenisey.setversions'
        }

        group = "com.github.ldenisey"
        version = "1.0.0"
        """.trimIndent()
        )
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "--new-version=2.0.0-SNAPSHOT")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertFalse(buildFile.readText().contains("1.0.0"))
        assertTrue(buildFile.readText().contains("2.0.0-SNAPSHOT"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":setVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Groovy build with call expression`() {
        val buildFile = File(projectFolder, "build.gradle")
        buildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }
    
            group 'com.github.ldenisey'
            version '1.0.0'
            """.trimIndent()
        )
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "--suffix=true")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(buildFile.readText().contains("1.0.0-SNAPSHOT"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":setVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Groovy build with local properties`() {
        val buildFile = File(projectFolder, "build.gradle")
        buildFile.writeText(
            """
            plugins {
                id "com.github.ldenisey.setversions"
            }
    
            group 'com.github.ldenisey'
            version "${"$"}{pluginVersion}"
            """.trimIndent()
        )
        val propertiesFile = File(projectFolder, "gradle.properties")
        propertiesFile.writeText(
            """
            anotherVersion=1.0.0
            pluginVersion=2.0.0-SNAPSHOT
            """.trimIndent().trim()
        )
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "--increment=2", "--suffix=false")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertFalse(propertiesFile.readText().contains("2.0.0-SNAPSHOT"))
        assertTrue(propertiesFile.readText().contains("2.1.0"))
        assertFalse(buildFile.readText().contains("2.1.0"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":setVersions")?.outcome)
        buildFile.delete()
        propertiesFile.delete()
    }

    @Test
    fun `Kotlin build gradle`() {
        val buildFile = File(projectFolder, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                `maven-publish`
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = "1.0.0"
            """.trimIndent()
        )
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "-Dsuffix=true")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(buildFile.readText().contains("1.0.0-SNAPSHOT"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":setVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Gradle properties only`() {
        val buildFile = File(projectFolder, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                `maven-publish`
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            """.trimIndent()
        )
        val propertiesFile = File(projectFolder, "gradle.properties")
        propertiesFile.writeText(
            """
            version=1.0.0
            pluginVersion=2.0.0-SNAPSHOT
            """.trimIndent().trim()
        )
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "--increment=2")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertFalse(propertiesFile.readText().contains("1.0.0"))
        assertTrue(propertiesFile.readText().contains("1.1.0"))
        assertTrue(propertiesFile.readText().contains("2.0.0-SNAPSHOT"))
        assertFalse(buildFile.readText().contains("1.1.0"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":setVersions")?.outcome)
        buildFile.delete()
        propertiesFile.delete()
    }
}