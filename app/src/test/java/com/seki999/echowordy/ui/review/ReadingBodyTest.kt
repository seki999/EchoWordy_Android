package com.seki999.echowordy.ui.review

import org.junit.Assert.*
import org.junit.Test

class ReadingBodyTest {
    @Test fun mixedExamplesAreSeparatedWithoutLosingText() {
        val chinese = "他一直很喜欢古典音乐。"
        val english = "He has always had a fondness for classical music."
        val blocks = readingBlocks("$chinese $english")
        assertEquals(listOf(chinese, english), blocks.map { it.text })
        assertEquals(listOf(ReadingRole.CHINESE_EXAMPLE, ReadingRole.ENGLISH_EXAMPLE), blocks.map { it.role })
    }
    @Test fun ipaMeaningAndCollocationHaveDistinctRoles() {
        val blocks = readingBlocks("n. /ˈfɒndnəs/ /ˈfɑːndnəs/\n喜爱；钟爱\nhave a fondness for\n喜欢；钟爱……")
        assertEquals(listOf(ReadingRole.IPA, ReadingRole.MEANING, ReadingRole.COLLOCATION, ReadingRole.TRANSLATION), blocks.map { it.role })
    }
    @Test fun arbitraryAndLongContentIsNeverTruncated() {
        val line = "自定义 mixed content " + "long text ".repeat(500)
        assertEquals(line, readingBlocks(line).single().text)
        assertTrue(readingBlocks("").isEmpty())
    }
}
