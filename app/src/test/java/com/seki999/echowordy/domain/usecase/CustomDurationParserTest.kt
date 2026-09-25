package com.seki999.echowordy.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomDurationParserTest {

    @Test
    fun `a positive whole number is accepted`() {
        assertEquals(30, CustomDurationParser.parseSeconds("30"))
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        assertEquals(12, CustomDurationParser.parseSeconds("  12  "))
    }

    @Test
    fun `zero is rejected`() {
        assertNull(CustomDurationParser.parseSeconds("0"))
    }

    @Test
    fun `negative numbers are rejected`() {
        assertNull(CustomDurationParser.parseSeconds("-5"))
    }

    @Test
    fun `blank input is rejected`() {
        assertNull(CustomDurationParser.parseSeconds(""))
        assertNull(CustomDurationParser.parseSeconds("   "))
    }

    @Test
    fun `decimal input is rejected`() {
        assertNull(CustomDurationParser.parseSeconds("12.5"))
    }

    @Test
    fun `non-numeric input is rejected`() {
        assertNull(CustomDurationParser.parseSeconds("abc"))
    }

    @Test
    fun `numbers too large to fit an Int are rejected instead of crashing`() {
        assertNull(CustomDurationParser.parseSeconds("999999999999999999999"))
    }
}
