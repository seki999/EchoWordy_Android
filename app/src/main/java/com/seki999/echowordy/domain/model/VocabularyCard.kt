package com.seki999.echowordy.domain.model

/**
 * A single vocabulary card. [word] is the first non-empty line of the card
 * (the only part that is pronounced); [body] is everything after it, shown
 * exactly as entered by the user.
 */
data class VocabularyCard(
    val id: Long,
    val listId: Long,
    val word: String,
    val body: String,
    val sortOrder: Int,
)
