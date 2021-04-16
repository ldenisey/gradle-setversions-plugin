package com.github.ldenisey.setversions.version

import org.gradle.api.InvalidUserDataException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class FullVersionTest {

    @Test
    fun `Base parsing tests`() {
        assertEquals("1.0.0", FullVersion("", BaseVersion("1.0.0"), "").toString())
        assertEquals("id/12-1.0.0-dev", FullVersion("id/12", BaseVersion("1.0.0"), "dev").toString())
        assertEquals("1.0.0", FullVersion.fromString("1.0.0").toString())
        assertEquals("feature/12-1.0.0-dev", FullVersion.fromString("feature/12-1.0.0-dev").toString())
    }

    @Test
    fun `Incrementation tests`() {
        assertEquals("2.0.0", FullVersion.fromString("1.2.3").incrementBaseVersion("1").toString())
        assertEquals("1.3.0-dev", FullVersion.fromString("1.2.3-dev").incrementBaseVersion("2").toString())
        assertEquals("i12-1.2.4-dev", FullVersion.fromString("i12-1.2.3-dev").incrementBaseVersion("-1").toString())
    }

    @Test
    fun `Set prefix`() {
        assertEquals("feature-1.2.3", FullVersion.fromString("1.2.3").updatePrefix("feature").toString())
        assertEquals("1.2.3", FullVersion.fromString("feature-1.2.3").updatePrefix("false").toString())
        assertEquals("other-1.2.3", FullVersion.fromString("feature-1.2.3").updatePrefix("other").toString())
    }

    @Test
    fun `Set suffix`() {
        assertEquals("1.2.3-SNAPSHOT", FullVersion.fromString("1.2.3").updateSuffix("true", "SNAPSHOT").toString())
        assertEquals("1.2.3-dev", FullVersion.fromString("1.2.3-dev").updateSuffix("true", "dev").toString())
        assertEquals("1.2.3-dev", FullVersion.fromString("1.2.3-SNAPSHOT").updateSuffix("true", "dev").toString())

        assertEquals("1.2.3", FullVersion.fromString("1.2.3-dev").updateSuffix("false", "SNAPSHOT").toString())
        assertEquals("1.2.3", FullVersion.fromString("1.2.3").updateSuffix("false", "SNAPSHOT").toString())

        assertEquals("1.2.3-rc", FullVersion.fromString("1.2.3-SNAPSHOT").updateSuffix("rc", "SNAPSHOT").toString())
        assertEquals("1.2.3-rc", FullVersion.fromString("1.2.3").updateSuffix("rc", "SNAPSHOT").toString())
    }

    @Test
    fun `Multiple modifications`() {
        assertEquals(
            "myfeature-1.2.4-SNAPSHOT",
            FullVersion.fromString("1.2.3")
                .updateSuffix("SNAPSHOT", "SNAPSHOT")
                .incrementBaseVersion("technical")
                .updatePrefix("myfeature")
                .toString()
        )
        assertEquals(
            "2.0.0-rc",
            FullVersion.fromString("id/12-1.2.3-SNAPSHOT")
                .updateSuffix("rc", "SNAPSHOT")
                .incrementBaseVersion("1")
                .updatePrefix("false")
                .toString()
        )
    }

    @Test
    fun `Invalid version`() {
        assertThrows(InvalidUserDataException::class.java) { FullVersion.fromString("").toString() }
        assertThrows(InvalidUserDataException::class.java) { FullVersion.fromString("not a version").toString() }
        assertThrows(InvalidUserDataException::class.java) { FullVersion.fromString("not.a.version").toString() }
        assertThrows(InvalidUserDataException::class.java) { FullVersion.fromString("not 1.2 version").toString() }
        assertThrows(InvalidUserDataException::class.java) { FullVersion.fromString("1.0.0.").toString() }
    }

    @Test
    fun `Strange version`() {
        assertEquals("1.0.0-", FullVersion.fromString("1.0.0-").toString())
    }
}