package com.seki999.echowordy.domain.usecase

/**
 * Validates a user-typed card duration. Only whole numbers of seconds greater
 * than zero are accepted; anything else (blank, non-numeric, zero, negative,
 * decimals, or too large to fit an [Int]) is rejected.
 */
object CustomDurationParser {

    fun parseSeconds(input: String): Int? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val value = trimmed.toIntOrNull() ?: return null
        return if (value > 0) value else null
    }
}
