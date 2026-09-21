package com.seki999.echowordy.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class UnknownListNameGeneratorTest {

    @Test
    fun `no existing unknown list uses the base suffix`() {
        val name = UnknownListNameGenerator.generateName("TOEIC 01", existingNames = emptyList())
        assertEquals("TOEIC 01 - Unknown Words", name)
    }

    @Test
    fun `one existing unknown list bumps to suffix 2`() {
        val name = UnknownListNameGenerator.generateName(
            sourceListName = "TOEIC 01",
            existingNames = listOf("TOEIC 01", "TOEIC 01 - Unknown Words"),
        )
        assertEquals("TOEIC 01 - Unknown Words 2", name)
    }

    @Test
    fun `two existing unknown lists bump to suffix 3`() {
        val name = UnknownListNameGenerator.generateName(
            sourceListName = "TOEIC 01",
            existingNames = listOf(
                "TOEIC 01",
                "TOEIC 01 - Unknown Words",
                "TOEIC 01 - Unknown Words 2",
            ),
        )
        assertEquals("TOEIC 01 - Unknown Words 3", name)
    }

    @Test
    fun `generating again from an unknown words list never doubles the suffix`() {
        val name = UnknownListNameGenerator.generateName(
            sourceListName = "TOEIC 01 - Unknown Words",
            existingNames = listOf("TOEIC 01", "TOEIC 01 - Unknown Words"),
        )
        assertEquals("TOEIC 01 - Unknown Words 2", name)
    }

    @Test
    fun `generating from an already-numbered unknown words list continues the sequence`() {
        val name = UnknownListNameGenerator.generateName(
            sourceListName = "TOEIC 01 - Unknown Words 2",
            existingNames = listOf(
                "TOEIC 01",
                "TOEIC 01 - Unknown Words",
                "TOEIC 01 - Unknown Words 2",
            ),
        )
        assertEquals("TOEIC 01 - Unknown Words 3", name)
    }

    @Test
    fun `a gap in the numbering still fills the lowest free suffix`() {
        val name = UnknownListNameGenerator.generateName(
            sourceListName = "TOEIC 01",
            existingNames = listOf(
                "TOEIC 01",
                "TOEIC 01 - Unknown Words",
                "TOEIC 01 - Unknown Words 3",
            ),
        )
        assertEquals("TOEIC 01 - Unknown Words 2", name)
    }
}
