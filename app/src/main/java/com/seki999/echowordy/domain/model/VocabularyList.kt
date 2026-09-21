package com.seki999.echowordy.domain.model

data class VocabularyList(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class VocabularyListSummary(
    val id: Long,
    val name: String,
    val cardCount: Int,
)
