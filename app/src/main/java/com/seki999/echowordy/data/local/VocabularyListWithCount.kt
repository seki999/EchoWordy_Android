package com.seki999.echowordy.data.local

data class VocabularyListWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val cardCount: Int,
)
