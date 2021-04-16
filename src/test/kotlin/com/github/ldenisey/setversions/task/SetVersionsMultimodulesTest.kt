package com.github.ldenisey.setversions.task

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class SetVersionsMultimodulesTest : AbstractTaskTest() {

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
            .withArguments("setVersions", "--increment=technical", "--suffix=false")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(module1BuildFile.readText().contains("1.0.1"))
        assertTrue(module2BuildFile.readText().contains("2.0.1"))
        assertNull(result.task(":setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:setVersions")?.outcome)
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
            .withArguments("setVersions", "--increment=technical")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(module1BuildFile.readText().contains("1.0.1-SNAPSHOT"))
        assertNull(result.task(":setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:setVersions")?.outcome)
    }

    @Test
    internal fun `Two modules with one skipped`() {
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
            
            setVersions {
                skipSet=true
            }
            """.trimIndent()
        )

        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "-Dsuffix=false")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(module1BuildFile.readText().contains("1.0.0"))
        assertNull(result.task(":setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:setVersions")?.outcome)
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
            .withArguments("setVersions", "--prefix=feature", "-Dincrement=3")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertTrue(module1BuildFile.readText().contains("feature-1.0.1-SNAPSHOT"))
        assertFalse(module2PropertiesFile.readText().contains("2.0.0-SNAPSHOT"))
        assertTrue(module2PropertiesFile.readText().contains("feature-2.0.1-SNAPSHOT"))
        assertTrue(module2PropertiesFile.readText().contains("3.0.0"))
        assertNull(result.task(":setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:setVersions")?.outcome)
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
            .withArguments("setVersions", "--increment=2")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertFalse(module1BuildFile.readText().contains("5.1.0-SNAPSHOT"))
        assertFalse(projectPropertiesFile.readText().contains("5.0.0-SNAPSHOT"))
        assertTrue(projectPropertiesFile.readText().contains("5.1.0-SNAPSHOT"))
        assertTrue(projectPropertiesFile.readText().contains("4.0.0"))
        assertFalse(module2BuildFile.readText().contains("2.1.0-SNAPSHOT"))
        assertFalse(module2PropertiesFile.readText().contains("2.0.0-SNAPSHOT"))
        assertTrue(module2PropertiesFile.readText().contains("2.1.0-SNAPSHOT"))
        assertTrue(module2PropertiesFile.readText().contains("3.0.0"))
        assertNull(result.task(":setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:setVersions")?.outcome)
    }

    @Test
    internal fun `Two modules with a common version defined in a buildSrc plugin`() {
        val module1BuildFile = File(module1, "build.gradle")
        module1BuildFile.writeText(
            """
            plugins {
                id("common")
                id("com.github.ldenisey.setversions")

            }
            """.trimIndent()
        )

        val module2BuildFile = File(module2, "build.gradle.kts")
        module2BuildFile.writeText(
            """
            plugins {
                id("common")
                id("com.github.ldenisey.setversions")
        }
            """.trimIndent()
        )

        val buildSrcFolder = File(projectFolder, "buildSrc")
        buildSrcFolder.mkdir()
        val buildSrcBuildGradle = File(buildSrcFolder, "build.gradle.kts")
        buildSrcBuildGradle.writeText(
            """
            plugins {
                `kotlin-dsl`
            }
    
            repositories {
                jcenter()
                mavenLocal()
            }
            """.trimIndent()
        )

        val buildSrcKotlinFolder = File(buildSrcFolder, "src/main/kotlin")
        buildSrcKotlinFolder.mkdirs()
        val buildSrcPlugin = File(buildSrcKotlinFolder, "common.gradle.kts")
        buildSrcPlugin.writeText(
            """
            version = "1.2.3-SNAPSHOT"
            """
        )

        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "-Dsuffix=false", "-Dincrement=major")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertFalse(buildSrcPlugin.readText().contains("1.2.3-SNAPSHOT"))
        assertTrue(buildSrcPlugin.readText().contains("2.0.0"))
        assertNull(result.task(":setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:setVersions")?.outcome)
    }

    @Test
    internal fun `Two modules with one defined in a buildSrc plugin and one from build file through properties`() {
        val module1BuildFile = File(module1, "build.gradle")
        module1BuildFile.writeText(
            """
            plugins {
                id("com.github.ldenisey.setversions")
            }

            version = project.findProperty("pluginVersion").toString()
            """.trimIndent()
        )

        val propertiesFile = File(projectFolder, "gradle.properties")
        propertiesFile.writeText(
            """
            anotherVersion = 4.0.0
            pluginVersion = 12.1.8-SNAPSHOT
            """.trimIndent()
        )

        val module2BuildFile = File(module2, "build.gradle.kts")
        module2BuildFile.writeText(
            """
            plugins {
                id("common")
                id("com.github.ldenisey.setversions")
         }
            """.trimIndent()
        )

        val module2PropertiesFile = File(module2, "gradle.properties")
        module2PropertiesFile.writeText(
            """
            anotherVersion = 3.0.0
            pluginVersion = 0.0.1-SNAPSHOT
            """.trimIndent()
        )

        val buildSrcFolder = File(projectFolder, "buildSrc")
        buildSrcFolder.mkdir()
        val buildSrcBuildGradle = File(buildSrcFolder, "build.gradle.kts")
        buildSrcBuildGradle.writeText(
            """
            plugins {
                `kotlin-dsl`
            }
    
            repositories {
                jcenter()
                mavenLocal()
            }
            """.trimIndent()
        )

        val buildSrcKotlinFolder = File(buildSrcFolder, "src/main/kotlin")
        buildSrcKotlinFolder.mkdirs()
        val buildSrcPlugin = File(buildSrcKotlinFolder, "common.gradle.kts")
        buildSrcPlugin.writeText(
            """
            version = project.findProperty("pluginVersion").toString()
            """
        )

        val result: BuildResult = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withArguments("setVersions", "--suffix=dev", "--increment=minor")
            .withPluginClasspath()
            .withDebug(isDebug)
            .build()

        assertFalse(propertiesFile.readText().contains("12.1.8-SNAPSHOT"))
        assertTrue(propertiesFile.readText().contains("12.2.0-dev"))
        assertTrue(propertiesFile.readText().contains("4.0.0"))
        assertFalse(module2PropertiesFile.readText().contains("0.0.1-SNAPSHOT"))
        assertTrue(module2PropertiesFile.readText().contains("0.1.0-dev"))
        assertTrue(module2PropertiesFile.readText().contains("3.0.0"))
        assertNull(result.task(":setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module1:setVersions")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":module2:setVersions")?.outcome)
    }

}