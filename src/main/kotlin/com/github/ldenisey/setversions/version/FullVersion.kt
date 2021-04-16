package com.github.ldenisey.setversions.version

import org.gradle.api.InvalidUserDataException

data class FullVersion(var prefix: String?, var baseVersion: BaseVersion, var suffix: String?) {

    companion object {
        private const val MSG_BASE_VERSION_NOT_FOUND = "Can not find base version from current version."
        private const val MSG_FOUND_WHITE_CHAR = "White char are not allowed in version."
        private const val MSG_PARSING_MISSING_CHARS = "An error occurred during version parsing."

        fun fromString(version: String): FullVersion {
            if (Regex("""\s""").containsMatchIn(version)) {
                throw InvalidUserDataException(MSG_FOUND_WHITE_CHAR)
            }
            val baseVersion: String = Regex("""((\d+\.)+\d+)""")
                .find(version)?.groupValues?.get(1)
                ?: throw InvalidUserDataException(MSG_BASE_VERSION_NOT_FOUND)
            val prefix: String? = Regex("""([A-Za-z0-9/_-]*)$baseVersion""")
                .find(version)?.groupValues?.get(1)
            val suffix: String? = Regex("""$baseVersion([A-Za-z0-9/_-]*)""")
                .find(version)?.groupValues?.get(1)
            if (version != prefix + baseVersion + suffix) {
                throw InvalidUserDataException(MSG_PARSING_MISSING_CHARS)
            }
            return FullVersion(prefix, BaseVersion(baseVersion), suffix)
        }
    }

    /**
     * Update suffix based on [suffixUpdate] and [defaultSuffix].
     * Return this, for development convenience.
     */
    fun updateSuffix(suffixUpdate: String?, defaultSuffix: String): FullVersion {
        suffix = when (suffixUpdate) {
            "", null -> suffix
            "true" -> defaultSuffix
            "false" -> ""
            else -> suffixUpdate.trimStart('-')
        }
        return this
    }

    /**
     * Update prefix based on [prefixUpdate].
     * Return this, for development convenience.
     */
    fun updatePrefix(prefixUpdate: String?): FullVersion {
        prefix = when (prefixUpdate) {
            "", null -> prefix
            "false" -> ""
            else -> prefixUpdate.trimEnd('-')
        }
        return this
    }

    /**
     * Increment base version digit at [incrementPosition], set following digits to 0.
     * Return this, for development convenience.
     */
    fun incrementBaseVersion(incrementPosition: String?): FullVersion {
        baseVersion.increment(incrementPosition)
        return this
    }

    override fun toString(): String {
        return (if (prefix?.isNotEmpty() == true) "${prefix!!.trimEnd('-')}-" else "") +
                baseVersion.toString() +
                (if (suffix?.isNotEmpty() == true) "-${suffix!!.trimStart('-')}" else "")
    }
}