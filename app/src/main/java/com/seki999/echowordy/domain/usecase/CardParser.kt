package com.seki999.echowordy.domain.usecase

/** A card parsed from raw pasted text, before it is persisted. */
data class ParsedCard(
    val word: String,
    val body: String,
)

/**
 * Splits raw pasted text into individual vocabulary cards.
 *
 * Rules:
 * - One or more blank lines (including whitespace-only lines) separate cards.
 * - Within a card block, the first non-empty line is the word; every other
 *   line is the body, preserved exactly as typed (no trimming, no rewriting).
 * - Empty blocks (extra blank lines) are ignored.
 * - Windows CRLF and Unix LF line endings are both supported.
 */
object CardParser {

    fun parse(raw: String): List<ParsedCard> {
        val normalized = raw.replace("\r\n", "\n").replace("\r", "\n")
        val lines = normalized.split("\n")

        val blocks = mutableListOf<MutableList<String>>()
        var current: MutableList<String>? = null

        for (line in lines) {
            if (line.trim().isEmpty()) {
                current = null
            } else {
                val block = current ?: mutableListOf<String>().also {
                    blocks.add(it)
                    current = it
                }
                block.add(line)
            }
        }

        return blocks.mapNotNull { block ->
            val word = block.first().trim()
            if (word.isEmpty()) return@mapNotNull null
            val body = block.drop(1).joinToString("\n")
            ParsedCard(word = word, body = body)
        }
    }
}
