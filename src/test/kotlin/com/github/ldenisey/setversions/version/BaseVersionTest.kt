package com.github.ldenisey.setversions.version

import org.gradle.api.InvalidUserDataException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class BaseVersionTest {

    @Test
    fun `incrementation with aliases`() {
        assertEquals("2.0.0", BaseVersion("1.2.3").increment("major").toString())
        assertEquals("1.3.0", BaseVersion("1.2.3").increment("minor").toString())
        assertEquals("1.2.4", BaseVersion("1.2.3").increment("technical").toString())

        assertEquals("2.0", BaseVersion("1.2").increment("major").toString())
        assertEquals("1.3", BaseVersion("1.2").increment("minor").toString())
        assertThrows(InvalidUserDataException::class.java) { BaseVersion("1.2").increment("technical") }

        assertEquals("2", BaseVersion("1").increment("major").toString())
        assertEquals("2.0.0.0", BaseVersion("1.2.3.4").increment("major").toString())
        assertEquals("1.3.0.0.0", BaseVersion("1.2.3.4.5").increment("minor").toString())
        assertEquals("1.2.4.0.0.0", BaseVersion("1.2.3.4.5.6").increment("technical").toString())
    }

    @Test
    fun `Increment with positive position`() {
        assertEquals("11.0.0", BaseVersion("10.2.399").increment("1").toString())
        assertEquals("10.3.0", BaseVersion("10.2.399").increment("2").toString())
        assertEquals("10.2.400", BaseVersion("10.2.399").increment("3").toString())

        assertThrows(InvalidUserDataException::class.java) { BaseVersion("1.2").increment("0") }
        assertThrows(InvalidUserDataException::class.java) { BaseVersion("1.2").increment("4") }
    }

    @Test
    fun `Increment with negative position`() {
        assertEquals("2.0.0", BaseVersion("1.2.3").increment("-3").toString())
        assertEquals("1.2.3.5", BaseVersion("1.2.3.4").increment("-1").toString())
        assertEquals("1.2.3.4.6", BaseVersion("1.2.3.4.5").increment("-1").toString())
        assertEquals("1.2.3.5.0.0", BaseVersion("1.2.3.4.5.6").increment("-3").toString())

        assertThrows(InvalidUserDataException::class.java) { BaseVersion("1.2").increment("-0") }
        assertThrows(InvalidUserDataException::class.java) { BaseVersion("1.2").increment("-3") }
    }

    @Test
    fun `Invalid base version`() {
        assertThrows(NumberFormatException::class.java) { BaseVersion("").toString() }
        assertThrows(NumberFormatException::class.java) { BaseVersion("1.").toString() }
        assertThrows(NumberFormatException::class.java) { BaseVersion("1.0.0.").toString() }
        assertThrows(NumberFormatException::class.java) { BaseVersion("1.000.0.").toString() }
        assertThrows(NumberFormatException::class.java) { BaseVersion("1.010.0.").toString() }
        assertThrows(NumberFormatException::class.java) { BaseVersion("toto").toString() }
        assertThrows(NumberFormatException::class.java) { BaseVersion("titi.toto").toString() }
    }
}