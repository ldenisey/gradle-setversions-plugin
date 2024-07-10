package com.github.ldenisey.setversions.task

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class GetVersionsMultimodulesTest : AbstractTaskTest() {

    private lateinit var settings: File
    private lateinit var module1: File
    private lateinit var module2: File

    @BeforeEach
    fun prepare() {
        settings = File(projectFolder, "settings.gradle")
        settings.writeText("rootProject.name = \"unit-tests\"\ninclude(\"module1\", \"module2\")")
        module1 = File(projectFolder, "module1")
        module1.mkdir()
        module2 = File(projectFolder, "module2")
        module2.mkdir()
    }

    @AfterEach
    fun clean() {
        settings.delete()
        module1.deleteRecursively()
        module2.deleteRecursively()
    }

    @Test
    internal fun `Two modules with hardcoded version`() {
        val module1BuildFile = File(module1, "build.gradle")
        module1BuildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = "1.0.0-SNAPSHOT"
            """.trimIndent()
        )

        val module2BuildFile = File(module2, "build.gradle.kts")
        module2BuildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = "2.0.0-SNAPSHOT"
            """.trimIndent()
        )

        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("getVersions")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains("1.0.0-SNAPSHOT"))
        assertTrue(result.output.contains("2.0.0-SNAPSHOT"))
        assertNull(result.task(":getVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:getVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:getVersions")?.outcome)
    }

    @Test
    internal fun `Two modules with one without version`() {
        val module1BuildFile = File(module1, "build.gradle")
        module1BuildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = "1.0.0-SNAPSHOT"
            """.trimIndent()
        )

        val module2BuildFile = File(module2, "build.gradle.kts")
        module2BuildFile.writeText(
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

        assertTrue(result.output.contains("1.0.0-SNAPSHOT"))
        assertTrue(result.output.contains(GetVersions.UNSPECIFIED))
        assertNull(result.task(":getVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:getVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:getVersions")?.outcome)
    }

    @Test
    internal fun `Two modules with one with version in local properties`() {
        val module1BuildFile = File(module1, "build.gradle")
        module1BuildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = "1.0.0-SNAPSHOT"
            """.trimIndent()
        )

        val module2BuildFile = File(module2, "build.gradle.kts")
        module2BuildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = project.findProperty("pluginVersion").toString()
            """.trimIndent()
        )

        val module2PropertiesFile = File(module2, "gradle.properties")
        module2PropertiesFile.writeText(
            """
            anotherVersion=3.0.0
            pluginVersion=2.0.0-SNAPSHOT
            """.trimIndent()
        )

        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("getVersions")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains("1.0.0-SNAPSHOT"))
        assertTrue(result.output.contains("2.0.0-SNAPSHOT"))
        assertFalse(result.output.contains("3.0.0"))
        assertNull(result.task(":getVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:getVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:getVersions")?.outcome)
    }

    @Test
    internal fun `Two modules with one with version in local properties and one in global properties`() {
        val module1BuildFile = File(module1, "build.gradle")
        module1BuildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = project.findProperty("pluginVersion").toString()
            """.trimIndent()
        )

        val projectPropertiesFile = File(projectFolder, "gradle.properties")
        projectPropertiesFile.writeText(
            """
            anotherVersion=4.0.0
            pluginVersion=5.0.0-SNAPSHOT
            """.trimIndent()
        )

        val module2BuildFile = File(module2, "build.gradle.kts")
        module2BuildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            group = "com.github.ldenisey"
            version = project.findProperty("pluginVersion").toString()
            """.trimIndent()
        )

        val module2PropertiesFile = File(module2, "gradle.properties")
        module2PropertiesFile.writeText(
            """
            anotherVersion=3.0.0
            pluginVersion=2.0.0-SNAPSHOT
            """.trimIndent()
        )

        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("getVersions")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(result.output.contains("5.0.0-SNAPSHOT"))
        assertFalse(result.output.contains("4.0.0"))
        assertTrue(result.output.contains("2.0.0-SNAPSHOT"))
        assertFalse(result.output.contains("3.0.0"))
        assertNull(result.task(":getVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:getVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:getVersions")?.outcome)
    }
}