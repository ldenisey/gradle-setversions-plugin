package com.github.ldenisey.setversions.parser

import java.io.File
import java.util.*

class PropertiesParser(file: File) {

    val file: File = file

    val content: String = file.readText()

    private val properties: Properties by lazy {
        return@lazy Properties().apply {
            file.reader().use {
                load(it)
            }
        }
    }

    fun getValue(key: String): String? {
        return properties[key] as String?
    }

    /**
     * Get the keys of this properties file that are contained in [string].
     */
    fun getKeysContainedIn(string: String): List<String> {
        return properties.keys.filter {
            string.contains(it as String)
        }.map { it.toString() }
    }

    /**
     * Get the definition of [key], with its original formatting.
     */
    fun getExpression(key: String): String? {
        val value: String? = properties[key] as String
        if (value?.isNotEmpty() == true) {
            return Regex("""(?m)^(\s*${key}\s*[=:]\s*['"]?${value}['"]?\s*)$""").find(content)?.groupValues?.get(1)
        }
        return null
    }
}