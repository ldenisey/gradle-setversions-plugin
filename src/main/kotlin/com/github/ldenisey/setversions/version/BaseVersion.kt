package com.github.ldenisey.setversions.version

import org.gradle.api.InvalidUserDataException

class BaseVersion(var version: String) {

    companion object {
        const val ALIAS_MAJOR = "major"
        const val ALIAS_MINOR = "minor"
        const val ALIAS_TECHNICAL = "technical"
        const val MSG_UNKNOWN_INCREMENT =
            "Increment position must be a digit, '$ALIAS_MAJOR', '$ALIAS_MINOR', or '$ALIAS_TECHNICAL'."
        const val MSG_INCREMENT_POSITION_OUT_OF_BOUND =
            "Increment position (%s) is greater than the numbers of digits in the version (%s)."
    }

    private val digits: MutableList<Int> = version.split('.').map { it.toInt() }.toMutableList()

    /**
     * Increment base version digit at [incrementPosition], set following digits to 0.
     *
     * [incrementPosition] can be :
     * - a positive number, 1 being first digit
     * - a negative number, -1 being last digit
     * - one of predefined aliases :
     *   - major = 1
     *   - minor = 2
     *   - technical = 3
     *
     * Return this, for development convenience.
     */
    fun increment(incrementPosition: String?): BaseVersion {
        // Convert increment option
        val position: Int = when (incrementPosition) {
            "", null -> return this
            ALIAS_MAJOR -> 1
            ALIAS_MINOR -> 2
            ALIAS_TECHNICAL -> 3
            else -> {
                var intPosition: Int
                try {
                    intPosition = incrementPosition.toInt()
                } catch (nfe: NumberFormatException) {
                    throw InvalidUserDataException(MSG_UNKNOWN_INCREMENT, nfe)
                }
                if (intPosition < 0) {
                    intPosition += digits.size + 1
                }
                intPosition
            }
        }
        if (1 > position || position > digits.size) {
            throw InvalidUserDataException(MSG_INCREMENT_POSITION_OUT_OF_BOUND.format(incrementPosition, toString()))
        }

        // Increment digits
        digits[position - 1] = digits[position - 1] + 1
        for (i in position until digits.size) {
            digits[i] = 0
        }

        return this
    }

    override fun toString(): String {
        return digits.joinToString(".")
    }
}