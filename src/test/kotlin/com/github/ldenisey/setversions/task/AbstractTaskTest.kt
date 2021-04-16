package com.github.ldenisey.setversions.task

import org.gradle.internal.impldep.org.junit.internal.management.ManagementFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal abstract class AbstractTaskTest {

    companion object {
        @TempDir
        lateinit var projectFolder: File
    }

    val isDebug: Boolean by lazy {
        val arguments = ManagementFactory.getRuntimeMXBean().inputArguments
        for (argument in arguments)
            if (argument.contains("-Xdebug") || argument.startsWith("-agentlib:jdwp"))
                return@lazy true
        return@lazy false
    }
}