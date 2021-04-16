package com.github.ldenisey.setversions.task

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class GetVersionsTest : AbstractTaskTest() {

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
            .withArguments("getVersions")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains(GetVersions.MSG_UNSPECIFIED_VERSION))
        assertTrue(result.output.contains("unspecified"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":getVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Error when options new-version and suffix are given`() {
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
            .withArguments("getVersions", "--new-version=1.0.0", "--suffix=dev")
            .withPluginClasspath()
            .withDebug(isDebug)
            .buildAndFail()

        assertTrue(result.output.contains(GetVersions.MSG_NEW_VERSION_AND_SUFFIX_OR_PREFIX))
        assertEquals(TaskOutcome.FAILED, result.task(":getVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Groovy build with assignment expression`() {
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
            .withArguments("getVersions")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains("1.0.0"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":getVersions")?.outcome)
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
            .withArguments("getVersions")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains("1.0.0"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":getVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Groovy build with local properties`() {
        val buildFile = File(projectFolder, "build.gradle")
        buildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
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
            .withArguments("getVersions")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertFalse(result.output.contains("1.0.0"))
        assertTrue(result.output.contains("2.0.0-SNAPSHOT"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":getVersions")?.outcome)
        buildFile.delete()
        propertiesFile.delete()
    }

    @Test
    fun `Kotlin build gradle`() {
        val buildFile = File(projectFolder, "build.gradle.kts")
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
            .withArguments("getVersions")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains("1.0.0"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":getVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Version transformation`() {
        val buildFile = File(projectFolder, "build.gradle.kts")
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
            .withArguments("getVersions", "--increment=2", "--suffix=true", "--prefix=myfeature")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains("myfeature-1.1.0-SNAPSHOT"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":getVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Kotlin build with default suffix configuration`() {
        val buildFile = File(projectFolder, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = "1.0.0"
            
            setVersions {
                defaultSuffix = "dev"        
            }
            """.trimIndent()
        )
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("getVersions", "-Dsuffix=true")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains("1.0.0-dev"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":getVersions")?.outcome)
        buildFile.delete()
    }

    @Test
    fun `Groovy build with default suffix configuration`() {
        val buildFile = File(projectFolder, "build.gradle")
        buildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = "1.0.0"
            
            setVersions {
                defaultSuffix = "dev"
                skipSet = false
            }
            """.trimIndent()
        )
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("getVersions", "-Dsuffix=true")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains("1.0.0-dev"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":getVersions")?.outcome)
        buildFile.delete()
    }
}