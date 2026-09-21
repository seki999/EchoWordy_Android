package com.seki999.echowordy.domain.usecase

/**
 * Generates a clean, non-duplicated name for an auto-generated "Unknown Words"
 * list, given the name of the list the review session was started from and
 * the names of all lists that already exist.
 *
 * Examples:
 *  - "TOEIC 01" -> "TOEIC 01 - Unknown Words"
 *  - "TOEIC 01" (when "TOEIC 01 - Unknown Words" exists) -> "TOEIC 01 - Unknown Words 2"
 *  - "TOEIC 01 - Unknown Words" -> "TOEIC 01 - Unknown Words 2" (never doubles the suffix)
 *  - "TOEIC 01 - Unknown Words 2" -> "TOEIC 01 - Unknown Words 3"
 */
object UnknownListNameGenerator {

    private val SUFFIX_REGEX = Regex("^(.*) - Unknown Words(?: (\\d+))?$")

    fun generateName(sourceListName: String, existingNames: Collection<String>): String {
        val root = extractRoot(sourceListName)
        val existing = existingNames.toHashSet()

        val base = "$root - Unknown Words"
        if (base !in existing) return base

        var suffix = 2
        while ("$base $suffix" in existing) {
            suffix++
        }
        return "$base $suffix"
    }

    /** Strips any existing " - Unknown Words" / " - Unknown Words N" suffix, however deep. */
    private fun extractRoot(name: String): String {
        var root = name
        var match = SUFFIX_REGEX.matchEntire(root)
        while (match != null) {
            root = match.groupValues[1]
            match = SUFFIX_REGEX.matchEntire(root)
        }
        return root
    }
}
