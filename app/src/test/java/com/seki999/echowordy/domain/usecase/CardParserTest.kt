package com.seki999.echowordy.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardParserTest {

    @Test
    fun `single card is parsed into word and body`() {
        val raw = "abstain\n英式 /əbˈsteɪn/\n美式 /əbˈsteɪn/\n意思 : 戒除；避免（尤指酒、食物等）\nabstain from alcohol 戒酒"

        val cards = CardParser.parse(raw)

        assertEquals(1, cards.size)
        assertEquals("abstain", cards[0].word)
        assertEquals(
            "英式 /əbˈsteɪn/\n美式 /əbˈsteɪn/\n意思 : 戒除；避免（尤指酒、食物等）\nabstain from alcohol 戒酒",
            cards[0].body,
        )
    }

    @Test
    fun `multiple cards separated by a single blank line`() {
        val raw = """
            abstain
            英式 /əbˈsteɪn/
            意思 : 戒除

            inaugural
            英式 /ɪˈnɔːɡjərəl/
            意思 : 就职的
        """.trimIndent()

        val cards = CardParser.parse(raw)

        assertEquals(2, cards.size)
        assertEquals("abstain", cards[0].word)
        assertEquals("inaugural", cards[1].word)
    }

    @Test
    fun `extra and trailing blank lines are ignored`() {
        val raw = "\n\nabstain\nbody line\n\n\n\ninaugural\nbody line\n\n\n\n"

        val cards = CardParser.parse(raw)

        assertEquals(2, cards.size)
        assertEquals("abstain", cards[0].word)
        assertEquals("inaugural", cards[1].word)
    }

    @Test
    fun `windows CRLF line endings are supported`() {
        val raw = "abstain\r\nbody line one\r\nbody line two\r\n\r\ninaugural\r\nbody line"

        val cards = CardParser.parse(raw)

        assertEquals(2, cards.size)
        assertEquals("abstain", cards[0].word)
        assertEquals("body line one\nbody line two", cards[0].body)
        assertEquals("inaugural", cards[1].word)
    }

    @Test
    fun `unix LF line endings are supported`() {
        val raw = "abstain\nbody line\n\ninaugural\nbody line"

        val cards = CardParser.parse(raw)

        assertEquals(2, cards.size)
    }

    @Test
    fun `unicode IPA and chinese text are preserved exactly`() {
        val raw = "ornament\n英式 /ˈɔːnəmənt/\n意思 : 装饰品；点缀"

        val cards = CardParser.parse(raw)

        assertEquals(1, cards.size)
        assertEquals("英式 /ˈɔːnəmənt/", cards[0].body.lines()[0])
        assertTrue(cards[0].body.contains("装饰品；点缀"))
    }

    @Test
    fun `empty input produces no cards`() {
        val cards = CardParser.parse("")
        assertTrue(cards.isEmpty())
    }

    @Test
    fun `whitespace only input produces no cards`() {
        val cards = CardParser.parse("   \n\n   \n\t\n")
        assertTrue(cards.isEmpty())
    }

    @Test
    fun `blank lines with accidental spaces still separate cards`() {
        val raw = "abstain\nbody\n   \n  \ninaugural\nbody"

        val cards = CardParser.parse(raw)

        assertEquals(2, cards.size)
        assertEquals("abstain", cards[0].word)
        assertEquals("inaugural", cards[1].word)
    }

    @Test
    fun `word line surrounding whitespace is trimmed`() {
        val raw = "  abstain  \nbody line"

        val cards = CardParser.parse(raw)

        assertEquals("abstain", cards[0].word)
    }

    @Test
    fun `card with only a word line has empty body`() {
        val raw = "abstain"

        val cards = CardParser.parse(raw)

        assertEquals(1, cards.size)
        assertEquals("abstain", cards[0].word)
        assertEquals("", cards[0].body)
    }

    @Test
    fun `does not crash on malformed or mixed content`() {
        val raw = "\n\n   \n\nabstain\nbody\n\n\n   \n\ninaugural\n\n\n"

        val cards = CardParser.parse(raw)

        assertEquals(2, cards.size)
    }
}
