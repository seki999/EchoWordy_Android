package com.seki999.echowordy.ui.review

/** Display-only segmentation: persisted card text and TTS input are never changed. */
internal enum class ReadingRole { IPA, MEANING, COLLOCATION, TRANSLATION, CHINESE_EXAMPLE, ENGLISH_EXAMPLE, BODY }
internal data class ReadingBlock(val text: String, val role: ReadingRole)
internal fun readingBlocks(body: String): List<ReadingBlock> {
    val blocks = mutableListOf<ReadingBlock>()
    body.lines().forEach { line ->
        // Split only after explicit Chinese sentence punctuation followed by Latin text.
        val parts = line.split(Regex("(?<=[。！？])\\s*(?=[A-Za-z])"))
        parts.forEach part@ { text ->
            if (text.isBlank()) return@part
            val han = text.any { it in '\u4e00'..'\u9fff' }
            val latin = text.any { it in 'a'..'z' || it in 'A'..'Z' }
            val ipa = Regex("/[^/]+/|\\[[^]]*[ˈˌəɒɑɪʊɛʌθðʃʒŋ][^]]*]").containsMatchIn(text)
            val sentence = text.any { it in "。！？!?" } || (latin && text.trimEnd().endsWith("."))
            val role = when {
                ipa -> ReadingRole.IPA
                han && sentence -> ReadingRole.CHINESE_EXAMPLE
                !han && latin && sentence -> ReadingRole.ENGLISH_EXAMPLE
                han && !latin && blocks.none { it.role == ReadingRole.MEANING } -> ReadingRole.MEANING
                han && !latin -> ReadingRole.TRANSLATION
                !han && latin -> ReadingRole.COLLOCATION
                else -> ReadingRole.BODY
            }
            blocks.add(ReadingBlock(text, role))
        }
    }
    return blocks
}
